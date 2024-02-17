package com.palgona.palgona.repository;

import com.palgona.palgona.domain.bookmark.Bookmark;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("DELETE FROM Bookmark b WHERE b.member = :member AND b.product.id = :productId")
    void deleteByMemberAndProductId(Member member, Long productId);

}
