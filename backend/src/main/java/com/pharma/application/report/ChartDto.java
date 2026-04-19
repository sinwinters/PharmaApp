package com.pharma.application.report;

import java.util.List;

public record ChartDto(
        List<String> labels,
        List<Number> values
) {
}
