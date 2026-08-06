package com.boljevac.warehouse.order.dto;

import com.boljevac.warehouse.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderResponse {

	private Long orderId;
	private String product;
	private int quantity;
	private BigDecimal totalPrice;
	private OrderStatus orderStatus;

}
