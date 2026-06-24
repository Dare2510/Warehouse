package com.boljevac.warehouse.warehouse.order.entity;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "Orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "product_entity_id")
	private ProductEntity productEntity;

	@Column(name = "order_quantity", nullable = false)
	private int quantity;

	@Column(name = "order_total_price", nullable = false)
	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_status", nullable = false)
	private OrderStatus orderStatus;

	public OrderEntity(ProductEntity productEntity, int quantity) {
		this.productEntity = productEntity;
		this.quantity = quantity;
		this.totalPrice = BigDecimal.valueOf(quantity).multiply(productEntity.getPricePerPiece());
		this.orderStatus = OrderStatus.ORDER_PLACED;
	}
}
