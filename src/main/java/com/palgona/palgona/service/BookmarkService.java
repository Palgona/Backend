package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.bookmark.Bookmark;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.repository.BookmarkRepository;
import com.palgona.palgona.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.palgona.palgona.common.error.code.BookmarkErrorCode.BOOKMARK_EXISTS;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void createBookmark(Long productId, CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1. 해당 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException());

        //2. 이미 추가된 찜인지 확인
        bookmarkRepository.findByMemberAndProduct(member, product)
                .ifPresent(b -> {
                    throw new BusinessException(BOOKMARK_EXISTS);
                });

        //3. 찜 추가
        Bookmark bookmark = Bookmark.builder()
                .member(member)
                .product(product)
                .build();

        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void deleteBookmark(Long productId, CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1. 해당 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException());

        //2. 해당 찜이 존재하는지 확인
        Bookmark bookmark = bookmarkRepository.findByMemberAndProduct(member, product)
                .orElseThrow(() -> new IllegalArgumentException());

        //3. 찜 삭제
        bookmarkRepository.delete(bookmark);
    }
}
