package com.palgona.palgona.repository;

import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product = :product AND pi.image.imageUrl = :imageUrl")
    Optional<ProductImage> findByProductAndImageUrl(Product product, String imageUrl);
}
