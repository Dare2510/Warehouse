package com.boljevac.warehouse.warehouse.inventory.entity;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Inventory")
public class InventoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_entity_id")
	private ProductEntity productEntity;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	public double getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(double totalWeight) {
		this.totalWeight = totalWeight;
	}

	@Column(name="total_weight", nullable = false)
	private double totalWeight;

	@Column(name = "location")
	private String location;

	public InventoryEntity(ProductEntity productEntity, int quantity, String location) {
		this.productEntity = productEntity;
		this.quantity = quantity;
		this.totalWeight = quantity* productEntity.getWeightPerPiece();
		this.location = location;
	}

	public InventoryEntity() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProductEntity getProductEntity() {
		return productEntity;
	}

	public void setProductEntity(ProductEntity productEntity) {
		this.productEntity = productEntity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
