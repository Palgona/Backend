package com.palgona.palgona.repository.product;

import com.palgona.palgona.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
}
