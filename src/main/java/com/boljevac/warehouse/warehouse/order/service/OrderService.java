package com.boljevac.warehouse.warehouse.order.service;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.location.entity.LocationType;
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
	private final ProductService productService;


	public OrderService(OrderRepository orderRepository,
						InventoryRepository inventoryRepository,
						ProductRepository productRepository, ProductService productService) {
		this.orderRepository = orderRepository;
		this.inventoryRepository = inventoryRepository;
		this.productRepository = productRepository;
		this.productService = productService;
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

		ProductEntity orderedItem = productService.getProductById(orderRequest.getId());

		List<InventoryEntity> inventories = inventoryRepository.getAllByProductEntity(orderedItem);

		int totalQuantity = inventories.stream().mapToInt(InventoryEntity::getQuantity).sum();

		if (totalQuantity < orderRequest.getQuantity()) {
			throw new OrderExceedsStockException();
		}

		OrderEntity orderEntity = new OrderEntity(
				orderedItem,
				orderRequest.getQuantity()
		);

		int remaining = orderRequest.getQuantity();
		for(InventoryEntity inventoryEntity : inventories) {
			if(remaining <=0) {
				break;
			}

			int available = inventoryEntity.getQuantity();

			if(available <= remaining) {
				remaining -= available;
				inventoryEntity.setQuantity(0);
				inventoryEntity.setTotalWeight(0);
			} else {
				inventoryEntity.setQuantity(inventoryEntity.getQuantity() - remaining);
				inventoryEntity.setTotalWeight(inventoryEntity.getTotalWeight()
						- (orderedItem.getWeightPerPiece()*remaining));
				remaining = 0;
			}
			inventoryRepository.save(inventoryEntity);

		}

		orderRepository.save(orderEntity);

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


		if (toCancel.getOrderStatuses().equals(OrderStatuses.ORDER_PLACED)) {
			toCancel.setOrderStatuses(OrderStatuses.CANCELLED);
			ProductEntity canceledItem = productRepository.findByProduct(toCancel.getProductEntity().getProduct());
			LocationEntity newLocation = new LocationEntity(canceledItem, LocationType.BLOCK, toCancel.getQuantity(),true);
			InventoryEntity canceledQuantity = new InventoryEntity(
					canceledItem,
					newLocation,
					toCancel.getQuantity(),
					newLocation.toString());

			inventoryRepository.save(canceledQuantity);
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
