package com.boljevac.warehouse.processor.dto;

import com.boljevac.warehouse.order.entity.OrderStatus;

public record ProcessorResponse(
		Long productId,
		String product,
		double quantity,
		OrderStatus orderStatus) {


}