package com.boljevac.warehouse.warehouse.integration;

import com.boljevac.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.location.dto.LocationsRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LocationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private LocationsRepository locationsRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	private static final String USER_MAIL = "clerk@mail.com";
	private static final String USER_USERNAME = "testClerk";
	private static final String USER_FIRST_NAME = "testClerkFirstName";
	private static final String USER_SURNAME = "testClerkSurname";
	private static final String PASSWORD = "password";

	private static final String PRODUCT_NAME = "TestProduct";
	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("300");
	private static final double PRODUCT_WEIGHT = 50;

	private static final double EXCEEDING_PRODUCT_WEIGHT = 500;

	private static final Integer INVENTORY_QUANTITY = 10;

	private static final Integer QUANTITY_TO_STORE = 5;
	private static final Integer EXCEEDING_QUANTITY_TO_STORE = 20;

	@AfterEach
	public void tearDown() throws Exception{

		inventoryRepository.deleteAll();
		locationsRepository.deleteAll();
		productRepository.deleteAll();
		userRepository.deleteAll();
	}


	@Test
	public void createLocations_whenCreatingFirstTime_returns201() throws Exception {
		Long userId = registerUserAndGetId(userRequest());

		mockMvc.perform(post("/api/warehouse/locations/create")
						.with(clerkAuth(userId)))
				.andExpect(status().isCreated());
	}

	@Test
	public void createLocations_whenLocationsExists_returns409() throws Exception {
		Long userId = registerUserAndGetId(userRequest());

		createLocations(userId);

		mockMvc.perform(post("/api/warehouse/locations/create")
						.with(clerkAuth(userId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message")
						.value("Locations already exists, cannot create new location"));


	}

	@Test
	public void storeInventory_whenInventoryExists_returns200() throws Exception {
		Long userId = registerUserAndGetId(userRequest());
		Long productId = createProductAndGetId(productRequestHelper(),userId);
		createLocations(userId);
		Long inventoryId = createInventoryAndGetId(userId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);

		mockMvc.perform(post("/api/warehouse/locations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(toStore))
				.with(clerkAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.inventoryId").exists())
				.andExpect(jsonPath("$.product").value(PRODUCT_NAME))
				.andExpect(jsonPath("$.weightPerPiece").value(PRODUCT_WEIGHT))
				.andExpect(jsonPath("$.totalWeight").value(PRODUCT_WEIGHT*QUANTITY_TO_STORE))
				.andExpect(jsonPath("$.location").exists());

	}

	@Test
	public void storeInventory_whenQuantityIsNotAvailable_returns400() throws Exception {
		Long userId = registerUserAndGetId(userRequest());
		Long productId = createProductAndGetId(productRequestHelper(),userId);
		createLocations(userId);
		Long inventoryId = createInventoryAndGetId(userId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,EXCEEDING_QUANTITY_TO_STORE);

		mockMvc.perform(post("/api/warehouse/locations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(toStore))
						.with(clerkAuth(userId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Not enough available Stock to store " + EXCEEDING_QUANTITY_TO_STORE + " pieces"));

	}

	@Test
	public void storeInventory_whenWeightExceedsLocation_returns400() throws Exception {
		Long userId = registerUserAndGetId(userRequest());
		Long productId = createProductAndGetId(productWithExceedingWeightRequestHelper(),userId);
		createLocations(userId);
		Long inventoryId = createInventoryAndGetId(userId,inventoryRequestHelper(productId));
		LocationsRequest toStore = new LocationsRequest(inventoryId,QUANTITY_TO_STORE);

		mockMvc.perform(post("/api/warehouse/locations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(toStore))
						.with(clerkAuth(userId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("The weight to store exceeds the load limit of the Location"));

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

	//Endpoint Helpers

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

	//Request Helpers

	private ProductRequest productRequestHelper() {
		return new ProductRequest(PRODUCT_NAME, PRODUCT_VALUE, PRODUCT_WEIGHT);
	}

	private ProductRequest productWithExceedingWeightRequestHelper() {
		return new ProductRequest(PRODUCT_NAME, PRODUCT_VALUE, EXCEEDING_PRODUCT_WEIGHT);
	}

	private UserRequest userRequest() {
		return new UserRequest(USER_MAIL, PASSWORD, USER_USERNAME, USER_FIRST_NAME, USER_SURNAME);
	}

	private InventoryRequest inventoryRequestHelper(Long productId) {
		return new InventoryRequest(productId, INVENTORY_QUANTITY);
	}


}
