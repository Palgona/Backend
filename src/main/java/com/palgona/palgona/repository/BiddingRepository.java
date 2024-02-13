package com.palgona.palgona.repository;

import com.palgona.palgona.domain.bidding.Bidding;
import com.palgona.palgona.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiddingRepository extends JpaRepository<Bidding, Long> {

    Page<Bidding> findAllByProduct(Pageable pageable, Product product);
}
