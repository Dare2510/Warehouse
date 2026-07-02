package com.boljevac.warehouse.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ShippedOrders")
@Getter
@Setter
@NoArgsConstructor
public class ShippedEntity {

	@Id
	private Long id;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "product_name", nullable = false)
	private String product;

	@Column(name = "order_quantity", nullable = false)
	private int quantity;

	@Column(name = "total_order_price", nullable = false)
	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	@Column(name = "orderstatus", nullable = false)
	private OrderStatus orderStatus;

	public ShippedEntity(OrderEntity orderEntity) {
		this.id = orderEntity.getId();
		this.product = orderEntity.getProductEntity().getProduct();
		this.productId = orderEntity.getProductEntity().getId();
		this.quantity = orderEntity.getQuantity();
		this.totalPrice = orderEntity.getTotalPrice();
		this.orderStatus = orderEntity.getOrderStatus();
	}
}