package com.palgona.palgona.service.product;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.code.ProductErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.bookmark.Bookmark;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductState;
import com.palgona.palgona.repository.BiddingRepository;
import com.palgona.palgona.repository.BookmarkRepository;
import com.palgona.palgona.repository.product.ProductRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceDeleteTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private BiddingRepository biddingRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    void 성공_일반_유저_상품_삭제() {
        // given
        // 멤버
        Member owner = createMember();
        Member member2 = Member.of(1000, Status.ACTIVE, "2222", Role.USER);
        Member member3 = Member.of(1000, Status.ACTIVE, "3333", Role.USER);
        CustomMemberDetails memberDetails = new CustomMemberDetails(owner);

        // 상품
        Product product = createProduct(owner);

        // 북마크
        List<Bookmark> bookmarks = Arrays.asList(
                createBookmark(product, member2),
                createBookmark(product, member3)
        );

        // when
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        doNothing().when(bookmarkRepository).deleteByProduct(product);
        productService.deleteProduct(product.getId(), memberDetails);

        // then
        verify(bookmarkRepository, times(1)).deleteByProduct(product);
        assertThat(product.getProductState()).isEqualTo(ProductState.DELETED);
    }

    @Test
    void 성공_관리자_상품_삭제() {
        // given
        // 멤버
        Member owner = createMember();
        Member member2 = Member.of(1000, Status.ACTIVE, "2222", Role.USER);
        Member member3 = Member.of(1000, Status.ACTIVE, "3333", Role.USER);
        Member admin = Member.of(1000, Status.ACTIVE, "3333", Role.ADMIN);
        CustomMemberDetails memberDetails = new CustomMemberDetails(admin); //관리자

        // 상품
        Product product = createProduct(owner);

        // when
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        doNothing().when(bookmarkRepository).deleteByProduct(product);
        productService.deleteProduct(product.getId(), memberDetails);

        // then
        verify(bookmarkRepository, times(1)).deleteByProduct(product);
        assertThat(product.getProductState()).isEqualTo(ProductState.DELETED);
    }

    @Test
    void 실패_유효하지_않은_상품id(){
        // 멤버
        Member member = createMember();
        CustomMemberDetails memberDetails = new CustomMemberDetails(member);

        // 상품
        Long productId = 2L;

        // when
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // then
        assertThrows(IllegalArgumentException.class, () -> productService.deleteProduct(productId, memberDetails));
    }

    @Test
    void 실패_권한이_없는_유저(){
        // given
        // 멤버
        Member owner = createMember();
        Member member2 = Member.of(1000, Status.ACTIVE, "2222", Role.USER);
        CustomMemberDetails memberDetails = new CustomMemberDetails(member2); //권한이 없는 유저

        // 상품
        Product product = createProduct(owner);

        // when
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        BusinessException exception = assertThrows(BusinessException.class, () -> productService.deleteProduct(product.getId(), memberDetails));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INSUFFICIENT_PERMISSION);
    }

    @Test
    void 실패_입찰_내역에_있는_상품(){
        // given
        // 멤버
        Member owner = createMember();
        CustomMemberDetails memberDetails = new CustomMemberDetails(owner);

        // 상품
        Product product = createProduct(owner);

        // when
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(biddingRepository.existsByProduct(product)).thenReturn(true);
        BusinessException exception = assertThrows(BusinessException.class, () -> productService.deleteProduct(product.getId(), memberDetails));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.RELATED_BIDDING_EXISTS);
    }

    @Test
    void 실패_구매_내역에_있는_상품(){

    }


    private Member createMember(){
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);

        return member;
    }

    private Product createProduct(Member member){
        String name = "상품1";
        Integer initialPrice = 10000;
        String content = "이것은 상품 설명 부분";
        Category category = Category.BOOK;
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);
        ProductState productState = ProductState.ON_SALE;

        Product product = Product.builder()
                .name(name)
                .initialPrice(initialPrice)
                .content(content)
                .category(category)
                .productState(productState)
                .deadline(deadline)
                .member(member)
                .build();

        return product;
    }

    private Bookmark createBookmark(Product product, Member member){
        Bookmark bookmark = Bookmark.builder()
                .product(product)
                .member(member)
                .build();
        return bookmark;
    }


}
