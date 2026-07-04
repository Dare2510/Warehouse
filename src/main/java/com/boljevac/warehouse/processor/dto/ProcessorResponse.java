package com.boljevac.warehouse.processor.dto;

import com.boljevac.warehouse.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProcessorResponse {

		Long productId;
		String product;
		double quantity;
		OrderStatus orderStatus;
}