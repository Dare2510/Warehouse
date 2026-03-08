package com.boljevac.warehouse.warehouse.location.entity;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;

@Entity
@Table(name="Locations")
public class LocationEntity {

	@Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

	@ManyToOne
	@JoinColumn(name = "product_entity_id")
	private ProductEntity productId;

	@Column(name = "Aisle")
	private String aisle;

	@Column(name="Rack")
	private int rack;

	@Column(name = "Level")
	private int level;

	@Column(name="Loaded")
	private boolean loaded;

	@Column(name = "quantity")
	private int quantity;

	public LocationEntity(ProductEntity productId, String aisle, int rack, int level, int quantity) {
		this.productId = productId;
		this.aisle = aisle;
		this.rack = rack;
		this.level = level;
		this.quantity = quantity;
		this.loaded = false;
	}

	public LocationEntity() {
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return " "+aisle +"-"+ rack+"-"+level;
	}

	public ProductEntity getProductId() {
		return productId;
	}

	public void setProductId(ProductEntity productId) {
		this.productId = productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
