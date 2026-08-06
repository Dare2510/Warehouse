package com.boljevac.warehouse.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ProductResponse {

	private Long id;
	private String name;
	private BigDecimal price;
	private double weight;
	private Long userId;

}
