package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;

import com.boljevac.warehouse.warehouse.order.exception.OrderNotFoundException;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProcessorController.class)
@AutoConfigureMockMvc(addFilters = false)

public class ProcessorControllerTest {


	@MockitoBean
	ProcessorService processorService;

	@MockitoBean
	JwtToken jwtToken;

	@MockitoBean
	JwtAuthFilter jwtAuthFilter;

	@MockitoBean
	UserDetailsService userDetailsService;
	@Autowired
	MockMvc mockMvc;


	@Test
	public void get_open_orders_expecting_200() throws Exception {
		when(processorService.getListOfOrdersByStatus(any(ProcessorRequest.class)))
				.thenReturn(List.of(new ProcessorResponse(
						1L,
						"TestProduct",
						500,
						OrderStatus.ORDER_PLACED)));


		mockMvc
				.perform(post("/api/warehouse/processing")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
									{ "orderStatus" : "ORDER_PLACED" }
								"""))
				.andExpect(status().isOk());

		verify(processorService).getListOfOrdersByStatus(any(ProcessorRequest.class));
	}

	@Test
	public void get_open_orders_expecting_404() throws Exception {
		when(processorService.getListOfOrdersByStatus(any(ProcessorRequest.class)))
				.thenThrow(new OrderNotFoundException());

		mockMvc
				.perform(post("/api/warehouse/processing")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "orderStatus" : "ORDER_PLACED" }
								"""))
				.andExpect(status().isNotFound());

		verify(processorService).getListOfOrdersByStatus(any(ProcessorRequest.class));


	}

	@Test
	public void change_order_status_expecting_200() throws Exception {
		when(processorService.changeStatusOfOrder(1L, OrderStatus.PROCESSING))
				.thenReturn(new ProcessorResponse(
						1L,
						"TestProduct",
						50,
						OrderStatus.ORDER_PLACED
				));

		mockMvc.perform(
						put("/api/warehouse/processing/statusChange/1/PROCESSING"))
				.andExpect(status().isOk());

		verify(processorService).changeStatusOfOrder(1L, OrderStatus.PROCESSING);
	}

	@Test
	public void change_order_status_expecting_400() throws Exception {
		when(processorService.changeStatusOfOrder(1L, OrderStatus.SHIPPED))
				.thenThrow(new StatusChangeInvalidOrderException());

		mockMvc.perform(
						put("/api/warehouse/processing/statusChange/1/SHIPPED"))
				.andExpect(status().isBadRequest());

		verify(processorService).changeStatusOfOrder(1L, OrderStatus.SHIPPED);
	}

	//change Order status with not available status -> Response bad Request
	@Test
	public void change_order_withInvalidStatus_expecting_400() throws Exception {
		when(processorService.changeStatusOfOrder(1L, OrderStatus.ORDER_PLACED))
				.thenThrow(new StatusChangeInvalidOrderException());

		mockMvc
				.perform(put("/api/warehouse/processing/statusChange/1/PLANNED"))
				.andExpect(status().isBadRequest());

		verify(processorService, never()).changeStatusOfOrder(1L, OrderStatus.ORDER_PLACED);
	}


}