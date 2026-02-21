package com.boljevac.warehouse.warehouse.service;


import com.boljevac.warehouse.warehouse.product.ProductEntity;
import com.boljevac.warehouse.warehouse.product.ProductRepository;
import com.boljevac.warehouse.warehouse.product.ProductService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

	@Mock
	ProductRepository productRepository;
	@InjectMocks
	ProductService productService;

	@Test
	public void test_create_product_and_get_response() {
		ProductRequest request = new ProductRequest(
				"TestProduct",
				BigDecimal.valueOf(300),
				50
		);
		ProductEntity productEntity = new ProductEntity(
				request.getProduct(),
				request.getValue(),
				request.getQuantity()
		);

		when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);

		ProductResponse response = productService.createItem(request);


		verify(productRepository).save(any(ProductEntity.class));
		assertEquals("TestProduct", response.getName());
		assertEquals(50, response.getQuantity());

	}

	@Test
	public void test_update_product() {
		ProductRequest toUpdate = new ProductRequest(
				"TestNewNameProduct",
				BigDecimal.valueOf(30),
				5000
		);

		ProductEntity productEntity = new ProductEntity(
				"TestProduct",
				BigDecimal.valueOf(30),
				500
		);

		productRepository.save(productEntity);
		Long id = productEntity.getId();
		when(productRepository.findById(id)).thenReturn(Optional.of(productEntity));

		productService.updateProduct(id, toUpdate);

		assertEquals("TestNewNameProduct", productEntity.getProduct());
		assertEquals(5000, productEntity.getQuantity());
		assertEquals(BigDecimal.valueOf(30),productEntity.getValuePerPiece());

		verify(productRepository,times(2)).save(any(ProductEntity.class));

	}



}
