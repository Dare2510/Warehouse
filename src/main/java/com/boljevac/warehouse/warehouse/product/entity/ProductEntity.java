package com.boljevac.warehouse.warehouse.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "Products")
@Getter
@Setter
@NoArgsConstructor
public class ProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="product_name",nullable = false)
	private String product;

	@Column(name="price_per_piece",nullable = false)
	private BigDecimal pricePerPiece;

	@Column(name="weight_per_piece", nullable = false)
	private double weightPerPiece;

	public ProductEntity(String product, BigDecimal pricePerPiece, double weightPerPiece ) {
		this.product = product;
		this.pricePerPiece = pricePerPiece;
		this.weightPerPiece = weightPerPiece;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;

		ProductEntity product1 = (ProductEntity) o;
		return Double.compare(weightPerPiece, product1.weightPerPiece) == 0
				&& product.equals(product1.product) && pricePerPiece.equals(product1.pricePerPiece);
	}

	@Override
	public int hashCode() {
		int result = product.hashCode();
		result = 31 * result + pricePerPiece.hashCode();
		result = 31 * result + Double.hashCode(weightPerPiece);
		return result;
	}

	@Override
	public String toString() {
		return " "+product;
	}
}
