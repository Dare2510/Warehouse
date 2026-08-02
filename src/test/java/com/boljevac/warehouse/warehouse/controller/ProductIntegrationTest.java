package com.boljevac.warehouse.warehouse.controller;

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

	private static final String PRODUCT_NAME = "TestProduct";
	private static final String UPDATED_PRODUCT_NAME = "TestNewNameProduct";

	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("300");
	private static final BigDecimal UPDATED_PRODUCT_VALUE = new BigDecimal("30");
	private static final double PRODUCT_WEIGHT = 50;
	private static final double UPDATED_PRODUCT_WEIGHT = 30;
	private static final double INVALID_PRODUCT_WEIGHT = 0;
	private static final Long UNAVAILABLE_PRODUCT_ID = 999L;

	private static final String USER_MAIL = "clerk@mail.com";
	private static final String USER_USERNAME = "testClerk";
	private static final String USER_FIRST_NAME = "testClerkFirstName";
	private static final String USER_SURNAME = "testClerkSurname";
	private static final String PASSWORD = "password";

	@AfterEach
	public void tearDown() {

		productRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	public void getPageOfProducts_withPageableDefaults_returnIsOK() throws Exception {
		UserRequest userRequest = userRequest();
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
		UserRequest userRequest = userRequest();
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
		UserRequest userRequest = userRequest();
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
		UserRequest userRequest = userRequest();
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
	public void deleteProduct_whenProductIsFound_deletesProduct() throws Exception {
		UserRequest userRequest = userRequest();
		Long userId = registerUserAndGetId(userRequest);
		ProductRequest productRequest = productRequestHelper();
		Long productId = createProductAndGetId(productRequest,userId);

		mockMvc.perform(delete("/api/warehouse/products/delete/" + productId)
				 .with(clerkAuth(userId)))
				.andExpect(status().isNoContent());

	}

	@Test
	public void deleteProduct_whenProductIsNotFound_returns405() throws Exception {
		UserRequest userRequest = userRequest();
		Long userId = registerUserAndGetId(userRequest);

		mockMvc.perform(delete("/api/warehouse/products/delete/" + UNAVAILABLE_PRODUCT_ID)
					.with(clerkAuth(userId)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message")
					.value("Product with id " + UNAVAILABLE_PRODUCT_ID + " not found"));

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


}
