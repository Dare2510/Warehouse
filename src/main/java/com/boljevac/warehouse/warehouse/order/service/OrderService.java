package com.boljevac.warehouse.warehouse.order.service;

import com.boljevac.warehouse.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatuses;
import com.boljevac.warehouse.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.warehouse.order.dto.OrderResponse;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.warehouse.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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

	//Helper Method to get the Order by id
	public OrderEntity getOrderById(Long id) throws OrderNotFoundException {
		return orderRepository.findById(id).orElseThrow(
				OrderNotFoundException::new
		);
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
		ProductService  productService = new ProductService(productRepository);
		ProductEntity orderedItem = productService.getProductById(orderRequest.getId());

		if(orderedItem.getQuantity() < orderRequest.getQuantity()) {
			throw new OrderExceedsStockException();
		}

		OrderEntity orderEntity = new OrderEntity(
				orderedItem,
				orderRequest.getQuantity()
		);
		orderRepository.save(orderEntity);
		orderedItem.setQuantity(orderedItem.getQuantity()-orderRequest.getQuantity());
		productRepository.save(orderedItem);

		return new OrderResponse(
				orderEntity.getProductEntity().getProduct(),
				orderEntity.getQuantity(),
				orderEntity.getTotalPrice(),
				orderEntity.getStatus()
		);

	}
	@Transactional
	public OrderResponse cancelOrder(Long id) {
		OrderEntity toCancel = getOrderById(id);

		if(toCancel.getStatus().equals(OrderStatuses.ORDER_PLACED)) {
			toCancel.setStatus(OrderStatuses.CANCELLED);
			ProductEntity canceledItem = productRepository.findByProduct(toCancel.getProductEntity().getProduct());
			canceledItem.setQuantity(canceledItem.getQuantity()+toCancel.getQuantity());

			productRepository.save(canceledItem);
			orderRepository.save(toCancel);
		} else{
			throw new OrderCancelNotPossibleException(id);
		}

		return new OrderResponse(
				toCancel.getProductEntity().getProduct(),
				toCancel.getQuantity(),
				toCancel.getTotalPrice(),
				toCancel.getStatus()
		);
	}

}
