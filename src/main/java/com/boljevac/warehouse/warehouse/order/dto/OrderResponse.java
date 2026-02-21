package com.boljevac.warehouse.warehouse.order.dto;

import com.boljevac.warehouse.warehouse.order.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse
		(String product,
		 int quantity,
		 BigDecimal totalPrice,
		 OrderStatus orderStatus) {
}
