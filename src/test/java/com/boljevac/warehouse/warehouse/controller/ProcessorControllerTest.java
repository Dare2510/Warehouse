package com.boljevac.warehouse.warehouse.controller;
;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;

import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.warehouse.processor.controller.ProcessorController;
import com.boljevac.warehouse.warehouse.processor.service.ProcessorService;
import com.boljevac.warehouse.warehouse.processor.dto.ProcessorRequest;
import com.boljevac.warehouse.warehouse.processor.dto.ProcessorResponse;
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

import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProcessorController.class)
@AutoConfigureMockMvc(addFilters = false)

public class ProcessorControllerTest {


	@MockitoBean ProcessorService processorService;

	@MockitoBean
	JwtToken jwtToken;

	@MockitoBean JwtAuthFilter  jwtAuthFilter;

	@MockitoBean UserDetailsService userDetailsService;
	@Autowired MockMvc mockMvc;


		//get open Orders -> Response isOK
	@Test
	public void get_open_orders_expecting_200() throws Exception {
		when(processorService.getOrders(any(ProcessorRequest.class)))
				.thenReturn(List.of(new ProcessorResponse(
						1L,
						"TestProduct",
						500,
						OrderStatus.ORDER_PLACED)));


		mockMvc
			.perform(post("/api/processor")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{ "Orderstatus" : "ORDER_PLACED" }
					"""))
					.andExpect(status().isOk());
	}
		//change Order status -> Response isOK
	@Test
	public void change_order_status_expecting_200() throws Exception {
		when(processorService.changeOrderStatus(1L, OrderStatus.PROCESSING))
				.thenReturn(new ProcessorResponse(
						1L,
						"TestProduct",
						50,
						OrderStatus.ORDER_PLACED
				));

		mockMvc.perform(
				put("/api/processor/changeStatus/1/PROCESSING"))
				.andExpect(status().isOk());
	}
		// changer Order status -> Response bad Request
	@Test
	public void change_order_status_expecting_400() throws Exception {
		when(processorService.changeOrderStatus(1L, OrderStatus.SHIPPED))
				.thenThrow(new StatusChangeInvalidOrderException());

		mockMvc.perform(
				put("/api/processor/changeStatus/1/SHIPPED"))
				.andExpect(status().isBadRequest());
	}
		//change Order status with not available status -> Response bad Request
	@Test
	public void change_order_withInvalidStatus_expecting_400() throws Exception {
		when(processorService.changeOrderStatus(1L, OrderStatus.ORDER_PLACED))
				.thenThrow(new StatusChangeInvalidOrderException());

		mockMvc
				.perform(put("/api/processor/changeStatus/1/PLANNED"))
				.andExpect(status().isBadRequest());

	}






}
