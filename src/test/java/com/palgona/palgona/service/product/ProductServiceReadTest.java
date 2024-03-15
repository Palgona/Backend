package com.palgona.palgona.service.product;

import com.palgona.palgona.common.error.code.ProductErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.ProductState;
import com.palgona.palgona.dto.ProductDetailResponse;
import com.palgona.palgona.repository.ProductImageRepository;
import com.palgona.palgona.repository.product.ProductRepository;
import com.palgona.palgona.repository.product.querydto.ProductDetailQueryResponse;
import com.palgona.palgona.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceReadTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    void 상품_상세_조회_성공() {
        // given
        // 상품
        Long productId = 1L;
        String productName = "상품1";
        String content = "이것은 상품 설명 부분";
        String category = Category.BOOK.getKey();
        String productState = ProductState.ON_SALE.getKey();
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);
        LocalDateTime create_at = LocalDateTime.now();
        Long ownerId = 1L;
        String ownerName = "상품 주인";
        String ownerImgUrl = "/profile";
        Integer highestPrice = 10000;
        Integer bookmarkCount = 3;

        ProductDetailQueryResponse response = new ProductDetailQueryResponse(
                productId,
                productName,
                content,
                category,
                productState,
                deadline,
                create_at,
                ownerId,
                ownerName,
                ownerImgUrl,
                highestPrice,
                bookmarkCount
        );

        List<String> imageUrls = Arrays.asList("image1", "image2");

        // when
        when(productRepository.findProductWithAll(productId)).thenReturn(Optional.of(response));
        when(productImageRepository.findProductImageUrlsByProduct(productId)).thenReturn(imageUrls);
        ProductDetailResponse result = productService.readProduct(productId);

        // then
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.productName()).isEqualTo(productName);
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.category()).isEqualTo(category);
        assertThat(result.productState()).isEqualTo(productState);
        assertThat(result.deadline()).isEqualTo(deadline);
        assertThat(result.created_at()).isEqualTo(create_at);
        assertThat(result.ownerId()).isEqualTo(ownerId);
        assertThat(result.ownerName()).isEqualTo(ownerName);
        assertThat(result.ownerImgUrl()).isEqualTo(ownerImgUrl);
        assertThat(result.highestPrice()).isEqualTo(highestPrice);
        assertThat(result.bookmarkCount()).isEqualTo(bookmarkCount);
        assertThat(result.imageUrls()).isEqualTo(imageUrls);
    }

    @Test
    void 실패_유효하지_않은_상품id() {
        // given
        Long productId = 2L;

        // when
        when(productRepository.findProductWithAll(productId)).thenReturn(Optional.empty());

        // then
        assertThrows(IllegalArgumentException.class, () -> productService.readProduct(productId));
    }

    @Test
    void 실패_삭제된_상품(){
        //given
        Long productId = 1L;
        String productName = "상품1";
        String content = "이것은 상품 설명 부분";
        String category = Category.BOOK.getKey();
        String productState = ProductState.DELETED.getKey(); //삭제된 상품
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);
        LocalDateTime create_at = LocalDateTime.now();
        Long ownerId = 1L;
        String ownerName = "상품 주인";
        String ownerImgUrl = "/profile";
        Integer highestPrice = 10000;
        Integer bookmarkCount = 3;

        ProductDetailQueryResponse response = new ProductDetailQueryResponse(
                productId,
                productName,
                content,
                category,
                productState,
                deadline,
                create_at,
                ownerId,
                ownerName,
                ownerImgUrl,
                highestPrice,
                bookmarkCount
        );

        // when
        when(productRepository.findProductWithAll(productId)).thenReturn(Optional.of(response));
        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.readProduct(productId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.DELETED_PRODUCT);
    }

}
