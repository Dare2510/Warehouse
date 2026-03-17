package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.exception.ProductDuplicateCreationException;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.warehouse.product.service.ProductService;
import com.boljevac.warehouse.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.warehouse.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

	@Mock
	ProductRepository productRepository;
	@InjectMocks
	ProductService productService;


	@Test
	public void test_double_createProduct() {
		ProductRequest request = new ProductRequest("TestProduct", BigDecimal.valueOf(300), 50);

		when(productRepository.existsByProduct(request.getProduct())).thenReturn(true);

		assertThrows(ProductDuplicateCreationException.class, () -> {
			productService.createAndValidateNewProduct(request);
		});

		verify(productRepository, never()).save(any());

	}

	@Test
	public void test_create_product_and_get_response() {
		ProductRequest request = new ProductRequest("TestProduct", BigDecimal.valueOf(300), 50);
		ProductEntity productEntity = new ProductEntity(request.getProduct(), request.getValue(), request.getWeight());

		when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
		ProductResponse response = productService.createAndValidateNewProduct(request);

		verify(productRepository).save(any(ProductEntity.class));
		assertEquals("TestProduct", response.name());
		assertEquals(BigDecimal.valueOf(300),response.price());
		assertEquals(50, response.weight());

	}

	@Test
	public void test_update_product() {
		ProductRequest newValues = new ProductRequest("TestNewNameProduct", BigDecimal.valueOf(30), 500);
		ProductEntity product = new ProductEntity("TestProduct", BigDecimal.valueOf(30), 500);

		Long id = product.getId();
		when(productRepository.findById(id)).thenReturn(Optional.of(product));
		productService.updateProduct(id, newValues);

		assertEquals("TestNewNameProduct", product.getProduct());
		assertEquals(BigDecimal.valueOf(30), product.getPricePerPiece());
		assertEquals(500, product.getWeightPerPiece());

		verify(productRepository).save(any(ProductEntity.class));

	}


}
