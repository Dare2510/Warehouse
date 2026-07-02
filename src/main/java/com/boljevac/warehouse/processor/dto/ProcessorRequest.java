package com.boljevac.warehouse.processor.dto;


import com.boljevac.warehouse.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcessorRequest {

	@NotNull(message = "OrderStatus is required")
	private OrderStatus orderStatus;


}
