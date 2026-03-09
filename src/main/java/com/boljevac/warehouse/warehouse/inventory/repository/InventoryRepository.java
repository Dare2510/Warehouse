package com.boljevac.warehouse.warehouse.inventory.repository;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

	public InventoryEntity getById(Long inventoryId);
	public List<InventoryEntity> getAllByProductId(ProductEntity productId);
	public InventoryEntity getByProductId(ProductEntity productId);
}

