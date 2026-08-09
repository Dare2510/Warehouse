package com.boljevac.warehouse.order.repository;

import com.boljevac.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	List<OrderEntity> getByOrderStatus(OrderStatus orderStatus);

	boolean existsByProductEntity(ProductEntity productEntity);
}
