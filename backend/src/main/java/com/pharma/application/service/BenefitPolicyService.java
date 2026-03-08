package com.pharma.application.service;

import com.pharma.application.dto.BenefitProgramDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class BenefitPolicyService {

    private static final Map<String, BenefitProgramDto> PROGRAMS = Map.of(
            "RB_DISABLED_GROUP_1_2", new BenefitProgramDto(
                    "RB_DISABLED_GROUP_1_2",
                    "Льгота для инвалидов I и II группы",
                    "Закон РБ о государственных социальных льготах (категории граждан с правом на льготное лекарственное обеспечение)",
                    new BigDecimal("50"),
                    "Скидка 50% на лекарственные средства по предъявлению подтверждающих документов"
            ),
            "RB_CHILD_UNDER_3", new BenefitProgramDto(
                    "RB_CHILD_UNDER_3",
                    "Льгота для детей до 3 лет",
                    "Нормативные акты Минздрава РБ по льготному обеспечению детей раннего возраста",
                    new BigDecimal("30"),
                    "Скидка 30% на препараты, отпускаемые по назначению"
            ),
            "RB_CHRONIC_DISEASE", new BenefitProgramDto(
                    "RB_CHRONIC_DISEASE",
                    "Льгота при хронических заболеваниях",
                    "Регламенты Минздрава РБ по обеспечению пациентов с хроническими заболеваниями",
                    new BigDecimal("20"),
                    "Скидка 20% для пациентов, состоящих на диспансерном наблюдении"
            )
    );

    public Optional<BenefitProgramDto> resolveProgram(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(PROGRAMS.get(code.trim().toUpperCase(Locale.ROOT)));
    }

    public List<BenefitProgramDto> listPrograms() {
        return PROGRAMS.values().stream().sorted((a, b) -> a.code().compareTo(b.code())).toList();
    }
}
