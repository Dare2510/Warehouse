package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.location.repository.LocationsRepository;
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
public class InventoryIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private LocationsRepository locationsRepository;

	private static final String PRODUCT_NAME = "TestProduct";
	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("300");
	private static final double PRODUCT_WEIGHT = 50;
	private static final Long UNAVAILABLE_PRODUCT_ID = 999L;

	private static final String USER_MAIL = "clerk@mail.com";
	private static final String USER_USERNAME = "testClerk";
	private static final String USER_FIRST_NAME = "testClerkFirstName";
	private static final String USER_SURNAME = "testClerkSurname";
	private static final String PASSWORD = "password";

	private static final Integer INVENTORY_QUANTITY = 10;
	private static final Long UNAVAILABLE_INVENTORY_ID = 999L;

	@AfterEach
	public void tearDown() throws Exception {
		inventoryRepository.deleteAll();
		locationsRepository.deleteAll();
		productRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void createStock_whenRequestIsValid_returns200() throws Exception {
		Long userId = registerUserAndGetId(userRequest());
		Long productId = createProductAndGetId(productRequestHelper(), userId);
		createLocations(userId);
		InventoryRequest request = inventoryRequestHelper(productId);

		mockMvc.perform(post("/api/warehouse/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
						.with(clerkAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantity").value(INVENTORY_QUANTITY))
				.andExpect(jsonPath("$.product").value(PRODUCT_NAME));
	}

	@Test
	void createStock_whenLocationsNotExist_returns400() throws Exception {
		Long userId = registerUserAndGetId(userRequest());
		Long productId = createProductAndGetId(productRequestHelper(), userId);
		InventoryRequest request = inventoryRequestHelper(productId);

		mockMvc.perform(post("/api/warehouse/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
						.with(clerkAuth(userId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Locations are not created, first create them"));
	}

	@Test
	void createStock_whenProductWasNotFound_returns404() throws Exception {
		Long userId = registerUserAndGetId(userRequest());
		createLocations(userId);
		InventoryRequest request = inventoryRequestHelper(UNAVAILABLE_PRODUCT_ID);

		mockMvc.perform(post("/api/warehouse/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
						.with(clerkAuth(userId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Product with id " + UNAVAILABLE_PRODUCT_ID + " not found"));
	}

	@Test
	void getStock_whenInventoryExists_returns200() throws Exception {
		Long userId = registerUserAndGetId(userRequest());
		Long productId = createProductAndGetId(productRequestHelper(), userId);
		createLocations(userId);
		Long inventoryId = createInventoryAndGetId(userId, inventoryRequestHelper(productId));

		mockMvc.perform(get("/api/warehouse/inventory/" + inventoryId)
						.with(clerkAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.inventoryId").value(inventoryId))
				.andExpect(jsonPath("$.quantity").value(INVENTORY_QUANTITY))
				.andExpect(jsonPath("$.product").value(PRODUCT_NAME));
	}

	@Test
	void getStock_whenInventoryWasNotFound_returns204() throws Exception {
		Long userId = registerUserAndGetId(userRequest());
		createLocations(userId);

		mockMvc.perform(get("/api/warehouse/inventory/" + UNAVAILABLE_INVENTORY_ID)
						.with(clerkAuth(userId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("Inventory with id " + UNAVAILABLE_INVENTORY_ID + " not found"));
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

	//
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

	private void createLocations(Long userId) throws Exception {
		mockMvc.perform(put("/api/warehouse/locations")
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


	private ProductRequest productRequestHelper() {
		return new ProductRequest(PRODUCT_NAME, PRODUCT_VALUE, PRODUCT_WEIGHT);
	}


	private UserRequest userRequest() {
		return new UserRequest(USER_MAIL, PASSWORD, USER_USERNAME, USER_FIRST_NAME, USER_SURNAME);
	}

	private InventoryRequest inventoryRequestHelper(Long productId) {
		return new InventoryRequest(productId, INVENTORY_QUANTITY);
	}
}
