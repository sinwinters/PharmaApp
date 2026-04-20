package com.pharma.application.service;

import com.pharma.application.dto.WriteOffDto;
import com.pharma.application.dto.WriteOffReportDto;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.StockStatus;
import com.pharma.domain.entity.WriteOff;
import com.pharma.domain.entity.WriteOffReason;
import com.pharma.domain.repository.StockRepository;
import com.pharma.domain.repository.WriteOffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WriteOffService {

    private final WriteOffRepository writeOffRepository;
    private final StockRepository stockRepository;

    @Transactional
    public void autoWriteOffExpiredAndDefective() {
        LocalDate today = LocalDate.now();
        for (Stock stock : stockRepository.findAll()) {
            if (stock.getQuantity() == null || stock.getQuantity() <= 0) {
                continue;
            }

            LocalDate expDate = resolveExpirationDate(stock);
            if (stock.getStatus() == StockStatus.DEFECTIVE) {
                createWriteOff(stock, WriteOffReason.DEFECT, "Автосписание: брак");
            } else if ((stock.getStatus() == StockStatus.EXPIRED) || (expDate != null && expDate.isBefore(today))) {
                createWriteOff(stock, WriteOffReason.EXPIRED, "Автосписание: истек срок годности");
                stock.setStatus(StockStatus.EXPIRED);
            } else {
                continue;
            }

            stock.setQuantity(0);
            stock.setReservedQuantity(0);
            stockRepository.save(stock);
        }
    }

    @Transactional(readOnly = true)
    public WriteOffReportDto getReport(LocalDate from,
                                       LocalDate to,
                                       WriteOffReason reason,
                                       Long drugId) {
        LocalDate fromDate = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate toDate = to != null ? to : LocalDate.now();

        List<WriteOff> source = reason == null
                ? writeOffRepository.findAllByCreatedAtBetweenOrderByCreatedAtDesc(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay())
                : writeOffRepository.findAllByCreatedAtBetweenAndReasonOrderByCreatedAtDesc(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay(), reason);

        List<WriteOffDto> items = source.stream()
                .filter(w -> drugId == null || w.getDrug().getId().equals(drugId))
                .map(this::toDto)
                .toList();

        int totalQty = items.stream().mapToInt(WriteOffDto::quantity).sum();
        int totalExpired = items.stream().filter(i -> i.reason() == WriteOffReason.EXPIRED).mapToInt(WriteOffDto::quantity).sum();
        int totalDefect = items.stream().filter(i -> i.reason() == WriteOffReason.DEFECT).mapToInt(WriteOffDto::quantity).sum();

        return new WriteOffReportDto(items, totalQty, totalExpired, totalDefect);
    }

    private void createWriteOff(Stock stock, WriteOffReason reason, String comment) {
        WriteOff writeOff = WriteOff.builder()
                .drug(stock.getDrug())
                .quantity(stock.getQuantity())
                .reason(reason)
                .comment(comment)
                .build();
        writeOffRepository.save(writeOff);
    }

    private WriteOffDto toDto(WriteOff writeOff) {
        return new WriteOffDto(
                writeOff.getId(),
                writeOff.getDrug().getId(),
                writeOff.getDrug().getName(),
                writeOff.getQuantity(),
                writeOff.getReason(),
                writeOff.getComment(),
                writeOff.getCreatedAt()
        );
    }

    private LocalDate resolveExpirationDate(Stock stock) {
        if (stock.getExpirationDate() != null) return stock.getExpirationDate();
        if (stock.getExpiresAt() != null) return stock.getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDate();
        return null;
    }
}
