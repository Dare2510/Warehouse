package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.warehouse.inventory.controller.InventoryController;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.warehouse.inventory.service.InventoryService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
	void add_stock_expecting_200() throws Exception {
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
}
