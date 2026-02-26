package com.boljevac.warehouse.warehouse.order.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ShippedOrders")
public class ShippedEntity {

	@Id
	private Long id;

	@Column(nullable = false)
	private Long productId;

	@Column(nullable = false)
	private String product;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	public ShippedEntity(OrderEntity orderEntity) {
		this.id = orderEntity.getId();
		this.product = orderEntity.getProductEntity().getProduct();
		this.productId = orderEntity.getProductEntity().getId();
		this.quantity = orderEntity.getQuantity();
		this.totalPrice = orderEntity.getTotalPrice();
		this.status = orderEntity.getStatus();
	}

	public ShippedEntity() {}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Long getId() {
		return id;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}
}