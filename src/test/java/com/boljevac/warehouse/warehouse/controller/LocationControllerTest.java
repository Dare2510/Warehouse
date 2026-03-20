package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.warehouse.location.controller.LocationsController;
import com.boljevac.warehouse.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.warehouse.location.dto.LocationsResponse;
import com.boljevac.warehouse.warehouse.location.service.LocationService;
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

@WebMvcTest(LocationsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LocationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	LocationService locationService;
	@MockitoBean
	JwtToken jwtToken;
	@MockitoBean
	JwtAuthFilter jwtAuthFilter;
	@MockitoBean
	UserDetailsService userDetailsService;

	@Test
	public void storeInventory_whenRequestIsValid_returns200() throws Exception{
		LocationsRequest request = new LocationsRequest(1L,5);
		when(locationService.storeInventory(request)).thenReturn(
				new LocationsResponse(
						1L,
						"TestProduct",
						50.00,
						500.00,
						"Location"
				)
		);

		mockMvc.perform(post("/api/warehouse/locations")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"inventoryId": 1 , "quantity": 5}
						"""
				)
		).andExpect(status().isOk());

		verify(locationService).storeInventory(any(LocationsRequest.class));
	}
}
