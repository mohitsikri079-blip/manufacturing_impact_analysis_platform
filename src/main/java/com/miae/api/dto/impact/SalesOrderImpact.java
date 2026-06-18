package com.miae.api.dto.impact;

import java.math.BigDecimal;

public record SalesOrderImpact(String salesOrderId, BigDecimal orderValue) {
}
