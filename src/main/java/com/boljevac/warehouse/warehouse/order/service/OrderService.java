package com.boljevac.warehouse.warehouse.order.service;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
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
import com.boljevac.warehouse.warehouse.product.exception.EmptyProductRepositoryException;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.warehouse.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final InventoryRepository inventoryRepository;
	private final ProductRepository productRepository;


	public OrderService(OrderRepository orderRepository,
						InventoryRepository inventoryRepository,
						ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.inventoryRepository = inventoryRepository;
		this.productRepository = productRepository;
	}

	public OrderEntity getOrderById(Long id) throws OrderNotFoundException {
		return orderRepository.findById(id).orElseThrow(
				OrderNotFoundException::new
		);
	}

	public List<ProductResponse> getProducts() {
		List<ProductResponse> products = new ArrayList<>();

		for (ProductEntity productEntity : productRepository.findAll()) {
			products.add(new ProductResponse(
					productEntity.getId(),
					productEntity.getProduct(),
					productEntity.getPricePerPiece(),
					productEntity.getWeightPerPiece()));
		}

		if (products.isEmpty()) {
			throw new EmptyProductRepositoryException();
		}

		return products;
	}

	@Transactional
	public OrderResponse createOrder(OrderRequest orderRequest) {
		ProductService productService = new ProductService(productRepository);
		ProductEntity orderedItem = productService.getProductById(orderRequest.getId());

		InventoryEntity item = inventoryRepository.getById(orderedItem.getId());

		if (item.getQuantity() < orderRequest.getQuantity()) {
			throw new OrderExceedsStockException();
		}



		OrderEntity orderEntity = new OrderEntity(
				orderedItem,
				orderRequest.getQuantity()
		);
		orderRepository.save(orderEntity);
		item.setQuantity(item.getQuantity() - orderRequest.getQuantity());
		productRepository.save(orderedItem);

		return new OrderResponse(
				orderEntity.getProductEntity().getProduct(),
				orderEntity.getQuantity(),
				orderEntity.getTotalPrice(),
				orderEntity.getOrderStatuses()
		);

	}

	@Transactional
	public OrderResponse cancelOrder(Long id) {
		OrderEntity toCancel = getOrderById(id);
		InventoryEntity item =  inventoryRepository.getById(toCancel.getProductEntity().getId());


		if (toCancel.getOrderStatuses().equals(OrderStatuses.ORDER_PLACED)) {
			toCancel.setOrderStatuses(OrderStatuses.CANCELLED);
			ProductEntity canceledItem = productRepository.findByProduct(toCancel.getProductEntity().getProduct());
			item.setQuantity(item.getQuantity() + toCancel.getQuantity());

			productRepository.save(canceledItem);
			orderRepository.save(toCancel);
		} else {
			throw new OrderCancelNotPossibleException(id);
		}

		return new OrderResponse(
				toCancel.getProductEntity().getProduct(),
				toCancel.getQuantity(),
				toCancel.getTotalPrice(),
				toCancel.getOrderStatuses()
		);
	}

}
