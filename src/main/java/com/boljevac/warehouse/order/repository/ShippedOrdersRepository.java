package com.boljevac.warehouse.order.repository;

import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.order.entity.ShippedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShippedOrdersRepository extends JpaRepository<ShippedEntity, Long> {

	List<ShippedEntity> getByOrderStatus(OrderStatus orderStatus);

}
