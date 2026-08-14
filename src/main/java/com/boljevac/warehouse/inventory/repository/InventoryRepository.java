package com.boljevac.warehouse.inventory.repository;

import com.boljevac.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.product.entity.ProductEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<InventoryEntity> getAllByProductEntity(ProductEntity productEntity);

	boolean existsByProductEntity(ProductEntity productEntity);
}

