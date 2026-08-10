package com.boljevac.warehouse.warehouse.integration;

import com.boljevac.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.dto.UserRequest;
import com.boljevac.warehouse.user.entity.Role;
import com.boljevac.warehouse.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OrderIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private LocationsRepository  locationsRepository;

	@Autowired
	private UserRepository userRepository;

	private static final String CLERK_MAIL = "clerk@mail.com";
	private static final String CLERK_USERNAME = "testClerk";
	private static final String CLERK_FIRST_NAME = "testClerkFirstName";
	private static final String CLERK_SURNAME = "testClerkSurname";

	private static final String USER_MAIL = "user@mail.com";
	private static final String USER_USERNAME = "testUser";
	private static final String USER_FIRST_NAME = "testUserFirstName";
	private static final String USER_SURNAME = "testUserSurname";

	private static final String SECOND_USER_MAIL = "secondUser@mail.com";


	private static final String PASSWORD = "password";

	private static final String PRODUCT_NAME = "TestProduct";
	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("300");
	private static final double PRODUCT_WEIGHT = 50;

	private static final Integer INVENTORY_QUANTITY = 10;

	private static final Integer QUANTITY_TO_STORE = 5;

	private static final int VALID_ORDER_QUANTITY = 3;
	private static final int EXCEEDING_ORDER_QUANTITY = 20;
	private static final Long INVALID_PRODUCT_ID_FOR_ORDER = 0L;

	@AfterEach
	public void afterEach() {
		orderRepository.deleteAll();
		inventoryRepository.deleteAll();
		locationsRepository.deleteAll();
		productRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	public void createOrder_whenRequestIsValid_returns201() throws Exception {
		Long clerkId = registerUserAndGetId(clerkRequest());
		Long productId = createProductAndGetId(productRequestHelper(),clerkId);
		createLocations(clerkId);
		Long inventoryId = createInventoryAndGetId(clerkId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);
		storeInventoryToLocation(toStore,clerkId);

		Long userId = registerUserAndGetId(userRequest(USER_MAIL));

		OrderRequest validOrder = orderRequestHelper(productId,VALID_ORDER_QUANTITY);
		mockMvc.perform(post("/api/warehouse/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validOrder))
				.with(userAuth(userId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.orderId").exists())
				.andExpect(jsonPath("$.product").value(PRODUCT_NAME))
				.andExpect(jsonPath("$.quantity").value(VALID_ORDER_QUANTITY))
				.andExpect(jsonPath("$.totalPrice").value(BigDecimal.valueOf(VALID_ORDER_QUANTITY).multiply(PRODUCT_VALUE).doubleValue()))
				.andExpect(jsonPath("$.orderStatus").value(OrderStatus.ORDER_PLACED.toString()));
	}

	@Test
	public void createOrder_whenRequestIsExceedingStock_returns400() throws Exception {
		Long clerkId = registerUserAndGetId(clerkRequest());
		Long productId = createProductAndGetId(productRequestHelper(),clerkId);
		createLocations(clerkId);
		Long inventoryId = createInventoryAndGetId(clerkId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);
		storeInventoryToLocation(toStore,clerkId);

		Long userId = registerUserAndGetId(userRequest(USER_MAIL));

		OrderRequest exceedingOrder = orderRequestHelper(productId,EXCEEDING_ORDER_QUANTITY);
		mockMvc.perform(post("/api/warehouse/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(exceedingOrder))
						.with(userAuth(userId)))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.message")
						.value("Order exceeds stock, -> Order not possible."));
	}

	@Test
	public void createOrder_whenRequestIsNotValid_returns400() throws Exception {
		Long clerkId = registerUserAndGetId(clerkRequest());
		Long productId = createProductAndGetId(productRequestHelper(),clerkId);
		createLocations(clerkId);
		Long inventoryId = createInventoryAndGetId(clerkId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);
		storeInventoryToLocation(toStore,clerkId);

		Long userId = registerUserAndGetId(userRequest(USER_MAIL));

		OrderRequest invalidOrder = orderRequestHelper(INVALID_PRODUCT_ID_FOR_ORDER,VALID_ORDER_QUANTITY);

		mockMvc.perform(post("/api/warehouse/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidOrder))
						.with(userAuth(userId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("ProductId must be positive"));
	}

	@Test
	public void cancelOrder_whenRequestIsValid_returns200() throws Exception {
		Long clerkId = registerUserAndGetId(clerkRequest());
		Long productId = createProductAndGetId(productRequestHelper(),clerkId);
		createLocations(clerkId);
		Long inventoryId = createInventoryAndGetId(clerkId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);
		storeInventoryToLocation(toStore,clerkId);

		Long userId = registerUserAndGetId(userRequest(USER_MAIL));
		Long orderId = createOrderAndGetId(userId,orderRequestHelper(productId,VALID_ORDER_QUANTITY));
		mockMvc.perform(patch("/api/warehouse/orders/"+orderId+"/cancel")
						.with(userAuth(userId)))
						.andExpect(status().isOk());
	}

	@Test
	public void cancelOrder_whenOwnershipValidationFailed_returns200() throws Exception {
		Long clerkId = registerUserAndGetId(clerkRequest());
		Long productId = createProductAndGetId(productRequestHelper(),clerkId);
		createLocations(clerkId);
		Long inventoryId = createInventoryAndGetId(clerkId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);
		storeInventoryToLocation(toStore,clerkId);

		Long userId = registerUserAndGetId(userRequest(USER_MAIL));
		Long orderId = createOrderAndGetId(userId,orderRequestHelper(productId,VALID_ORDER_QUANTITY));
		Long failingUserId = registerUserAndGetId(userRequest(SECOND_USER_MAIL));

		mockMvc.perform(patch("/api/warehouse/orders/"+orderId+"/cancel")
						.with(userAuth(failingUserId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("You don't own order with id " + orderId));
	}
//
//	@Test
//	public void cancelOrder_whenOrderStatusIsNotOrderPlaced_returns400() throws Exception {
//		doThrow(new OrderCancelOrDeleteNotPossibleException(1L)).when(orderService).cancelOrder(1L);
//		mockMvc
//				.perform(patch("/api/warehouse/orders/1/cancel"))
//				.andExpect(status().isBadRequest());
//
//		verify(orderService).cancelOrder(1L);
//	}
//

	//Clerk Authenticator

	private RequestPostProcessor clerkAuth(Long clerkId) {
		AuthenticatedUser principal = new AuthenticatedUser(
				clerkId,
				CLERK_MAIL,
				Role.CLERK
		);

		UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(
						principal,
						null,
						List.of(new SimpleGrantedAuthority("ROLE_CLERK"))
				);

		return authentication(auth);
	}

	//User Authenticator

	private RequestPostProcessor userAuth(Long userId) {
		AuthenticatedUser principal = new AuthenticatedUser(
				userId,
				USER_MAIL,
				Role.USER
		);

		UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(
						principal,
						null,
						List.of(new SimpleGrantedAuthority("ROLE_USER"))
				);

		return authentication(auth);
	}

	//Endpoint Helpers

	private void createLocations(Long userId) throws Exception {
		mockMvc.perform(post("/api/warehouse/locations/create")
						.with(clerkAuth(userId)))
				.andExpect(status().isCreated());
	}

	private void storeInventoryToLocation(LocationsRequest toStore, Long userId) throws Exception {
		mockMvc.perform(post("/api/warehouse/locations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(toStore))
						.with(clerkAuth(userId)))
				.andExpect(status().isOk());
	}

	private Long createInventoryAndGetId(Long userId, InventoryRequest request) throws Exception {
		String inventoryResponseJson = mockMvc.perform(post("/api/warehouse/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
						.with(clerkAuth(userId)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(inventoryResponseJson, "$.inventoryId")).longValue();
	}

	private Long createProductAndGetId(ProductRequest productRequest, Long userId) throws Exception {

		String productResponseJson = mockMvc.perform(post("/api/warehouse/products/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productRequest))
						.with(clerkAuth(userId)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(productResponseJson, "$.id")).longValue();
	}

	private Long registerUserAndGetId(UserRequest userRequest) throws Exception {
		String userJson = mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(userJson, "$.userId")).longValue();
	}

	private Long createOrderAndGetId(Long userId, OrderRequest orderRequest) throws Exception {
		String orderJson = mockMvc.perform(post("/api/warehouse/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderRequest))
						.with(userAuth(userId)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(orderJson, "$.orderId")).longValue();
	}

	//Request Helpers

	private ProductRequest productRequestHelper() {
		return new ProductRequest(PRODUCT_NAME, PRODUCT_VALUE, PRODUCT_WEIGHT);
	}

	private UserRequest clerkRequest() {
		return new UserRequest(CLERK_MAIL, PASSWORD, CLERK_USERNAME, CLERK_FIRST_NAME, CLERK_SURNAME);
	}

	private UserRequest userRequest(String userMail) {
		return new UserRequest(userMail, PASSWORD, USER_USERNAME, USER_FIRST_NAME, USER_SURNAME);
	}

	private InventoryRequest inventoryRequestHelper(Long productId) {
		return new InventoryRequest(productId, INVENTORY_QUANTITY);
	}

	private OrderRequest orderRequestHelper(Long productId, Integer quantity) {
		return new OrderRequest(productId, quantity);
	}
}



