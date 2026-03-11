package com.boljevac.warehouse.warehouse.location.repository;

import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationsRepository extends JpaRepository<LocationEntity, Long> {

	LocationEntity getLocationById(Long locationId);
	Page<LocationEntity> findByProductEntityIsNotNull(Pageable pageable);
}
