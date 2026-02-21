package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.warehouse.product.ProductController;
import com.boljevac.warehouse.warehouse.product.ProductService;
import com.boljevac.warehouse.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.warehouse.security.utils.JwtAuthFilter;
import com.boljevac.warehouse.warehouse.security.utils.JwtUtil;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)

//Product Controller Tests

public class ProductControllerTest {

	@Autowired MockMvc mockMvc;

	//Mocked JWT
	@MockitoBean ProductService  productService;
	@MockitoBean JwtUtil  jwtUtil;
	@MockitoBean JwtAuthFilter  jwtAuthFilter;
	@MockitoBean UserDetailsService  userDetailsService;

	//Get all Products -> Response OK
	@Test
	void getProducts_expecting_200() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk());

	}
	//Create Product -> Response isCreated.
	@Test
	void createProduct_expecting_201() throws Exception {
		when(productService.createItem(any(ProductRequest.class)))
				.thenReturn(new ProductResponse(
						1L,
						"TestProduct",
						BigDecimal.valueOf(500),
						100));

		mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
						{ "product" : "TestProduct", "value" : 500, "quantity" : 100 }
						""")).
						andExpect(status().isCreated());
}
//Validation Test , invalid Json -> Response Bad Request
@Test
void createProduct_expecting_400() throws Exception {
		when(productService.createItem(any(ProductRequest.class)))
				.thenReturn(new  ProductResponse(
						1L,
						"TestProduct",
						BigDecimal.valueOf(500),
						100

				));
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
				{ "productName" : "TestProduct", "value" : 500, "quantity" : 100 }
				"""))
				.andExpect(status().isBadRequest());
	}



}
