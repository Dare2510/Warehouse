package com.boljevac.warehouse.warehouse.product.controller;

import com.boljevac.warehouse.warehouse.product.service.ProductService;
import com.boljevac.warehouse.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.warehouse.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService service;

	public ProductController(ProductService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ProductResponse> createItem(@RequestBody@Valid ProductRequest productRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.createItem(productRequest));
	}
	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
		service.deleteItem(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<Page<ProductResponse>> getItems(Pageable pageable) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getAll(pageable));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> updateItem(@PathVariable Long id,
										   @RequestBody @Valid ProductRequest productRequest) {
		service.updateProduct(id, productRequest);
		return ResponseEntity.noContent().build();
	}
}
