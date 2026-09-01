package com.boljevac.warehouse.warehouse.integration;

import com.boljevac.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.inventory.service.InventoryService;
import com.boljevac.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.location.service.LocationService;
import com.boljevac.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.order.service.OrderService;
import com.boljevac.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.product.service.ProductService;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.dto.UserRequest;
import com.boljevac.warehouse.user.dto.UserResponse;
import com.boljevac.warehouse.user.entity.UserEntity;
import com.boljevac.warehouse.user.repository.UserRepository;
import com.boljevac.warehouse.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@ActiveProfiles("postgres-test")
public class OrderConcurrencyIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres =
			new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	@Autowired
	private ProductService productService;

	@Autowired
	private InventoryService inventoryService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LocationsRepository locationsRepository;


	private static final String PRODUCT_NAME = "TestProduct";
	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("10");
	private static final double PRODUCT_WEIGHT = 100;

	private static final String EMAIL = "testuser@mail.com";
	private static final String PASSWORD = "password";
	private static final String USERNAME = "tester";
	private static final String NAME = "testName";
	private static final String SURNAME = "testSurname";

	@AfterEach
	public void afterEach() {
		orderRepository.deleteAll();
		inventoryRepository.deleteAll();
		locationsRepository.deleteAll();
		productRepository.deleteAll();
		userRepository.findByEmail(EMAIL)
				.ifPresent(user -> {
					userRepository.delete(user);
				});
	}


	@Test
	void concurrentOrders_shouldNotOversellInventory_throwsOrderExceedsStockException() throws Exception {

		//Get automated generated Admin User
		UserEntity adminUser = userService.getUserById(1L);
		AuthenticatedUser authenticatedAdminUser = createAuthenticatedUser(adminUser);

		Long productId = createProductAndReturnId(authenticatedAdminUser);
		ProductEntity product = getProduct(productId);

		createLocationsAndStock(productId, authenticatedAdminUser);

		UserEntity firstUser = createUserAndReturnEntity();
		AuthenticatedUser authenticatedUser = createAuthenticatedUser(firstUser);

		OrderRequest firstRequest =
				new OrderRequest(productId, 4);

		ExecutorService executor =
				Executors.newFixedThreadPool(2);

		CountDownLatch ready =
				new CountDownLatch(2);

		CountDownLatch start =
				new CountDownLatch(1);

		Callable<Boolean> task = () -> {

			ready.countDown();

			start.await();

			try {
				orderService.createOrder(authenticatedUser, firstRequest);
				return true;
			} catch (OrderExceedsStockException exception) {
				return false;
			}
		};

		try {

			Future<Boolean> a = executor.submit(task);
			Future<Boolean> b = executor.submit(task);

			ready.await();

			start.countDown();

			boolean aSucceeded = a.get();
			boolean bSucceeded = b.get();

			//Sum of successful orders
			long successCount =
					Stream.of(aSucceeded, bSucceeded)
							.filter(Boolean::booleanValue)
							.count();

			assertEquals(1, successCount);

			int remaining =
					inventoryRepository
							.findAllByProductEntity(product)
							.stream()
							.mapToInt(InventoryEntity::getQuantity)
							.sum();

			assertEquals(1, remaining);

			assertEquals(1, orderRepository.count());
		} finally {

			executor.shutdown();
		}


	}

	@Test
	void concurrentOrders_multipleOrderNotExceedingInventory() throws Exception {

		//Get automated generated Admin User
		UserEntity adminUser = userService.getUserById(1L);
		AuthenticatedUser authenticatedAdminUser = createAuthenticatedUser(adminUser);

		Long productId = createProductAndReturnId(authenticatedAdminUser);
		ProductEntity product = getProduct(productId);

		createLocationsAndStock(productId, authenticatedAdminUser);

		UserEntity user = createUserAndReturnEntity();
		AuthenticatedUser authenticatedUser = createAuthenticatedUser(user);

		OrderRequest orderRequest =
				new OrderRequest(productId, 1);

		ExecutorService executor =
				Executors.newFixedThreadPool(5);

		CountDownLatch ready =
				new CountDownLatch(5);

		CountDownLatch start =
				new CountDownLatch(1);

		Callable<Boolean> task = () -> {
			ready.countDown();

			start.await();

			try {
				orderService.createOrder(authenticatedUser, orderRequest);
				return true;
			} catch (OrderExceedsStockException exception) {
				return false;
			}
		};

		try {

			Future<Boolean> a = executor.submit(task);
			Future<Boolean> b = executor.submit(task);
			Future<Boolean> c = executor.submit(task);
			Future<Boolean> d = executor.submit(task);
			Future<Boolean> e = executor.submit(task);

			ready.await();
			start.countDown();

			boolean aSucceeded = a.get();
			boolean bSucceeded = b.get();
			boolean cSucceeded = c.get();
			boolean dSucceeded = d.get();
			boolean eSucceeded = e.get();

			//Sum of successful orders
			long successCount =
					Stream.of(aSucceeded, bSucceeded, cSucceeded, dSucceeded, eSucceeded)
							.filter(Boolean::booleanValue)
							.count();

			assertEquals(5, successCount);

			int remaining =
					inventoryRepository
							.findAllByProductEntity(product)
							.stream()
							.mapToInt(InventoryEntity::getQuantity)
							.sum();

			assertEquals(0, remaining);

			assertEquals(5, orderRepository.count());

		} finally {

			executor.shutdown();
		}



	}

	private AuthenticatedUser createAuthenticatedUser(UserEntity user) {

		return new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole());
	}

	private Long createProductAndReturnId(AuthenticatedUser authenticatedUser) {
		ProductResponse response = productService.createAndValidateNewProduct(authenticatedUser,
				new ProductRequest(PRODUCT_NAME, PRODUCT_VALUE, PRODUCT_WEIGHT));

		return response.getId();
	}

	private ProductEntity getProduct(Long productId) {
		return productService.getProductById(productId);
	}

	private void createLocationsAndStock(Long productId, AuthenticatedUser authenticatedUser) {
		locationService.createLocations();
		InventoryRequest inventoryRequest = new InventoryRequest(productId, 5);
		inventoryService.createStock(authenticatedUser, inventoryRequest);

	}

	private UserEntity createUserAndReturnEntity() {
		UserRequest userRequest = new UserRequest(EMAIL, PASSWORD, USERNAME, NAME, SURNAME);
		UserResponse userResponse = userService.registerUserByCustomer(userRequest);

		return userService.getUserById(userResponse.getUserId());

	}
}
