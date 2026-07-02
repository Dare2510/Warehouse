package com.boljevac.warehouse.order.dto;

import com.boljevac.warehouse.order.entity.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse
		(String product,
		 int quantity,
		 BigDecimal totalPrice,
		 OrderStatus orderStatus) {
}
