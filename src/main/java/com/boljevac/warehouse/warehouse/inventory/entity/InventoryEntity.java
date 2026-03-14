package com.boljevac.warehouse.warehouse.inventory.entity;

import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "Inventory")
public class InventoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_entity_id")
	private ProductEntity productEntity;

	@Enumerated(EnumType.STRING)
	private LocationType locationType;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_entity_id")
	private LocationEntity locationEntity;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name="total_weight", nullable = false)
	private double totalWeight;

	@Column(name = "location")
	private String location;

	public InventoryEntity(ProductEntity productEntity, LocationEntity locationEntity, int quantity, String location) {
		this.productEntity = productEntity;
		this.locationEntity = locationEntity;
		this.locationType = locationEntity.getLocationType();
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

	public double getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(double totalWeight) {
		this.totalWeight = totalWeight;
	}

	public LocationEntity getLocationEntity() {
		return locationEntity;
	}

	public void setLocationEntity(LocationEntity locationEntity) {
		this.locationEntity = locationEntity;
	}

	public LocationType getLocationType() {
		return locationType;
	}

	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}
}
