package com.boljevac.warehouse.warehouse.order.entity;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Orders")
public class OrderEntity {


	@ManyToOne
	@JoinColumn(name = "product_entity_id")
	private ProductEntity productEntity;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	public OrderEntity(ProductEntity productEntity, int quantity) {
		this.productEntity = productEntity;
		this.quantity = quantity;
		this.totalPrice = BigDecimal.valueOf(quantity).multiply(productEntity.getValuePerPiece());
		this.status = OrderStatus.ORDER_PLACED;
	}

	public OrderEntity() {}


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

	public ProductEntity getProductEntity() {
		return productEntity;
	}

	public void setProductEntity(ProductEntity productEntity) {
		this.productEntity = productEntity;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
