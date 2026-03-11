package com.boljevac.warehouse.warehouse.product.dto;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price, double weight) {

}
