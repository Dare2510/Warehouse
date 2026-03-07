package com.boljevac.warehouse.warehouse.order.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ShippedOrders")
public class ShippedEntity {

	@Id
	private Long id;

	@Column(name="product_id",nullable = false)
	private Long productId;

	@Column(name="product_name",nullable = false)
	private String product;

	@Column(name="order_quantity",nullable = false)
	private int quantity;

	@Column(name="total_order_price",nullable = false)
	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	@Column(name="orderstatus",nullable = false)
	private OrderStatuses orderStatuses;

	public ShippedEntity(OrderEntity orderEntity) {
		this.id = orderEntity.getId();
		this.product = orderEntity.getProductEntity().getProduct();
		this.productId = orderEntity.getProductEntity().getId();
		this.quantity = orderEntity.getQuantity();
		this.totalPrice = orderEntity.getTotalPrice();
		this.orderStatuses = orderEntity.getOrderStatuses();
	}

	public ShippedEntity() {
	}

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

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public OrderStatuses getOrderStatuses() {
		return orderStatuses;
	}

	public void setOrderStatuses(OrderStatuses orderStatuses) {
		this.orderStatuses = orderStatuses;
	}
}