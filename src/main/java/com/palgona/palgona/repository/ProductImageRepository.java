package com.palgona.palgona.repository;

import com.palgona.palgona.domain.image.Image;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    @Query("""
        select i.imageUrl
        from ProductImage pi
        left join pi.image i
        where pi.product.id = :productId
    """)
    List<String> findProductImageUrlsByProduct(Long productId);

    @Query("""
        delete from ProductImage pi
        where pi.image in :images
    """)
    void deleteByImageIds(List<Image> images);
}
