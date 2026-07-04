package com.boljevac.warehouse.order.dto;

import com.boljevac.warehouse.order.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderResponse {
		String product;
		 int quantity;
		 BigDecimal totalPrice;
		 OrderStatus orderStatus;

	}
