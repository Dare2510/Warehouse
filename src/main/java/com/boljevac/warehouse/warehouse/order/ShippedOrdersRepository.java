package com.boljevac.warehouse.warehouse.order;

import com.boljevac.warehouse.warehouse.order.entity.ShippedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippedOrdersRepository extends JpaRepository<ShippedEntity, Long> {

}
