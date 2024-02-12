package com.palgona.palgona.repository;

import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);
}
