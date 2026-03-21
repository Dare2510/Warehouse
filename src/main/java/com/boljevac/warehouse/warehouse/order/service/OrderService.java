package com.boljevac.warehouse.warehouse.order.service;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.warehouse.order.dto.OrderResponse;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelOrDeleteNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.exception.EmptyProductRepositoryException;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.warehouse.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final InventoryRepository inventoryRepository;
	private final ProductRepository productRepository;
	private final LocationsRepository locationsRepository;
	private final ProductService productService;
	private final Logger logger = LoggerFactory.getLogger(OrderService.class);


	public OrderService(OrderRepository orderRepository,
						InventoryRepository inventoryRepository,
						ProductRepository productRepository, LocationsRepository locationsRepository, ProductService productService) {
		this.orderRepository = orderRepository;
		this.inventoryRepository = inventoryRepository;
		this.productRepository = productRepository;
		this.locationsRepository = locationsRepository;
		this.productService = productService;
	}

	public List<ProductResponse> getListOfProducts() {
		List<ProductResponse> productList = new ArrayList<>();

		for (ProductEntity productEntity : productRepository.findAll()) {
			productList.add(new ProductResponse(
					productEntity.getId(),
					productEntity.getProduct(),
					productEntity.getPricePerPiece(),
					productEntity.getWeightPerPiece()));
		}

		if (productList.isEmpty()) {
			throw new EmptyProductRepositoryException();
		}

		return productList;
	}


	@Transactional
	public OrderResponse createOrder(OrderRequest orderRequest) {

		ProductEntity orderedItem = productService.getProductById(orderRequest.getProductId());
		List<InventoryEntity> inventories = inventoryRepository.getAllByProductEntity(orderedItem);

		int totalAvailableQuantity = inventories.stream().mapToInt(InventoryEntity::getQuantity).sum();
		int orderedQuantity = orderRequest.getQuantity();
		int remainingToFulfill = orderRequest.getQuantity();

		validateOrderQuantity(totalAvailableQuantity, orderedQuantity);

		OrderEntity fulfilledOrder = getQuantitiesAndReturnOrder(orderedItem, remainingToFulfill, inventories);
		orderRepository.save(fulfilledOrder);
		logger.info("New Order with Id {} has been created", fulfilledOrder.getId());

		return mapToResponse(fulfilledOrder);

	}

	@Transactional
	public OrderResponse cancelOrder(Long id) {
		OrderEntity orderToCancel = getOrderById(id);

		boolean cancelIsValid = validateCancelRequest(orderToCancel);

		if (cancelIsValid) {
			updateInventory(orderToCancel);
			logger.info("Order with Id {} has been cancelled", orderToCancel.getId());
		} else {
				throw new OrderCancelOrDeleteNotPossibleException(orderToCancel.getId());
			}
		return mapToResponse(orderToCancel);
	}

	//Helper Methods

	private boolean validateCancelRequest(OrderEntity orderToCancel) {
		return orderToCancel.getOrderStatus().equals(OrderStatus.ORDER_PLACED);
	}

	private void updateInventory(OrderEntity orderToCancel){

			orderToCancel.setOrderStatus(OrderStatus.CANCELLED);
			ProductEntity canceledItem = productService.getProductById(orderToCancel.getProductEntity().getId());
			LocationEntity newLocation = new LocationEntity(canceledItem, LocationType.BLOCK, orderToCancel.getQuantity(),true);

			InventoryEntity canceledQuantity = new InventoryEntity(
					canceledItem,
					newLocation,
					orderToCancel.getQuantity(),
					newLocation.toString());

			inventoryRepository.save(canceledQuantity);
			orderRepository.save(orderToCancel);
			locationsRepository.save(newLocation);

		}

	public OrderEntity getOrderById(Long id) throws OrderNotFoundException {
		return orderRepository.findById(id).orElseThrow(
				OrderNotFoundException::new
		);
	}

	private void validateOrderQuantity(int totalStockQuantity, int orderedQuantity) throws OrderExceedsStockException {
		if (totalStockQuantity < orderedQuantity) {
			throw new OrderExceedsStockException();
		}
	}

	private OrderEntity getQuantitiesAndReturnOrder(ProductEntity product,int remaining,List<InventoryEntity> inventories) {

		int orderedQuantity = remaining;

		for(InventoryEntity inventoryEntity : inventories) {
			if(remaining <=0) {
				break;
			}

			int available = inventoryEntity.getQuantity();

			if(available <= remaining) {
				remaining -= available;
				inventoryEntity.getLocationEntity().setLoaded(false);
				inventoryEntity.getLocationEntity().setQuantity(0);
				inventoryEntity.getLocationEntity().setProductEntity(null);
				inventoryEntity.getLocationEntity().setRemainingWeightToStore(1000);

				inventoryEntity.setQuantity(0);
				inventoryEntity.setTotalWeight(0);
				inventoryEntity.setLocationEntity(null);
				inventoryEntity.setProductEntity(null);



			} else {
				inventoryEntity.setQuantity(inventoryEntity.getQuantity() - remaining);
				inventoryEntity.setTotalWeight(inventoryEntity.getTotalWeight()
						- (inventoryEntity.getProductEntity().getWeightPerPiece()*remaining));

				inventoryEntity.getLocationEntity().setQuantity(inventoryEntity.getLocationEntity().getQuantity() - remaining);
				inventoryEntity.getLocationEntity().setRemainingWeightToStore(inventoryEntity.getLocationEntity().getRemainingWeightToStore()
						+(inventoryEntity.getProductEntity().getWeightPerPiece()*remaining));
				remaining = 0;
			}
			inventoryRepository.save(inventoryEntity);

		}

		return new OrderEntity(product,orderedQuantity);

	}

	private OrderResponse mapToResponse(OrderEntity orderEntity) {
		return new OrderResponse(
				orderEntity.getProductEntity().getProduct(),
				orderEntity.getQuantity(),
				orderEntity.getTotalPrice(),
				orderEntity.getOrderStatus()
		);
	}

}
