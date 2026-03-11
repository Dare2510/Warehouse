package com.boljevac.warehouse.warehouse.location.entity;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;

@Entity
@Table(name="Locations")
public class LocationEntity {

	@Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_entity_id")
	private ProductEntity productEntity;

	@Column(name = "aisle")
	private String aisle;

	@Column(name="rack")
	private int rack;

	@Column(name = "level")
	private int level;

	@Column(name = "quantity")
	private int quantity;

	@Column(name="loaded")
	private boolean loaded;

	@Column(name="remaining_weight_to_store")
	private double remainingWeightToStore;


	public LocationEntity(ProductEntity productEntity,
						  String aisle, int rack, int level, int quantity) {
		this.productEntity = productEntity;
		this.aisle = aisle;
		this.rack = rack;
		this.level = level;
		this.quantity = quantity;
		this.remainingWeightToStore = 1000.0;
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

	public double getRemainingWeightToStore() {
		return remainingWeightToStore;
	}

	public void setRemainingWeightToStore(double remainingWeightToStore) {
		this.remainingWeightToStore = remainingWeightToStore;
	}
}
