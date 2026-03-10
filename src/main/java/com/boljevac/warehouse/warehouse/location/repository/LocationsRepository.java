package com.boljevac.warehouse.warehouse.location.repository;

import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationsRepository extends JpaRepository<LocationEntity, Long> {

	List<LocationEntity> getByLoaded(boolean loaded);
	LocationEntity getLocationById(Long locationId);
}
