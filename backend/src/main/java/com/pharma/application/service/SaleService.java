package com.pharma.application.service;

import com.pharma.application.dto.BenefitProgramDto;
import com.pharma.application.dto.ReceiptDto;
import com.pharma.application.dto.SaleCreateRequest;
import com.pharma.application.dto.SaleDto;
import com.pharma.application.dto.SaleItemDto;
import com.pharma.application.dto.SaleRequestDto;
import com.pharma.application.dto.SaleResponseDto;
import com.pharma.application.exception.InsufficientStockException;
import com.pharma.application.exception.PharmaException;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.application.exception.PrescriptionNotVerifiedException;
import com.pharma.application.exception.PrescriptionRequiredException;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Sale;
import com.pharma.domain.entity.SaleItem;
import com.pharma.domain.entity.SaleStatus;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.Prescription;
import com.pharma.domain.entity.User;
import com.pharma.domain.observer.LowStockNotifier;
import com.pharma.domain.repository.DrugRepository;
import com.pharma.domain.repository.SaleRepository;
import com.pharma.domain.repository.StockRepository;
import com.pharma.domain.repository.UserRepository;
import com.pharma.domain.strategy.PricingStrategy;
import com.pharma.infrastructure.integration.AvestEdsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Фасад продажи: проведение продажи + списание остатков + уведомление о низком остатке (паттерн Facade).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private static final long EXPIRING_SOON_DAYS = 30;

    private final SaleRepository saleRepository;
    private final DrugRepository drugRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PricingStrategy pricingStrategy;
    private final LowStockNotifier lowStockNotifier;
    private final BenefitPolicyService benefitPolicyService;
    private final AvestEdsGateway avestEdsGateway;
    private final ReceiptService receiptService;

    @Transactional
    public SaleDto createSale(SaleCreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username));

        Optional<BenefitProgramDto> benefit = benefitPolicyService.resolveProgram(request.benefitCode());
        BigDecimal discountPercent = benefit.map(BenefitProgramDto::discountPercent).orElse(BigDecimal.ZERO);

        Sale sale = Sale.builder()
                .user(user)
                .totalAmount(BigDecimal.ZERO)
                .build();

        Prescription linkedPrescription = resolveLegacyPrescription(request);

        List<SaleItem> items = new ArrayList<>();
        List<Drug> soldDrugs = new ArrayList<>();
        BigDecimal totalBeforeDiscount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (var req : request.items()) {
            Drug drug = drugRepository.findByIdWithAssociations(req.drugId())
                    .orElseThrow(() -> new ResourceNotFoundException("Лекарство", req.drugId()));
            soldDrugs.add(drug);
            enforcePrescriptionPolicy(drug, linkedPrescription);
            Stock stock = stockRepository.findByDrugId(drug.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Остаток", req.drugId()));
            int qty = req.quantity();
            inventoryPolicyService.assertSellAllowed(stock);
            if (stock.getQuantity() < qty) {
                throw new InsufficientStockException(drug.getName(), qty, Optional.of(stock.getQuantity()));
            }

            BigDecimal unitPrice = drug.getBasePrice();
            BigDecimal lineBase = pricingStrategy.calculatePrice(drug, qty);
            BigDecimal lineDiscount = lineBase.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .drug(drug)
                    .drugName(drug.getName())
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .total(lineBase.subtract(lineDiscount))
                    .build();
            items.add(item);

            totalBeforeDiscount = totalBeforeDiscount.add(lineBase);
            totalDiscount = totalDiscount.add(lineDiscount);

            decreaseStock(stock, qty);
            if (stock.getQuantity() <= drug.getMinQuantity()) {
                lowStockNotifier.notifyLowStock(drug, stock.getQuantity(), drug.getMinQuantity());
            }
        }

        boolean edsRequired = soldDrugs.stream().anyMatch(d -> Boolean.TRUE.equals(d.getRequiresEdsSignature()));
        boolean edsValidated = validateEdsIfRequired(request, edsRequired);

        sale.setEdsRequired(edsRequired);
        sale.setEdsValidated(edsValidated);
        sale.setEdsProvider(edsRequired ? request.edsProvider() : null);
        sale.setPrescriptionNumber(edsRequired ? request.prescriptionNumber() : null);
        sale.setStatus(SaleStatus.COMPLETED);

        sale.setTotalAmount(totalBeforeDiscount.subtract(totalDiscount));
        sale.setItems(items);
        sale = saleRepository.save(sale);
        auditLogService.log("SALE", username, "Sale", sale.getId());

        return toDto(sale, discountPercent, benefit, edsRequired, edsValidated, request.edsProvider(), request.prescriptionNumber());
    }

    @Transactional
    public SaleResponseDto createSale(SaleRequestDto request, String username) {
        return createSaleInternal(request, username, false);
    }

    @Transactional
    public SaleResponseDto createSaleByCard(SaleRequestDto request, String username) {
        if (request.medicalCardNumber() == null || request.medicalCardNumber().isBlank()) {
            throw new PharmaException("Для продажи по медкарте требуется medicalCardNumber");
        }
        return createSaleInternal(request, username, true);
    }

    @Transactional(readOnly = true)
    public SaleResponseDto findSaleResponseById(Long id) {
        Sale sale = saleRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Продажа", id));
        return toResponseDto(sale);
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(Long id) {
        Sale sale = saleRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Продажа", id));
        return receiptService.generateReceipt(sale);
    }

    @Transactional(readOnly = true)
    public Page<SaleDto> findAll(Pageable pageable) {
        return saleRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(sale -> toDto(sale, BigDecimal.ZERO, Optional.empty(),
                        sale.getEdsRequired(),
                        sale.getEdsValidated(),
                        sale.getEdsProvider(),
                        sale.getPrescriptionNumber()));
    }

    @Transactional(readOnly = true)
    public Optional<SaleDto> findById(Long id) {
        return saleRepository.findWithItemsById(id)
                .map(sale -> toDto(sale, BigDecimal.ZERO, Optional.empty(),
                        sale.getEdsRequired(),
                        sale.getEdsValidated(),
                        sale.getEdsProvider(),
                        sale.getPrescriptionNumber()));
    }

    private SaleResponseDto createSaleInternal(SaleRequestDto request, String username, boolean forcePrescriptionSale) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username));

        boolean cardSale = request.medicalCardNumber() != null && !request.medicalCardNumber().isBlank();
        Sale sale = Sale.builder()
                .user(user)
                .paymentType(request.paymentType())
                .medicalCardNumber(cardSale ? request.medicalCardNumber() : null)
                .isPrescriptionSale(cardSale || forcePrescriptionSale)
                .status(SaleStatus.COMPLETED)
                .edsRequired(false)
                .edsValidated(false)
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<SaleItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var req : request.items()) {
            Drug drug = drugRepository.findById(req.drugId())
                    .orElseThrow(() -> new ResourceNotFoundException("Лекарство", req.drugId()));

            Stock stock = stockRepository.findByDrugId(req.drugId())
                    .orElseThrow(() -> new ResourceNotFoundException("Остаток", req.drugId()));

            validateExpiry(stock, drug);

            if (stock.getQuantity() < req.quantity()) {
                throw new InsufficientStockException(drug.getName(), req.quantity(), Optional.of(stock.getQuantity()));
            }

            BigDecimal effectivePrice = req.price() != null ? req.price() : drug.getBasePrice();
            if (effectivePrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new PharmaException("Цена не может быть отрицательной");
            }

            BigDecimal lineTotal = effectivePrice.multiply(BigDecimal.valueOf(req.quantity()));

            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .drug(drug)
                    .drugName(drug.getName())
                    .quantity(req.quantity())
                    .unitPrice(effectivePrice)
                    .total(lineTotal)
                    .build();
            items.add(item);

            decreaseStock(stock, req.quantity());
            if (stock.getQuantity() <= drug.getMinQuantity()) {
                lowStockNotifier.notifyLowStock(drug, stock.getQuantity(), drug.getMinQuantity());
            }

            totalAmount = totalAmount.add(lineTotal);
        }

        sale.setTotalAmount(totalAmount);
        sale.setItems(items);
        sale.setPrescription(linkedPrescription);
        Sale saved = saleRepository.save(sale);

        auditLogService.log("SALE", username, "Sale", saved.getId());

        if (saved.getIsPrescriptionSale()) {
            log.info("Продажа по медкарте выполнена: saleId={}, card={}", saved.getId(), saved.getMedicalCardNumber());
        }

        return toResponseDto(saved);
    }

    private void validateExpiry(Stock stock, Drug drug) {
        if (stock.getExpiresAt() == null) {
            return;
        }

        Instant now = Instant.now();
        if (stock.getExpiresAt().isBefore(now)) {
            throw new PharmaException("Нельзя продать просроченный препарат: " + drug.getName());
        }

        Instant soonBorder = now.plus(EXPIRING_SOON_DAYS, ChronoUnit.DAYS);
        if (stock.getExpiresAt().isBefore(soonBorder)) {
            log.warn("Срок годности скоро истекает: drugId={}, drugName={}, expiresAt={}",
                    drug.getId(), drug.getName(), stock.getExpiresAt());
        }
    }

    private void decreaseStock(Stock stock, int quantity) {
        int newQuantity = stock.getQuantity() - quantity;
        if (newQuantity < 0) {
            throw new PharmaException("Недостаточный остаток для списания");
        }
        stock.setQuantity(newQuantity);
        stockRepository.save(stock);
    }

    private boolean validateEdsIfRequired(SaleCreateRequest request, boolean edsRequired) {
        if (!edsRequired) return false;

        if (request.prescriptionNumber() == null || request.prescriptionNumber().isBlank()) {
            throw new PharmaException("Для антибиотиков/наркосодержащих препаратов требуется номер электронного рецепта");
        }
        if (request.edsProvider() == null || !"AVEST".equalsIgnoreCase(request.edsProvider())) {
            throw new PharmaException("Для контролируемых препаратов поддерживается ЭЦП провайдера Avest");
        }

        boolean ok = avestEdsGateway.verify(request.prescriptionNumber(), request.edsSignature());
        if (!ok) {
            throw new PharmaException("ЭЦП Avest не прошла проверку для электронного рецепта");
        }
        return true;
    }

    private SaleResponseDto toResponseDto(Sale sale) {
        return new SaleResponseDto(
                sale.getId(),
                sale.getTotalAmount(),
                sale.getCreatedAt(),
                sale.getPaymentType(),
                sale.getMedicalCardNumber(),
                sale.getIsPrescriptionSale(),
                sale.getStatus()
        );
    }

    private SaleDto toDto(Sale s,
                          BigDecimal discountPercent,
                          Optional<BenefitProgramDto> benefit,
                          boolean edsRequired,
                          boolean edsValidated,
                          String edsProvider,
                          String prescriptionNumber) {
        BigDecimal totalBeforeDiscount = s.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal effectiveDiscountPercent = discountPercent;
        if (effectiveDiscountPercent.compareTo(BigDecimal.ZERO) == 0 && totalBeforeDiscount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmountDerived = totalBeforeDiscount.subtract(s.getTotalAmount());
            if (discountAmountDerived.compareTo(BigDecimal.ZERO) > 0) {
                effectiveDiscountPercent = discountAmountDerived.multiply(BigDecimal.valueOf(100))
                        .divide(totalBeforeDiscount, 2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal finalDiscountPercent = effectiveDiscountPercent;
        List<SaleItemDto> itemDtos = s.getItems().stream()
                .map(i -> {
                    BigDecimal lineBase = i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
                    BigDecimal lineDiscount = lineBase.multiply(finalDiscountPercent)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    return new SaleItemDto(
                            i.getDrug().getId(),
                            i.getDrugName() != null ? i.getDrugName() : i.getDrug().getName(),
                            i.getQuantity(),
                            i.getUnitPrice(),
                            lineBase,
                            finalDiscountPercent,
                            lineBase.subtract(lineDiscount)
                    );
                })
                .toList();

        BigDecimal discountAmount = totalBeforeDiscount.subtract(s.getTotalAmount());

        return new SaleDto(
                s.getId(),
                s.getUser().getId(),
                s.getUser().getUsername(),
                s.getTotalAmount(),
                totalBeforeDiscount,
                discountAmount,
                benefit.map(BenefitProgramDto::code).orElse(null),
                benefit.map(BenefitProgramDto::lawReference).orElse(null),
                edsRequired,
                edsValidated,
                edsProvider,
                prescriptionNumber,
                s.getCreatedAt(),
                itemDtos
        );
    }
}
