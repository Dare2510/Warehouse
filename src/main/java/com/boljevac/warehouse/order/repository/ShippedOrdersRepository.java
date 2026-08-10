package com.boljevac.warehouse.order.repository;

import com.boljevac.warehouse.order.entity.ShippedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippedOrdersRepository extends JpaRepository<ShippedEntity, Long> {

	boolean existsByProductId(Long productId);

}
