package com.boljevac.warehouse.inventory.entity;

import com.boljevac.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Inventory")
@Getter
@Setter
@NoArgsConstructor
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

	@Column(name = "total_weight", nullable = false)
	private double totalWeight;

	@Column(name = "location")
	private String location;

	public InventoryEntity(ProductEntity productEntity, LocationEntity locationEntity, int quantity, String location) {
		this.productEntity = productEntity;
		this.locationEntity = locationEntity;
		this.locationType = locationEntity.getLocationType();
		this.quantity = quantity;
		this.totalWeight = quantity * productEntity.getWeightPerPiece();
		this.location = location;
	}


}
