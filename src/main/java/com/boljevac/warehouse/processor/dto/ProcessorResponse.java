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

	private Long productId;
	private String product;
	private double quantity;
	private OrderStatus orderStatus;
}