package com.boljevac.warehouse.warehouse.order.repository;

import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	 List<OrderEntity> getOrdersByStatus(OrderStatus status);

	 OrderEntity getOrderById(Long id);

	 List<OrderEntity> getOrderByStatus(OrderStatus status);
}
