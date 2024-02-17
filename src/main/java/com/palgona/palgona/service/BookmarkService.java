package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.bookmark.Bookmark;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.repository.BookmarkRepository;
import com.palgona.palgona.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final ProductRepository productRepository;

    public void createBookmark(Long productId, CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException());

        Bookmark bookmark = Bookmark.builder()
                .member(member)
                .product(product)
                .build();

        bookmarkRepository.save(bookmark);
    }
}
