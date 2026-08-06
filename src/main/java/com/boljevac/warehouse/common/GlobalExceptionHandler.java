package com.boljevac.warehouse.common;

import com.boljevac.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.inventory.exceptions.NotSufficientStockToStoreException;
import com.boljevac.warehouse.location.exceptions.LocationLoadLimitExceededException;
import com.boljevac.warehouse.location.exceptions.LocationsAlreadyCreatedException;
import com.boljevac.warehouse.location.exceptions.LocationsNotCreatedException;
import com.boljevac.warehouse.location.exceptions.NoUnusedLocationException;
import com.boljevac.warehouse.order.exception.OrderCancelOrDeleteNotPossibleException;
import com.boljevac.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.product.exception.EmptyProductRepositoryException;
import com.boljevac.warehouse.product.exception.ProductDuplicateCreationException;
import com.boljevac.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.user.exception.UserDoubleCreationException;
import com.boljevac.warehouse.user.exception.UserIncorrectCredentialsException;
import com.boljevac.warehouse.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
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

		log.error(error.toString());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(EmptyProductRepositoryException.class)
	public ResponseEntity<ErrorResponse> handleEmptyProductRepositoryException(EmptyProductRepositoryException ex,
	                                                                           HttpServletRequest request) {

		return errorResponseBuilder(ex, request, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex,
	                                                                    HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.NOT_FOUND);

	}

	@ExceptionHandler(ProductDuplicateCreationException.class)
	public ResponseEntity<ErrorResponse> handleProductDoubleCreationException(ProductDuplicateCreationException ex,
	                                                                          HttpServletRequest request) {

		return errorResponseBuilder(ex, request, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex,
	                                                                  HttpServletRequest request) {

		return errorResponseBuilder(ex, request, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(OrderExceedsStockException.class)
	public ResponseEntity<ErrorResponse> handleOrderExceedsStockException(OrderExceedsStockException ex,
	                                                                      HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	//Sequence of status changes must be: ORDER_PLACED -> (CANCELLED)/PROCESSING -> PACKAGED -> SHIPPED
	@ExceptionHandler(StatusChangeInvalidOrderException.class)
	public ResponseEntity<ErrorResponse> handleInvalidStatusException(StatusChangeInvalidOrderException ex,
	                                                                  HttpServletRequest request) {

		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	//To cancel an order it must have status Order_Placed
	@ExceptionHandler(OrderCancelOrDeleteNotPossibleException.class)
	public ResponseEntity<ErrorResponse> handleOrderCancelNotPossibleException(OrderCancelOrDeleteNotPossibleException ex,
	                                                                           HttpServletRequest request) {

		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	//If trying to set the status to a status that is not available
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
	                                                                               HttpServletRequest request) {

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Order Status must be ORDER_PLACED|PACKAGED|PROCESSING|SHIPPED|CANCELLED",
				request.getRequestURI());

		log.error(error.toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(NotSufficientStockToStoreException.class)
	public ResponseEntity<ErrorResponse> handleNotSufficientStockToStoreException(NotSufficientStockToStoreException ex,
	                                                                              HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NoUnusedLocationException.class)
	public ResponseEntity<ErrorResponse> handleNoUnusedLocationException(NoUnusedLocationException ex,
	                                                                     HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(InventoryNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleInventoryNotFoundException(InventoryNotFoundException ex,
	                                                                      HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.NOT_FOUND);
	}

	//Maximum 300 locations
	@ExceptionHandler(LocationsAlreadyCreatedException.class)
	public ResponseEntity<ErrorResponse> handleLocationsAlreadyCreatedException(LocationsAlreadyCreatedException ex,
	                                                                            HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.CONFLICT);
	}

	//Max weight per location = 1000
	@ExceptionHandler(LocationLoadLimitExceededException.class)
	public ResponseEntity<ErrorResponse> handleLocationLoadLimitExceededException(LocationLoadLimitExceededException ex,
	                                                                              HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(LocationsNotCreatedException.class)
	public ResponseEntity<ErrorResponse> handleLocationsNotCreatedException(LocationsNotCreatedException ex,
	                                                                        HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadableException(HttpMessageNotReadableException ex,
	                                                                HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Request body is invalid or contains missing/incorrect field values",
				request.getRequestURI());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex,
	                                                                 HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(UserIncorrectCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleUserIncorrectCredentialsException(UserIncorrectCredentialsException ex,
	                                                                             HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UserDoubleCreationException.class)
	public ResponseEntity<ErrorResponse> handleUserDoubleCreationException(UserDoubleCreationException ex,
	                                                                       HttpServletRequest request) {

		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	//Helper Method
	private ResponseEntity<ErrorResponse> errorResponseBuilder(Exception ex, HttpServletRequest request, HttpStatus status) {
		ErrorResponse error = new ErrorResponse(
				status.value(),
				ex.getMessage(),
				request.getRequestURI());

		return new ResponseEntity<>(error, status);

	}
}
