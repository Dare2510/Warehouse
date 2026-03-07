package com.boljevac.warehouse.warehouse.inventory.repository;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

}

