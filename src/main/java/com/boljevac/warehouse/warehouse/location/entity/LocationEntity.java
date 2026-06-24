package com.boljevac.warehouse.warehouse.location.entity;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Locations")
@Getter
@Setter
@NoArgsConstructor
public class LocationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_entity_id")
	private ProductEntity productEntity;

	@Enumerated(EnumType.STRING)
	private LocationType locationType;

	@Column(name = "aisle", nullable = false)
	private String aisle;

	@Column(name = "rack", nullable = false)
	private int rack;

	@Column(name = "level", nullable = false)
	private int level;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "loaded", nullable = false)
	private boolean loaded;

	@Column(name = "remaining_weight_to_store", nullable = false)
	private double remainingWeightToStore = 1000;


	public LocationEntity(ProductEntity productEntity, LocationType locationType, int quantity, boolean loaded) {
		{
			this.productEntity = productEntity;
			this.setLocationType(locationType);
			this.quantity = quantity;
			this.loaded = false;
		}
	}

	@Override
	public String toString() {
		if (locationType == LocationType.BLOCK) {
			return "Block";
		} else {
			return " " + aisle + "-" + rack + "-" + level;
		}
	}

	public void setLocationType(LocationType locationType) {
		if (locationType == LocationType.BLOCK) {
			setAisle(Aisle.Floor.toString());
			setLevel(0);
			setRack(0);
		}
		if (locationType == LocationType.STORAGE) {
			setAisle(this.aisle);
			setLevel(this.level);
			setRack(this.rack);
		}
		this.locationType = locationType;
	}
}
