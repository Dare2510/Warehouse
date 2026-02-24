package com.boljevac.warehouse.warehouse.product.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Products")
public class ProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String product;

	@Column(nullable = false)
	private BigDecimal valuePerPiece;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private BigDecimal totalValue;

	public ProductEntity(String product, BigDecimal valuePerPiece, int quantity) {
		this.product = product;
		this.valuePerPiece = valuePerPiece;
		this.quantity = quantity;
		this.totalValue = new BigDecimal(valuePerPiece.doubleValue() * quantity);
	}

	public ProductEntity() {}

	public Long getId() {
		return id;
	}

	public String getProduct() {
		return product;
	}

	public BigDecimal getValuePerPiece() {
		return valuePerPiece;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public void setValuePerPiece(BigDecimal valuePerPiece) {
		this.valuePerPiece = valuePerPiece;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getTotalValue() {
		return totalValue;
	}
	public void setTotalValue(BigDecimal totalValue) {
		this.totalValue = totalValue;
	}
}
