package com.palgona.palgona.repository.product;

import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.repository.product.querydto.ProductDetailQueryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    @Query("""
        SELECT 
            p, m, 
            COALESCE((SELECT MAX(b.price) FROM Bidding b WHERE b.product = p), p.initialPrice) AS highestBid, 
            (SELECT COUNT(bm) FROM Bookmark bm WHERE bm.product = p) AS bookmarkCount 
        FROM Product p 
        JOIN FETCH p.member m 
        WHERE p.id = :productId
    """)
    Optional<ProductDetailQueryResponse> findProductWithAll(long productId);

}
