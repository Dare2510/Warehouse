package com.boljevac.warehouse.product.controller;

import com.boljevac.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.product.service.ProductService;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse/products")
@PreAuthorize("hasAnyRole('ADMIN','CLERK')")
public class ProductController {

	private final ProductService service;

	public ProductController(ProductService service) {
		this.service = service;
	}

	@PostMapping("/create")
	public ResponseEntity<ProductResponse> createItem(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
													  @RequestBody @Valid ProductRequest productRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).
				body(service.createAndValidateNewProduct(authenticatedUser,productRequest));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
		service.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<Page<ProductResponse>> getItems(Pageable pageable) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getAllProducts(pageable));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> updateItem(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
										   @PathVariable Long id,
	                                       @RequestBody @Valid ProductRequest productRequest) {
		service.updateProduct(authenticatedUser,id, productRequest);
		return ResponseEntity.ok().build();
	}
}
