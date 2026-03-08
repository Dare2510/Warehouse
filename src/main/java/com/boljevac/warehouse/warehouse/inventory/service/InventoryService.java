package com.boljevac.warehouse.warehouse.inventory.service;

import com.boljevac.warehouse.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryService {

	private final InventoryRepository inventoryRepository;
	private final ProductRepository productRepository;

	public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
		this.inventoryRepository = inventoryRepository;
		this.productRepository = productRepository;

	}

	public InventoryResponse getStock(Long id) {
	InventoryEntity stockProduct = inventoryRepository.findById(id).orElseThrow(
			() ->  new ProductNotFoundException(id)
	);
	return new InventoryResponse(stockProduct.getProductId().getProduct(),stockProduct.getQuantity());

	}
	@Transactional
	public InventoryResponse createStock(InventoryRequest inventoryRequest) {
		ProductEntity product =  productRepository.findById(inventoryRequest.getProductId()).orElseThrow(
				() ->  new ProductNotFoundException(inventoryRequest.getProductId())
		);
		InventoryEntity existingInventoryProduct = inventoryRepository.findById(product.getId()).orElse(null);

		if(existingInventoryProduct!=null) {
			int currentQuantity = inventoryRepository.getById(product.getId()).getQuantity();
			existingInventoryProduct.setQuantity(currentQuantity+inventoryRequest.getQuantity());
			inventoryRepository.save(existingInventoryProduct);
		}

		InventoryEntity newInventoryProduct = new InventoryEntity(
				product,inventoryRequest.getQuantity(),"Block storage"
		);

		inventoryRepository.save(newInventoryProduct);

		return new InventoryResponse(newInventoryProduct.getProductId().getProduct(), newInventoryProduct.getQuantity());

	}



//	public InventoryResponse storeProduct(InventoryRequest inventoryRequest) {
//
//		ProductEntity productToStore = productRepository.findById(inventoryRequest.getProductId()).orElseThrow(
//				()-> new ProductNotFoundException(inventoryRequest.getProductId())
//		);
//
//		Location location =
//				new Location(inventoryRequest.getAisle(),
//				new Rack(inventoryRequest.getRack()),
//				new LevelOn(inventoryRequest.getLevelOn()));
//
//		boolean locationUsage = inventoryRepository.findByLocation(location).getLocationEntity().isLoaded();
//
//		if(locationUsage){
//			throw new LocationIsLoadedException();
//		}
//
//		InventoryEntity inventoryEntity = new InventoryEntity(
//				productToStore,
//				inventoryRequest.getQuantity(),
//				new LocationEntity(
//						location.getAisle().toString(),
//						location.getRack().getRack(),
//						location.getLevel().getLevel(),
//						true
//				)
//		);
//
//		inventoryRepository.save(inventoryEntity);
//
//
//		return new InventoryResponse(inventoryEntity.getProductEntity()
//				.getProduct(),location.toString(),
//					inventoryEntity.getQuantity());
//
//	}

//	public List<InventoryResponse> getInventory(){
//		return inventoryRepository.findAll().stream()
//				.filter(inventory -> inventory.getProductEntity().getProduct() != null)
//				.map(product ->
//						new InventoryResponse(
//								product.getProductEntity().getProduct(),
//								product.getLocationEntity().toString(),
//								product.getQuantity()
//						)).toList();
//
//	}
}
