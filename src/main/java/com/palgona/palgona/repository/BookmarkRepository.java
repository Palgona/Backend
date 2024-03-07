package com.palgona.palgona.repository;

import com.palgona.palgona.domain.bookmark.Bookmark;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByMemberAndProduct(Member member, Product product);

    List<Bookmark> findByProduct(Product product);

    long countByProduct(Product product);

}
