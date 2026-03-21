package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.warehouse.inventory.controller.InventoryController;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.warehouse.inventory.service.InventoryService;
import com.boljevac.warehouse.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.warehouse.security.jwt.JwtAuthFilter;
import com.boljevac.warehouse.warehouse.security.jwt.JwtToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class InventoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	InventoryService inventoryService;
	@MockitoBean
	JwtToken jwtToken;
	@MockitoBean
	JwtAuthFilter jwtAuthFilter;
	@MockitoBean
	UserDetailsService userDetailsService;


	@Test
	void createStock_whenRequestIsValid_returns200() throws Exception {
		when(inventoryService.createStock(any(InventoryRequest.class))).thenReturn(
				new InventoryResponse(
						"TestProduct" , 50
				)
		);
		mockMvc.perform(post("/api/warehouse/inventory")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"productId" : 1, "quantity" : 5}
						"""))
				.andExpect(status().isOk());

		verify(inventoryService).createStock(any(InventoryRequest.class));
	}

	@Test
	void createStock_whenProductIsNotFound_returns404() throws Exception {
		doThrow(new ProductNotFoundException(1L)).when(inventoryService).createStock(any(InventoryRequest.class));

		mockMvc.perform(post("/api/warehouse/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
						{"productId" : 1, "quantity" : 5}
						"""))
				.andExpect(status().isNotFound());

		verify(inventoryService).createStock(any(InventoryRequest.class));
	}

	@Test
	void getInventoryResponse_whenRequestIsValid_returns200() throws Exception {
		when(inventoryService.getInventoryResponse(1L)).thenReturn(any(InventoryResponse.class));

		mockMvc.perform(get("/api/warehouse/inventory/1")).andExpect(status().isOk());

		verify(inventoryService).getInventoryResponse(1L);
	}

	@Test
	void getInventoryResponse_whenInventoryWasNotFound_returns404() throws Exception {
		doThrow(new InventoryNotFoundException(1L)).when(inventoryService).getInventoryResponse(1L);
		mockMvc.perform(get("/api/warehouse/inventory/1")).andExpect(status().isNotFound());

		verify(inventoryService).getInventoryResponse(1L);
	}
}
