package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.product.exception.ProductDuplicateCreationException;
import com.boljevac.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.product.service.ProductService;
import com.boljevac.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.entity.Role;
import com.boljevac.warehouse.user.entity.UserEntity;
import com.boljevac.warehouse.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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

	private static final String PRODUCT_NAME = "TestProduct";
	private static final String UPDATED_PRODUCT_NAME = "TestNewNameProduct";

	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("300");
	private static final BigDecimal UPDATED_PRODUCT_VALUE = new BigDecimal("30");
	private static final double PRODUCT_WEIGHT = 50;
	private static final double UPDATED_PRODUCT_WEIGHT = 30;

	@Mock
	ProductRepository productRepository;
	@Mock
	ProductService productService;
	@Mock
	UserService userService;

	@BeforeEach
	void setUp() {
		productService = new ProductService(productRepository,userService);

	}


	@Test
	public void createAndValidateNewProduct_whenProductAlreadyExists_throwsProductAlreadyExistsException() {
		ProductRequest request = productRequest();
		AuthenticatedUser adminUser = authenticatedAdmin();

		when(productRepository.existsByProduct(request.getProduct())).thenReturn(true);

		assertThrows(ProductDuplicateCreationException.class, () -> {
			productService.createAndValidateNewProduct(adminUser,request);
		});

		verify(productRepository, never()).save(any());

	}

	@Test
	public void createAndValidateNewProduct_whenRequestIsValid_returnsProductResponse() {
		ProductRequest request = productRequest();
		AuthenticatedUser adminUser = authenticatedAdmin();
		UserEntity createdByUser = user(adminUser);
		ProductEntity productEntity = product(createdByUser,request);
		UserEntity user = new UserEntity();

		when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
		when(userService.getUserByAuthenticatedUser(any())).thenReturn(user);
		ProductResponse response = productService.createAndValidateNewProduct(adminUser,request);

		verify(productRepository).save(any(ProductEntity.class));
		assertEquals(PRODUCT_NAME, response.name());
		assertEquals(PRODUCT_VALUE, response.price());
		assertEquals(PRODUCT_WEIGHT, response.weight());

	}

	@Test
	public void updateProduct_whenRequestIsValid_returnsProductResponse() {
		ProductRequest newValues = updatedProductRequest();
		AuthenticatedUser adminUser = authenticatedAdmin();
		UserEntity createdByUser = user(adminUser);
		ProductEntity product = product(createdByUser,newValues);


		Long id = product.getId();
		when(productRepository.findById(id)).thenReturn(Optional.of(product));
		productService.updateProduct(adminUser,id, newValues);

		assertEquals(UPDATED_PRODUCT_NAME, product.getProduct());
		assertEquals(UPDATED_PRODUCT_VALUE, product.getPricePerPiece());
		assertEquals(UPDATED_PRODUCT_WEIGHT, product.getWeightPerPiece());

		verify(productRepository).save(any(ProductEntity.class));

	}

	@Test
	public void updateProduct_whenProductDoesNotExist_throwsProductNotFoundException() {
		ProductRequest newValues = updatedProductRequest();
		AuthenticatedUser adminUser = authenticatedAdmin();

		when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ProductNotFoundException.class, () ->
				productService.updateProduct(adminUser,anyLong(), newValues)
		);
		verify(productRepository, never()).save(any(ProductEntity.class));

	}

	private AuthenticatedUser authenticatedAdmin(){
		return new AuthenticatedUser(99L, "Admin@gmail.com", Role.ADMIN);
	}

	private ProductRequest productRequest(){
		return new ProductRequest(PRODUCT_NAME,PRODUCT_VALUE,PRODUCT_WEIGHT);
	}

	private ProductRequest updatedProductRequest(){
		return new ProductRequest(UPDATED_PRODUCT_NAME,UPDATED_PRODUCT_VALUE,UPDATED_PRODUCT_WEIGHT);
	}

	private ProductEntity product(UserEntity user,ProductRequest productRequest){
		return new ProductEntity(user,productRequest.getProduct(), productRequest.getValue(), productRequest.getWeight());
	}

	private UserEntity user(AuthenticatedUser authenticatedUser){
		return userService.getUserByAuthenticatedUser(authenticatedUser);
	}


}
