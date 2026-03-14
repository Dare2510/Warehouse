package com.boljevac.warehouse.warehouse.order.entity;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Orders")
public class OrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "product_entity_id")
	private ProductEntity productEntity;

	@Column(name="order_quantity",nullable = false)
	private int quantity;

	@Column(name="order_total_price",nullable = false)
	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	@Column(name="order_status",nullable = false)
	private OrderStatus orderStatus;

	public OrderEntity(ProductEntity productEntity, int quantity) {
		this.productEntity = productEntity;
		this.quantity = quantity;
		this.totalPrice = BigDecimal.valueOf(quantity).multiply(productEntity.getPricePerPiece());
		this.orderStatus = OrderStatus.ORDER_PLACED;
	}

	public OrderEntity() {
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


	public ProductEntity getProductEntity() {
		return productEntity;
	}

	public void setProductEntity(ProductEntity productEntity) {
		this.productEntity = productEntity;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}
}
