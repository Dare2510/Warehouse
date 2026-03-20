package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.warehouse.order.controller.OrderController;
import com.boljevac.warehouse.warehouse.order.service.OrderService;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.warehouse.order.dto.OrderResponse;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelOrDeleteNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.warehouse.processor.service.ProcessorService;
import com.boljevac.warehouse.warehouse.product.exception.EmptyProductRepositoryException;
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

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	JwtToken jwtToken;

	@MockitoBean
	JwtAuthFilter jwtAuthFilter;

	@MockitoBean
	UserDetailsService userDetailsService;

	@MockitoBean
	OrderService orderService;
	@MockitoBean
	ProcessorService processorService;

	@Test
	public void createOrder_whenRequestIsValid_returns201() throws Exception {
		when(orderService.createOrder(any(OrderRequest.class)))
				.thenReturn(new OrderResponse("Item",
						3, BigDecimal.valueOf(30),
						OrderStatus.ORDER_PLACED));

		mockMvc.perform(post("/api/warehouse/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"id":1,"quantity":3}
								"""))
				.andExpect(status().isCreated());

		verify(orderService).createOrder(any(OrderRequest.class));

	}

	@Test
	public void createOrder_whenRequestExceedsStock_returns406() throws Exception {
		when(orderService.createOrder(any(OrderRequest.class)))
				.thenThrow(new OrderExceedsStockException());

		mockMvc.perform(post("/api/warehouse/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"id":1,"quantity":3}
								"""))
				.andExpect(status().isNotAcceptable());

		verify(orderService).createOrder(any(OrderRequest.class));

	}

	@Test
	public void createOrder_whenRequestIsNotValid_returns400() throws Exception {
		mockMvc
				.perform(post("/api/warehouse/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"id": -1, "quantity":3}
								"""))
				.andExpect(status().isBadRequest());
		verify(orderService, never()).createOrder(any(OrderRequest.class));
	}

	@Test
	public void cancelOrder_whenRequestIsValid_returns200() throws Exception {
		when(orderService.cancelOrder(1L))
				.thenReturn(new OrderResponse("Item",
						3, BigDecimal.valueOf(30),
						OrderStatus.CANCELLED));


		mockMvc.perform(patch("/api/warehouse/orders/1/cancel")
		).andExpect(status().isOk());

		verify(orderService).cancelOrder(1L);

	}

	@Test
	public void cancelOrder_whenOrderStatusIsNotOrderPlaced_returns406() throws Exception {

		when(orderService.cancelOrder(1L))
				.thenThrow(new OrderCancelOrDeleteNotPossibleException(1L));

		mockMvc
				.perform(patch("/api/warehouse/orders/1/cancel"))
				.andExpect(status().isNotAcceptable());

		verify(orderService).cancelOrder(1L);
	}

	@Test
	public void getListOfProducts_whenProductRepositoryContainsProducts_returns200() throws Exception {
		when(orderService.getListOfProducts()).thenReturn(Collections.emptyList());

		mockMvc
				.perform(get("/api/warehouse/orders/products"))
				.andExpect(status().isOk());

		verify(orderService).getListOfProducts();
	}

	@Test
	public void getProducts_whenProductRepositoryIsEmpty_returns404() throws Exception {
		when(orderService.getListOfProducts()).thenThrow(EmptyProductRepositoryException.class);

		mockMvc
				.perform(get("/api/warehouse/orders/products"))
				.andExpect(status().isNotFound());

		verify(orderService).getListOfProducts();
	}
}



