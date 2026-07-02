package com.boljevac.warehouse.order.repository;

import com.boljevac.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	List<OrderEntity> getByOrderStatus(OrderStatus orderStatus);

	@Query("""
		SELECT COUNT(o) = 0
			FROM OrderEntity o
				WHERE o.user = : user
					AND NOT(
						o.orderStatus = com.boljevac.warehouse.order.entity.OrderStatus.CANCELLED
							AND o.orderStatus = com.boljevac.warehouse.order.entity.OrderStatus.SHIPPED)
		""")
	boolean userCanBeDeleted(@Param("user") UserEntity user);
}
