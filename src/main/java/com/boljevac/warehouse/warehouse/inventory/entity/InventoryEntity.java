package com.boljevac.warehouse.warehouse.inventory.entity;

import com.boljevac.warehouse.warehouse.location.*;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "inventory_location")
public class InventoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name="product_entity_id")
	private ProductEntity productEntity;

	@Column(name = "Aisle")
	private String aisle;

	@Column(name="Rack")
	private int rack;

	@Column(name="Level")
	private int levelOn;

	@Column(name="Quantity")
	private int quantity;

	public InventoryEntity(ProductEntity productEntity, String aisle, int rack,
						   int levelOn,  int quantity) {
		this.productEntity = productEntity;
		this.aisle = aisle;
		this.rack = rack;
		this.levelOn = levelOn;
		this.quantity = quantity;
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

	public String getAisle() {
		return aisle;
	}

	public void setAisle(String aisle) {
		this.aisle = aisle;
	}

	public int getRack() {
		return rack;
	}

	public void setRack(int rack) {
		this.rack = rack;
	}

	public int getLevelOn() {
		return levelOn;
	}

	public void setLevelOn(int levelOn) {
		this.levelOn = levelOn;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
