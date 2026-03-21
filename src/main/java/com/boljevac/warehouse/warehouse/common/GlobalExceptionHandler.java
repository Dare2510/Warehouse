package com.boljevac.warehouse.warehouse.common;

import com.boljevac.warehouse.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.warehouse.location.exceptions.LocationLoadLimitExceededException;
import com.boljevac.warehouse.warehouse.location.exceptions.LocationsAlreadyCreatedException;
import com.boljevac.warehouse.warehouse.location.exceptions.LocationsNotCreatedException;
import com.boljevac.warehouse.warehouse.location.exceptions.NoUnusedLocationException;
import com.boljevac.warehouse.warehouse.inventory.exceptions.NotSufficientStockToStoreException;
import com.boljevac.warehouse.warehouse.order.exception.*;
import com.boljevac.warehouse.warehouse.product.exception.EmptyProductRepositoryException;
import com.boljevac.warehouse.warehouse.product.exception.ProductDuplicateCreationException;
import com.boljevac.warehouse.warehouse.product.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	//Exception handling for validation
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException ex, HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getAllErrors().get(0).getDefaultMessage(),
				request.getRequestURI()
		);

		logger.error(error.toString());

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

		logger.error(error.toString());
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

		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

	}

	@ExceptionHandler(ProductDuplicateCreationException.class)
	public ResponseEntity<ErrorResponse> handleProductDoubleCreationException(ProductDuplicateCreationException ex,
																			  HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.CONFLICT.value(),
				ex.getMessage(),
				request.getRequestURI()
		);

		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex,
																	  HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(OrderExceedsStockException.class)
	public ResponseEntity<ErrorResponse> handleOrderExceedsStockException(OrderExceedsStockException ex,
																		  HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	//To cancel an order it must have status Order_Placed
	@ExceptionHandler(OrderCancelOrDeleteNotPossibleException.class)
	public ResponseEntity<ErrorResponse> handleOrderCancelNotPossibleException(OrderCancelOrDeleteNotPossibleException ex,
																			   HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	//If trying to set the status to a status that is not available
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
																				   HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Order Status must be ORDER_PLACED|PACKAGED|PROCESSING|SHIPPED|CANCELLED",
				request.getRequestURI()
		);
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(NotSufficientStockToStoreException.class)
	public ResponseEntity<ErrorResponse> handleNotSufficientStockToStoreException(NotSufficientStockToStoreException ex,
																		   HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(NoUnusedLocationException.class)
	public ResponseEntity<ErrorResponse>  handleNoUnusedLocationException(NoUnusedLocationException ex,
																		  HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(InventoryNotFoundException.class)
	public ResponseEntity<ErrorResponse>  handleInventoryNotFoundException(InventoryNotFoundException ex,
																		   HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	//Maximum 300 locations
	@ExceptionHandler(LocationsAlreadyCreatedException.class)
	public ResponseEntity<ErrorResponse> handleLocationsAlreadyCreatedException(LocationsAlreadyCreatedException ex,
																		   HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.CONFLICT.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}
	//Max weight per location = 1000
	@ExceptionHandler(LocationLoadLimitExceededException.class)
	public ResponseEntity<ErrorResponse> handleLocationLoadLimitExceededException(LocationLoadLimitExceededException ex,
																				  HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		logger.error(error.toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(LocationsNotCreatedException.class)
	public ResponseEntity<ErrorResponse> handleLocationsNotCreatedException(LocationsNotCreatedException ex,
																			HttpServletRequest request){
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI()
		);
		logger.error(error.toString());
		return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
}
