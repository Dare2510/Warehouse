package com.boljevac.warehouse.warehouse.security;

import com.boljevac.warehouse.warehouse.order.service.OrderService;
import com.boljevac.warehouse.warehouse.product.dto.ProductResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.jwt.secret=dGhpcy1pcy1qdXN0LWEtdGVzdC1zZWNyZXQtbG9uZy1lbm91Z2g=")
@AutoConfigureMockMvc
public class JwtFlowTest {

	@Autowired
	MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean OrderService orderService;

	//Helper for Login and get Token
	public String loginAndGetToken(String username, String password) throws Exception {
		String userJson = """
						{ "username": "%s", "password": "%s" }
						""".formatted(username, password);
		System.out.println("JWT_SECRET=" + System.getenv("JWT_SECRET"));
		String responseJson = mockMvc
				.perform(post("/api/warehouse/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(userJson))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode responseJsonNode = objectMapper.readTree(responseJson);
		System.out.println("JWT_SECRET=" + System.getenv("JWT_SECRET"));
		return responseJsonNode.get("token").asText();
	}

	@Test
	void getOrders_withoutLogin_expecting401() throws Exception {
		mockMvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
	}

	@Test
	void getOrders_withClerkLogin_expecting403() throws Exception {
		when(orderService.getProducts())
				.thenReturn(List.of(new ProductResponse(
								1L,
								"TestProduct",
								BigDecimal.valueOf(500),
								10

				)));
		String token = loginAndGetToken("clerk", "ClerkPassword");
		mockMvc.perform(get("/api/orders/products")
				.header("Authorization","Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void getOrders_withClerkLogin_expecting200() throws Exception {
		when(orderService.getProducts()).thenReturn(
				List.of(new ProductResponse(
						1L,
						"TestProduct",
						BigDecimal.valueOf(500),
						10
				)));

		String token = loginAndGetToken("user", "UserPassword");
		mockMvc.perform(get("/api/orders/products")
						.header("Authorization","Bearer " + token))
				.andExpect(status().isOk());
	}
}
