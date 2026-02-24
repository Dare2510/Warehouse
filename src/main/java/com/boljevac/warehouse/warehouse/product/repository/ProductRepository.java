package com.boljevac.warehouse.warehouse.product.repository;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>{

	@Override
	Page<ProductEntity> findAll(Pageable pageable);

	ProductEntity findByProduct(String productName);

}
