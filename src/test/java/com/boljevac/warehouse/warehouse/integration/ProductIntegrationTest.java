package com.boljevac.warehouse.warehouse.integration;

import com.boljevac.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.product.dto.ProductRequest;
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

public class ProductIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private LocationsRepository locationsRepository;

	private static final String PRODUCT_NAME = "TestProduct";
	private static final String UPDATED_PRODUCT_NAME = "TestNewNameProduct";

	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("300");
	private static final BigDecimal UPDATED_PRODUCT_VALUE = new BigDecimal("30");
	private static final double PRODUCT_WEIGHT = 50;
	private static final double UPDATED_PRODUCT_WEIGHT = 30;
	private static final double INVALID_PRODUCT_WEIGHT = 0;
	private static final Long UNAVAILABLE_PRODUCT_ID = 999L;

	private static final Integer INVENTORY_QUANTITY = 10;
	private static final Integer QUANTITY_TO_STORE = 5;
	private static final int VALID_ORDER_QUANTITY = 3;

	private static final String USER_MAIL = "user@mail.com";
	private static final String USER_USERNAME = "testUser";
	private static final String USER_FIRST_NAME = "testUserFirstName";
	private static final String USER_SURNAME = "testUserSurname";
	private static final String PASSWORD = "password";

	private static final String CLERK_MAIL = "clerk@mail.com";
	private static final String CLERK_USERNAME = "testClerk";
	private static final String CLERK_FIRST_NAME = "testClerkFirstName";
	private static final String CLERK_SURNAME = "testClerkSurname";

	@AfterEach
	public void afterEach() {
		orderRepository.deleteAll();
		inventoryRepository.deleteAll();
		locationsRepository.deleteAll();
		productRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	public void getPageOfProducts_withPageableDefaults_returnIsOK() throws Exception {
		UserRequest userRequest = clerkRequest();
		Long userId = registerUserAndGetId(userRequest);

		mockMvc.perform(get("/api/warehouse/products")
						.with(clerkAuth(userId))
						.param("page", "0")
						.param("size", "10")
						.param("sort", "product")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(10));
	}

	@Test
	public void createProduct_whenRequestIsValid_returnCreated() throws Exception {
		UserRequest userRequest = clerkRequest();
		Long userId = registerUserAndGetId(userRequest);
		ProductRequest productRequest = productRequestHelper();

		mockMvc.perform(post("/api/warehouse/products/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productRequest))
						.with(clerkAuth(userId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(PRODUCT_NAME))
				.andExpect(jsonPath("$.price").value(PRODUCT_VALUE))
				.andExpect(jsonPath("$.weight").value(PRODUCT_WEIGHT))
				.andExpect(jsonPath("$.userId").value(userId));

	}

	@Test
	public void createProduct_whenRequestProductWeightIsNotValid_returnsBadRequest() throws Exception {
		UserRequest userRequest = clerkRequest();
		Long userId = registerUserAndGetId(userRequest);
		ProductRequest productRequest = productRequestWithInvalidWeight();

		mockMvc.perform(post("/api/warehouse/products/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productRequest))
						.with(clerkAuth(userId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("weight must be >0"));

	}

	@Test
	public void createProduct_whenProductNameAlreadyExists_returns409() throws Exception {
		UserRequest userRequest = clerkRequest();
		Long userId = registerUserAndGetId(userRequest);
		ProductRequest productRequest = productRequestHelper();
		createProduct(productRequest, userId);

		mockMvc.perform(post("/api/warehouse/products/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productRequest))
						.with(clerkAuth(userId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message")
						.value("Cannot create product with duplicate Name: " + PRODUCT_NAME));

	}

	@Test
	public void deleteProduct_whenProductIsFoundAndNoOrdersExist_deletesProduct() throws Exception {
		UserRequest userRequest = clerkRequest();
		Long userId = registerUserAndGetId(userRequest);
		ProductRequest productRequest = productRequestHelper();
		Long productId = createProductAndGetId(productRequest,userId);

		mockMvc.perform(delete("/api/warehouse/products/delete/" + productId)
				 .with(clerkAuth(userId)))
				.andExpect(status().isNoContent());

	}

	@Test
	public void deleteProduct_whenProductIsNotFound_returns405() throws Exception {
		UserRequest userRequest = clerkRequest();
		Long userId = registerUserAndGetId(userRequest);

		mockMvc.perform(delete("/api/warehouse/products/delete/" + UNAVAILABLE_PRODUCT_ID)
					.with(clerkAuth(userId)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message")
					.value("Product with id " + UNAVAILABLE_PRODUCT_ID + " not found"));

	}

	@Test
	public void deleteProduct_whenOrderExists_returns400() throws Exception {
		Long clerkId = registerUserAndGetId(clerkRequest());
		Long productId = createProductAndGetId(productRequestHelper(),clerkId);
		createLocations(clerkId);
		Long inventoryId = createInventoryAndGetId(clerkId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);
		storeInventoryToLocation(toStore,clerkId);

		Long userId = registerUserAndGetId(userRequest());
		createOrder(userId,orderRequestHelper(productId,VALID_ORDER_QUANTITY));

		mockMvc.perform(delete("/api/warehouse/products/delete/" + productId)
						.with(clerkAuth(clerkId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Cannot delete product with id " +productId + " order exist"));


	}

	@Test
	public void updateProduct_whenRequestIsValid_returns200() throws Exception {
		UserRequest userRequest = userRequest();
		Long userId = registerUserAndGetId(userRequest);
		ProductRequest productRequest = productRequestHelper();
		Long productId = createProductAndGetId(productRequest,userId);
		ProductRequest updatedProductRequest = updatedProductRequestHelper();

		mockMvc.perform(put("/api/warehouse/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedProductRequest))
				.with(clerkAuth(userId)))
				.andExpect(status().isOk());

	}


	//Clerk Authenticator

	private RequestPostProcessor clerkAuth(Long clerkId) {
		AuthenticatedUser principal = new AuthenticatedUser(
				clerkId,
				"clerk@example.com",
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

	//Endpoint Helper

	private Long createProductAndGetId(ProductRequest productRequest, Long userId) throws Exception {

		String productResponseJson = mockMvc.perform(post("/api/warehouse/products/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productRequest))
						.with(clerkAuth(userId)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(productResponseJson, "$.id")).longValue();
	}

	private void createProduct(ProductRequest productRequest, Long userId) throws Exception {

		mockMvc.perform(post("/api/warehouse/products/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productRequest))
						.with(clerkAuth(userId)))
				.andExpect(status().isCreated());
	}

	private Long registerUserAndGetId(UserRequest userRequest) throws Exception {
		String userJson = mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(userJson, "$.userId")).longValue();
	}

	private void createLocations(Long userId) throws Exception {
		mockMvc.perform(post("/api/warehouse/locations/create")
						.with(clerkAuth(userId)))
				.andExpect(status().isCreated());
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

	private void storeInventoryToLocation(LocationsRequest toStore, Long userId) throws Exception {
		mockMvc.perform(post("/api/warehouse/locations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(toStore))
						.with(clerkAuth(userId)))
				.andExpect(status().isOk());
	}

	private void createOrder(Long userId, OrderRequest orderRequest) throws Exception {
		mockMvc.perform(post("/api/warehouse/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderRequest))
						.with(userAuth(userId)))
				.andExpect(status().isCreated());
	}

	//Request Helper

	private ProductRequest productRequestHelper(){
		return new ProductRequest(PRODUCT_NAME,PRODUCT_VALUE,PRODUCT_WEIGHT);
	}

	private ProductRequest updatedProductRequestHelper(){
		return new ProductRequest(UPDATED_PRODUCT_NAME,UPDATED_PRODUCT_VALUE,UPDATED_PRODUCT_WEIGHT);
	}

	private ProductRequest productRequestWithInvalidWeight(){
		return new ProductRequest(PRODUCT_NAME,PRODUCT_VALUE,INVALID_PRODUCT_WEIGHT);
	}

	private UserRequest userRequest() {
		return new UserRequest(USER_MAIL, PASSWORD, USER_USERNAME, USER_FIRST_NAME, USER_SURNAME);
	}

	private UserRequest clerkRequest() {
		return new UserRequest(CLERK_MAIL, PASSWORD, CLERK_USERNAME, CLERK_FIRST_NAME, CLERK_SURNAME);
	}

	private InventoryRequest inventoryRequestHelper(Long productId) {
		return new InventoryRequest(productId, INVENTORY_QUANTITY);
	}

	private OrderRequest orderRequestHelper(Long productId, Integer quantity) {
		return new OrderRequest(productId, quantity);
	}


}
