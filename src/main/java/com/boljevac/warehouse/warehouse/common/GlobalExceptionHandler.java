package com.boljevac.warehouse.warehouse.common;

import com.boljevac.warehouse.warehouse.order.exception.*;
import com.boljevac.warehouse.warehouse.product.exception.EmptyProductRepositoryException;
import com.boljevac.warehouse.warehouse.product.exception.ProductDuplicateCreationException;
import com.boljevac.warehouse.warehouse.product.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	//Exception handling for validation
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getAllErrors().get(0).getDefaultMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(EmptyProductRepositoryException.class)
	public ResponseEntity<ErrorResponse> handleEmptyProductRepositoryException(EmptyProductRepositoryException ex,
																			   HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex,
																		HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

	}

	@ExceptionHandler(ProductDuplicateCreationException.class)
	public ResponseEntity<ErrorResponse> handleProductDoubleCreationException(ProductDuplicateCreationException ex,
																			  HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex,
																	  HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	//Order quantity must be <= product stock
	@ExceptionHandler(OrderExceedsStockException.class)
	public ResponseEntity<ErrorResponse> handleOrderExceedsStockException(OrderExceedsStockException ex,
																		  HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_ACCEPTABLE.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(error);
	}

	//Sequence of status changes must be: ORDER_PLACED -> (CANCELLED)/PROCESSING -> PACKAGED -> SHIPPED
	@ExceptionHandler(StatusChangeInvalidOrderException.class)
	public ResponseEntity<ErrorResponse> handleInvalidStatusException(StatusChangeInvalidOrderException ex,
																	  HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	//To cancel an order it must have status Order placed
	@ExceptionHandler(OrderCancelNotPossibleException.class)
	public ResponseEntity<ErrorResponse> handleOrderCancelNotPossibleException(OrderCancelNotPossibleException ex,
																			   HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_ACCEPTABLE.value(),
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(error);
	}

	//If trying to set the status to a staus that is not available
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
																				   HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Order Status must be ORDER_PLACED|PACKAGED|PROCESSING|SHIPPED|CANCELLED",
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(EmptyOrderRepositoryException.class)
	public ResponseEntity<ErrorResponse> handleEmptyOrderRepositoryException(EmptyOrderRepositoryException ex,
																			 HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);


	}
}
