package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.order.repository.ShippedOrdersRepository;
import com.boljevac.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.product.exception.DeletionProductFailed;
import com.boljevac.warehouse.product.exception.ProductDuplicateCreationException;
import com.boljevac.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.product.service.ProductService;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.entity.Role;
import com.boljevac.warehouse.user.entity.UserEntity;
import com.boljevac.warehouse.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private OrderRepository orderRepository;
	@Mock
	private ProductService productService;
	@Mock
	private ShippedOrdersRepository shippedOrdersRepository;
	@Mock
	private InventoryRepository inventoryRepository;
	@Mock
	private UserService userService;

	private static final String PRODUCT_NAME = "TestProduct";
	private static final String UPDATED_PRODUCT_NAME = "TestNewNameProduct";

	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("300");
	private static final BigDecimal UPDATED_PRODUCT_VALUE = new BigDecimal("30");
	private static final double PRODUCT_WEIGHT = 50;
	private static final double UPDATED_PRODUCT_WEIGHT = 30;

	@BeforeEach
	void setUp() {
		productService = new ProductService(userService, productRepository, orderRepository, shippedOrdersRepository, inventoryRepository);

	}

	@Test
	public void createAndValidateNewProduct_whenProductAlreadyExists_throwsProductAlreadyExistsException() {
		ProductRequest request = productRequestHelper();
		AuthenticatedUser adminUser = createAuthenticatedAdminHelper();

		when(productRepository.existsByProduct(request.getProduct())).thenReturn(true);

		assertThrows(ProductDuplicateCreationException.class, () -> {
			productService.createAndValidateNewProduct(adminUser, request);
		});

		verify(productRepository, never()).save(any());

	}

	@Test
	public void createAndValidateNewProduct_whenRequestIsValid_returnsProductResponse() {
		ProductRequest request = productRequestHelper();
		AuthenticatedUser adminUser = createAuthenticatedAdminHelper();
		UserEntity createdByUser = getUserByAuthenticatedUser(adminUser);
		ProductEntity productEntity = createProductHelper(createdByUser, request);
		UserEntity user = new UserEntity();

		when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
		when(userService.getUserByAuthenticatedUser(any())).thenReturn(user);
		ProductResponse response = productService.createAndValidateNewProduct(adminUser, request);

		verify(productRepository).save(any(ProductEntity.class));
		assertEquals(PRODUCT_NAME, response.getName());
		assertEquals(PRODUCT_VALUE, response.getPrice());
		assertEquals(PRODUCT_WEIGHT, response.getWeight());

	}

	@Test
	public void updateProduct_whenRequestIsValid_returnsProductResponse() {
		ProductRequest newValues = updatedProductRequestHelper();
		AuthenticatedUser adminUser = createAuthenticatedAdminHelper();
		UserEntity createdByUser = getUserByAuthenticatedUser(adminUser);
		ProductEntity product = createProductHelper(createdByUser, newValues);


		Long id = product.getId();
		when(productRepository.findById(id)).thenReturn(Optional.of(product));
		productService.updateProduct(adminUser, id, newValues);

		assertEquals(UPDATED_PRODUCT_NAME, product.getProduct());
		assertEquals(UPDATED_PRODUCT_VALUE, product.getPricePerPiece());
		assertEquals(UPDATED_PRODUCT_WEIGHT, product.getWeightPerPiece());

		verify(productRepository).save(any(ProductEntity.class));

	}

	@Test
	public void updateProduct_whenProductDoesNotExist_throwsProductNotFoundException() {
		ProductRequest newValues = updatedProductRequestHelper();
		AuthenticatedUser adminUser = createAuthenticatedAdminHelper();

		when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ProductNotFoundException.class, () ->
				productService.updateProduct(adminUser, anyLong(), newValues)
		);
		verify(productRepository, never()).save(any(ProductEntity.class));

	}

	@Test
	public void deleteProduct_whenNoOrdersExist_deletesProduct() {
		ProductRequest request = productRequestHelper();
		AuthenticatedUser adminUser = createAuthenticatedAdminHelper();
		UserEntity createdByUser = getUserByAuthenticatedUser(adminUser);
		ProductEntity productEntity = createProductHelper(createdByUser, request);

		Long id = productEntity.getId();

		when(productRepository.findById(id)).thenReturn(Optional.of(productEntity));
		when(orderRepository.existsByProductEntity(productEntity)).thenReturn(false);
		when(shippedOrdersRepository.existsByProductId(id)).thenReturn(false);
		when(inventoryRepository.existsByProductEntity(productEntity)).thenReturn(false);

		productService.deleteProduct(id);

		verify(productRepository).findById(id);
		verify(orderRepository, times(1)).existsByProductEntity(productEntity);
		verify(shippedOrdersRepository, times(1)).existsByProductId(id);
		verify(inventoryRepository, times(1)).existsByProductEntity(productEntity);
		verify(productRepository).delete(productEntity);

	}

	@Test
	public void deleteProduct_whenOrdersExist_throwsDeletionProductFailed() {
		ProductRequest request = productRequestHelper();
		AuthenticatedUser adminUser = createAuthenticatedAdminHelper();
		UserEntity createdByUser = getUserByAuthenticatedUser(adminUser);
		ProductEntity productEntity = createProductHelper(createdByUser, request);

		Long id = productEntity.getId();

		when(productRepository.findById(id)).thenReturn(Optional.of(productEntity));
		when(orderRepository.existsByProductEntity(productEntity)).thenReturn(true);
		when(inventoryRepository.existsByProductEntity(productEntity)).thenReturn(false);

		assertThatThrownBy(() -> productService.deleteProduct(id))
				.isInstanceOf(DeletionProductFailed.class)
				.hasMessage(("Cannot delete product with id " + id + " order or inventory exist"));

		verify(productRepository).findById(id);
		verify(productRepository, never()).delete(productEntity);
		verify(orderRepository, times(1)).existsByProductEntity(productEntity);
		verify(shippedOrdersRepository, never()).existsByProductId(id);
		verify(inventoryRepository, times(1)).existsByProductEntity(productEntity);

	}

	@Test
	public void deleteProduct_whenShippedOrdersExist_throwsDeletionProductFailed() {
		ProductRequest request = productRequestHelper();
		AuthenticatedUser adminUser = createAuthenticatedAdminHelper();
		UserEntity createdByUser = getUserByAuthenticatedUser(adminUser);
		ProductEntity productEntity = createProductHelper(createdByUser, request);

		Long id = productEntity.getId();

		when(productRepository.findById(id)).thenReturn(Optional.of(productEntity));
		when(orderRepository.existsByProductEntity(productEntity)).thenReturn(false);
		when(shippedOrdersRepository.existsByProductId(id)).thenReturn(true);
		when(inventoryRepository.existsByProductEntity(productEntity)).thenReturn(false);

		assertThatThrownBy(() -> productService.deleteProduct(id))
				.isInstanceOf(DeletionProductFailed.class)
				.hasMessage(("Cannot delete product with id " + id + " order or inventory exist"));

		verify(productRepository).findById(id);
		verify(productRepository, never()).delete(productEntity);
		verify(orderRepository, times(1)).existsByProductEntity(productEntity);
		verify(shippedOrdersRepository, times(1)).existsByProductId(id);
		verify(inventoryRepository, times(1)).existsByProductEntity(productEntity);

	}

	@Test
	public void deleteProduct_whenInventoryExist_throwsDeletionProductFailed() {
		ProductRequest request = productRequestHelper();
		AuthenticatedUser adminUser = createAuthenticatedAdminHelper();
		UserEntity createdByUser = getUserByAuthenticatedUser(adminUser);
		ProductEntity productEntity = createProductHelper(createdByUser, request);

		Long id = productEntity.getId();

		when(productRepository.findById(id)).thenReturn(Optional.of(productEntity));
		when(orderRepository.existsByProductEntity(productEntity)).thenReturn(false);
		when(shippedOrdersRepository.existsByProductId(id)).thenReturn(false);
		when(inventoryRepository.existsByProductEntity(productEntity)).thenReturn(true);

		assertThatThrownBy(() -> productService.deleteProduct(id))
				.isInstanceOf(DeletionProductFailed.class)
				.hasMessage(("Cannot delete product with id " + id + " order or inventory exist"));

		verify(productRepository).findById(id);
		verify(productRepository, never()).delete(productEntity);
		verify(orderRepository, times(1)).existsByProductEntity(productEntity);
		verify(shippedOrdersRepository, times(1)).existsByProductId(id);
		verify(inventoryRepository, times(1)).existsByProductEntity(productEntity);

	}

	private AuthenticatedUser createAuthenticatedAdminHelper() {
		return new AuthenticatedUser(99L, "Admin@gmail.com", Role.ADMIN);
	}

	private ProductRequest productRequestHelper() {
		return new ProductRequest(PRODUCT_NAME, PRODUCT_VALUE, PRODUCT_WEIGHT);
	}

	private ProductRequest updatedProductRequestHelper() {
		return new ProductRequest(UPDATED_PRODUCT_NAME, UPDATED_PRODUCT_VALUE, UPDATED_PRODUCT_WEIGHT);
	}

	private ProductEntity createProductHelper(UserEntity user, ProductRequest productRequest) {
		return new ProductEntity(user, productRequest.getProduct(), productRequest.getValue(), productRequest.getWeight());
	}

	private UserEntity getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser) {
		return userService.getUserByAuthenticatedUser(authenticatedUser);
	}


}
