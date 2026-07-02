package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.product.controller.ProductController;
import com.boljevac.warehouse.product.service.ProductService;
import com.boljevac.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.security.jwt.JwtUtil;
import com.boljevac.warehouse.security.jwt.JwTAuthenticationFilter;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)

public class ProductControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	ProductService productService;
	@MockitoBean
	JwtUtil jwtToken;
	@MockitoBean
	JwTAuthenticationFilter jwtAuthFilter;


	@Test
	void getAllProducts_returns200() throws Exception {

		when(productService.getAllProducts(any(Pageable.class))).thenReturn(Page.empty());

		mockMvc.perform(get("/api/warehouse/products"))
				.andExpect(status().isOk());

		verify(productService).getAllProducts(any(Pageable.class));

	}

	@Test
	void createAndValidateNewProduct_whenRequestIsValid_returns201() throws Exception {
		when(productService.createAndValidateNewProduct(any(ProductRequest.class)))
				.thenReturn(new ProductResponse(
						1L,
						"TestProduct",
						BigDecimal.valueOf(500),
						100));

		mockMvc.perform(post("/api/warehouse/products/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "product" : "TestProduct", "value" : 500, "weight" : 100 }
								""")).
				andExpect(status().isCreated());

		verify(productService).createAndValidateNewProduct(any(ProductRequest.class));
	}

	//Validation Test , invalid Json -> Response Bad Request
	@Test
	void createAndValidateNewProduct_whenRequestIsInvalid_returns400() throws Exception {
		when(productService.createAndValidateNewProduct(any(ProductRequest.class)))
				.thenReturn(new ProductResponse(
						1L,
						"TestProduct",
						BigDecimal.valueOf(500),
						100));

		mockMvc.perform(post("/api/warehouse/products/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "productName" : "TestProduct", "value" : 500, "weight" : 100 }
								"""))
				.andExpect(status().isBadRequest());

		verify(productService, never()).createAndValidateNewProduct(any(ProductRequest.class));
	}


}
