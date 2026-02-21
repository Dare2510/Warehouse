package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.warehouse.order.OrderController;
import com.boljevac.warehouse.warehouse.order.OrderService;
import com.boljevac.warehouse.warehouse.order.OrderStatus;
import com.boljevac.warehouse.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.warehouse.order.dto.OrderResponse;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.warehouse.processor.ProcessorService;
import com.boljevac.warehouse.warehouse.processor.dto.ProcessorResponse;
import com.boljevac.warehouse.warehouse.product.ProductEntity;
import com.boljevac.warehouse.warehouse.product.ProductService;
import com.boljevac.warehouse.warehouse.security.utils.JwtAuthFilter;
import com.boljevac.warehouse.warehouse.security.utils.JwtUtil;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

	@Autowired MockMvc mockMvc;

	@MockitoBean JwtUtil  jwtUtil;

	@MockitoBean JwtAuthFilter  jwtAuthFilter;

	@MockitoBean UserDetailsService userDetailsService;

	@MockitoBean OrderService orderService;
	@MockitoBean ProcessorService processorService;

		//Test create Order -> Response created
	@Test
	public void createOrder_expecting_201() throws Exception {
			when(orderService.createOrder(any(OrderRequest.class)))
					.thenReturn(new OrderResponse("Item",
							3, BigDecimal.valueOf(30),
							OrderStatus.ORDER_PLACED));

			mockMvc.perform(post("/api/order")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{"id":1,"quantity":3}
									"""))
					.andExpect(status().isCreated());

		}
		//Test Order exceeding Stock -> Response is not acceptable
	@Test
	public void createOrder_expecting_406() throws Exception {
		when(orderService.createOrder(any(OrderRequest.class)))
				.thenThrow(new OrderExceedsStockException());

		mockMvc.perform(post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"id":1,"quantity":3}
						"""))
				.andExpect(status().isNotAcceptable());

	}
		//Test invalid validation create Order -> Response bad Request
	@Test
	public void createOrder_expecting_400() throws Exception{
		when(orderService.createOrder(any(OrderRequest.class)))
				.thenReturn(new OrderResponse(
						"Item",
						3, BigDecimal.valueOf(30),
						OrderStatus.ORDER_PLACED
				));
		mockMvc
				.perform(post("/api/order")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"id": -1, "quantity":3}
								"""))
				.andExpect(status().isBadRequest());
	}
		//Test to successfully cancel Order -> Response OK
	@Test
	public void cancelOrder_expecting_200() throws Exception {
		when(orderService.cancelOrder(1L))
				.thenReturn(new OrderResponse("Item",
						3, BigDecimal.valueOf(30),
						OrderStatus.CANCELLED));


		mockMvc.perform(patch("/api/order/1/cancel")
		).andExpect(status().isOk());


		}

		//Test to unsuccessfully cancel Order -> Response not acceptable
	@Test
	public void cancelOrder_expecting_406() throws Exception {

		when(orderService.cancelOrder(1L))
				.thenThrow(new OrderCancelNotPossibleException(1L));

		mockMvc
				.perform(patch("/api/order/1/cancel"))
				.andExpect(status().isNotAcceptable());
		}

	}

