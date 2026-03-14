package com.boljevac.warehouse.warehouse.processor.dto;

import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;

public record ProcessorResponse(
		Long productId,
		String product,
		double quantity,
		OrderStatus orderStatus) {


}