package com.boljevac.warehouse.warehouse.integration;

import com.boljevac.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.order.repository.ShippedOrdersRepository;
import com.boljevac.warehouse.processor.dto.ProcessorRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ProcessorIntegrationTest {


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
	private LocationsRepository locationsRepository;

	@Autowired
	private ShippedOrdersRepository shippedOrdersRepository;

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


	private static final String PASSWORD = "password";

	private static final String PRODUCT_NAME = "TestProduct";
	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("300");
	private static final double PRODUCT_WEIGHT = 50;

	private static final double EXCEEDING_PRODUCT_WEIGHT = 500;

	private static final Integer INVENTORY_QUANTITY = 10;

	private static final Integer QUANTITY_TO_STORE = 5;

	private static final int VALID_ORDER_QUANTITY = 3;

	private static final OrderStatus VALID_PROCESSOR_REQUEST = OrderStatus.ORDER_PLACED;
	private static final OrderStatus NOT_VALID_PROCESSOR_REQUEST = OrderStatus.PACKAGED;

	@AfterEach
	public void afterEach() {
		shippedOrdersRepository.deleteAll();
		orderRepository.deleteAll();
		inventoryRepository.deleteAll();
		locationsRepository.deleteAll();
		productRepository.deleteAll();
		userRepository.deleteAll();
	}


	@Test
	public void getListOfOrdersByStatus_whenOrdersAreFound_returns200() throws Exception {
		Long clerkId = registerUserAndGetId(clerkRequest());
		Long productId = createProductAndGetId(productRequestHelper(),clerkId);
		createLocations(clerkId);
		Long inventoryId = createInventoryAndGetId(clerkId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);
		storeInventoryToLocation(toStore,clerkId);

		Long userId = registerUserAndGetId(userRequest());

		OrderRequest validOrder = orderRequestHelper(productId,VALID_ORDER_QUANTITY);
		createOrder(userId,validOrder);

		ProcessorRequest orderPlaced = new ProcessorRequest(VALID_PROCESSOR_REQUEST);

		mockMvc
				.perform(post("/api/warehouse/processing")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderPlaced))
						.with(clerkAuth(clerkId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0]").isMap())
				.andExpect(jsonPath("$.[0].productId").value(productId))
				.andExpect(jsonPath("$.[0].product").value(PRODUCT_NAME))
				.andExpect(jsonPath("$.[0].quantity").value(VALID_ORDER_QUANTITY))
				.andExpect(jsonPath("$.[0].orderStatus").value(VALID_PROCESSOR_REQUEST.toString()));

	}

	@Test
	public void getListOfOrdersByStatus_whenOrdersAreNotFound_returns404() throws Exception {
			Long clerkId = registerUserAndGetId(clerkRequest());
			Long productId = createProductAndGetId(productRequestHelper(),clerkId);
			createLocations(clerkId);
			Long inventoryId = createInventoryAndGetId(clerkId,inventoryRequestHelper(productId));
			LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);
			storeInventoryToLocation(toStore,clerkId);

			Long userId = registerUserAndGetId(userRequest());

			OrderRequest validOrder = orderRequestHelper(productId,VALID_ORDER_QUANTITY);
			createOrder(userId,validOrder);

			ProcessorRequest notValidRequest = new ProcessorRequest(NOT_VALID_PROCESSOR_REQUEST);

			mockMvc
					.perform(post("/api/warehouse/processing")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(notValidRequest))
							.with(clerkAuth(clerkId)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message")
							.value("Order/s not found"));



	}
	@Test
	public void changeStatusOfOrder_whenRequestedStatusIsNotValidNextStatus_returns400() throws Exception {

		Long clerkId = registerUserAndGetId(clerkRequest());
		Long productId = createProductAndGetId(productRequestHelper(),clerkId);
		createLocations(clerkId);
		Long inventoryId = createInventoryAndGetId(clerkId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);
		storeInventoryToLocation(toStore,clerkId);

		Long userId = registerUserAndGetId(userRequest());

		OrderRequest validOrder = orderRequestHelper(productId,VALID_ORDER_QUANTITY);
		Long orderId = createOrderAndGetId(userId,validOrder);

		mockMvc.perform(put("/api/warehouse/processing/statusChange/"+orderId+"/"+NOT_VALID_PROCESSOR_REQUEST)
						.with(clerkAuth(clerkId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Status change order: " +
								"ORDER_PLACED -> (CANCELLED)/PROCESSING -> PACKAGED -> SHIPPED"));
	}

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
		mockMvc.perform(put("/api/warehouse/locations")
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


	private void createOrder(Long userId, OrderRequest orderRequest) throws Exception {
		mockMvc.perform(post("/api/warehouse/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderRequest))
						.with(userAuth(userId)))
				.andExpect(status().isCreated());
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

	private ProductRequest productWithExceedingWeightRequestHelper() {
		return new ProductRequest(PRODUCT_NAME, PRODUCT_VALUE, EXCEEDING_PRODUCT_WEIGHT);
	}

	private UserRequest clerkRequest() {
		return new UserRequest(CLERK_MAIL, PASSWORD, CLERK_USERNAME, CLERK_FIRST_NAME, CLERK_SURNAME);
	}

	private UserRequest userRequest() {
		return new UserRequest(USER_MAIL, PASSWORD, USER_USERNAME, USER_FIRST_NAME, USER_SURNAME);
	}



	private InventoryRequest inventoryRequestHelper(Long productId) {
		return new InventoryRequest(productId, INVENTORY_QUANTITY);
	}

	private OrderRequest orderRequestHelper(Long productId, Integer quantity) {
		return new OrderRequest(productId, quantity);
	}


}