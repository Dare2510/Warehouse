package com.boljevac.warehouse.warehouse.order;

import com.boljevac.warehouse.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.warehouse.order.dto.OrderResponse;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.warehouse.product.ProductEntity;
import com.boljevac.warehouse.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.warehouse.product.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;



	public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
	}

	public List<ProductResponse> getProducts() {
		List<ProductResponse> products = new ArrayList<>();

		for(ProductEntity productEntity : productRepository.findAll()) {
			products.add(new ProductResponse(
					productEntity.getId(),
					productEntity.getProduct(),
					productEntity.getValuePerPiece(),
					productEntity.getQuantity()));
		}

		return products;
	}
	@Transactional
	public OrderResponse createOrder( OrderRequest orderRequest) {
		ProductEntity orderedItem = productRepository.findById(orderRequest.getId()).orElseThrow(
				() -> new ProductNotFoundException(orderRequest.getId())
		);

		if(orderedItem.getQuantity() < orderRequest.getQuantity()) {
			throw new OrderExceedsStockException();
		}

		OrderEntity orderEntity = new OrderEntity(
				orderedItem.getProduct(),
				orderedItem.getId(),
				orderRequest.getQuantity(),
				new BigDecimal(orderRequest.getQuantity()*orderedItem.getValuePerPiece().doubleValue())
		);
		orderRepository.save(orderEntity);
		orderedItem.setQuantity(orderedItem.getQuantity()-orderRequest.getQuantity());
		productRepository.save(orderedItem);

		return new OrderResponse(
				orderEntity.getProduct(),
				orderEntity.getQuantity(),
				orderEntity.getTotalPrice(),
				orderEntity.getStatus()
		);

	}
	@Transactional
	public OrderResponse cancelOrder(Long id) {
		OrderEntity orderEntity = orderRepository.findById(id).orElseThrow(
				OrderNotFoundException::new
		);

		if(orderEntity.getStatus().equals(OrderStatus.ORDER_PLACED)) {
			orderEntity.setStatus(OrderStatus.CANCELLED);
			ProductEntity canceledItem = productRepository.findByProduct(orderEntity.getProduct());
			canceledItem.setQuantity(canceledItem.getQuantity()+orderEntity.getQuantity());

			productRepository.save(canceledItem);
			orderRepository.save(orderEntity);
		} else{
			throw new OrderCancelNotPossibleException(id);
		}

		return new OrderResponse(
				orderEntity.getProduct(),
				orderEntity.getQuantity(),
				orderEntity.getTotalPrice(),
				orderEntity.getStatus()
		);
	}

}
