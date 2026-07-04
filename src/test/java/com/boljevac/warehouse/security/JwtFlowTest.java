package com.boljevac.warehouse.security;

import com.boljevac.warehouse.order.service.OrderService;
import com.boljevac.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.user.dto.UserRequest;
import com.boljevac.warehouse.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class JwtFlowTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	OrderService orderService;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	UserRepository userRepository;

	@AfterEach
	public void afterEach() {
		userRepository.deleteAll();
	}

	@Test
	void getOrders_withoutLogin_returns401() throws Exception {
		mockMvc.perform(get("/api/warehouse/orders")).andExpect(status().isUnauthorized());
	}

	@Test
	void getProducts_withUserLogin_returns403() throws Exception {
		when(orderService.getListOfProducts())
				.thenReturn(List.of(new ProductResponse(
						1L,
						"TestProduct",
						BigDecimal.valueOf(500),
						10

				)));
		postUserRegistration();
		String token = loginAndGetToken("testUser@mail.com", "testPassword");

		mockMvc.perform(get("/api/warehouse/products")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void getProducts_withAdminLogin_returns200() throws Exception {
		when(orderService.getListOfProducts()).thenReturn(
				List.of(new ProductResponse(
						1L,
						"TestProduct",
						BigDecimal.valueOf(500),
						10
				)));

		String token = loginAndGetToken("emailForTest@mail.com", "testPassword");
		mockMvc.perform(get("/api/warehouse/products")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void getProductsForOrders_withUserLogin_returns200() throws Exception {
		when(orderService.getListOfProducts())
				.thenReturn(List.of(new ProductResponse(
						1L,
						"TestProduct",
						BigDecimal.valueOf(500),
						10

				)));
		postUserRegistration();
		String token = loginAndGetToken("testUser@mail.com", "testPassword");

		mockMvc.perform(get("/api/warehouse/orders/products")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	//Helper Methods
	private void postUserRegistration() throws Exception {
		UserRequest newUser = new UserRequest("testUser@mail.com", "testPassword","tester",
				"testName", "testSurname");
		mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newUser)))
				.andExpect(status().isOk());
	}

	public String loginAndGetToken(String email, String password) throws Exception {
		String userJson = """
				{ "email": "%s",
				"password": "%s" }
				""".formatted(email, password);
		System.out.println("JWT_SECRET=" + System.getenv("JWT_SECRET"));
		String responseJson = mockMvc
				.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		System.out.println("JWT_SECRET=" + System.getenv("JWT_SECRET"));
		return responseJson;
	}

}
