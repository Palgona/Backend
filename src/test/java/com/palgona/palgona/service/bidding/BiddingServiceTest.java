package com.palgona.palgona.service.bidding;

import com.palgona.palgona.common.error.code.BiddingErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.bidding.Bidding;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductState;
import com.palgona.palgona.dto.BiddingAttemptRequest;
import com.palgona.palgona.repository.BiddingRepository;
import com.palgona.palgona.repository.product.ProductRepository;
import com.palgona.palgona.service.BiddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class BiddingServiceTest {

    @Mock
    private BiddingRepository biddingRepository;

    @Mock
    private ProductRepository productRepository;

    private BiddingService biddingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        biddingService = new BiddingService(biddingRepository, productRepository);
    }

    @Test
    void 입찰_시도_성공한다() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);

        long productId = 1L;
        String productName = "상품";
        String productContent = "설명";
        int initialPrice = 1000;
        LocalDateTime deadline = LocalDateTime.now().plusHours(2);
        Category category = Category.BOOK;
        ProductState state = ProductState.ON_SALE;

        Product product = Product.builder().name(productName).content(productContent).initialPrice(initialPrice)
                .deadline(deadline).category(category).productState(state).member(member).build();

        int highestPrice = 1300;
        Bidding existingBidding = Bidding.builder().product(product).member(member).price(highestPrice).build();

        int attemptPrice = 1500;
        BiddingAttemptRequest request = new BiddingAttemptRequest(productId, attemptPrice);

        // when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(biddingRepository.findHighestPriceByProduct(product)).thenReturn(Optional.of(existingBidding.getPrice()));

        // then
        assertDoesNotThrow(() -> biddingService.attemptBidding(member, request));
    }

    @Test
    void 이미_지난_상품에_입찰_실패한다() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);

        long productId = 1L;
        String productName = "상품";
        String productContent = "설명";
        int initialPrice = 1000;
        LocalDateTime deadline = LocalDateTime.now().minusHours(2); // 이미 마감된 상태
        Category category = Category.BOOK;
        ProductState state = ProductState.ON_SALE;
        Product product = Product.builder().name(productName).content(productContent).initialPrice(initialPrice)
                .deadline(deadline).category(category).productState(state).member(member).build();

        int attemptPrice = 1500;
        BiddingAttemptRequest request = new BiddingAttemptRequest(productId, attemptPrice);

        // when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> biddingService.attemptBidding(member, request));
        assertEquals(BiddingErrorCode.BIDDING_EXPIRED_PRODUCT, exception.getErrorCode());
    }

    @Test
    void 더_낮은_가격_입찰_실패한다() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);

        long productId = 1L;
        String productName = "상품";
        String productContent = "설명";
        int initialPrice = 1000;
        LocalDateTime deadline = LocalDateTime.now().plusHours(2);
        Category category = Category.BOOK;
        ProductState state = ProductState.ON_SALE;
        Product product = Product.builder().name(productName).content(productContent).initialPrice(initialPrice)
                .deadline(deadline).category(category).productState(state).member(member).build();

        int highestPrice = 1500;
        Bidding existingBidding = Bidding.builder().product(product).member(member).price(highestPrice).build();

        int attemptPrice = 1300;
        BiddingAttemptRequest request = new BiddingAttemptRequest(productId, attemptPrice);

        // when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(biddingRepository.findHighestPriceByProduct(product)).thenReturn(Optional.of(existingBidding.getPrice()));

        // then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> biddingService.attemptBidding(member, request));
        assertEquals(BiddingErrorCode.BIDDING_LOWER_PRICE, exception.getErrorCode());
    }

    @Test
    void 최소_단위보다_작은_입찰_실패한다() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);

        long productId = 1L;
        String productName = "상품";
        String productContent = "설명";
        int initialPrice = 1000;
        LocalDateTime deadline = LocalDateTime.now().plusHours(2);
        Category category = Category.BOOK;
        ProductState state = ProductState.ON_SALE;
        Product product = Product.builder().name(productName).content(productContent).initialPrice(initialPrice)
                .deadline(deadline).category(category).productState(state).member(member).build();

        int highestPrice = 1500;
        Bidding existingBidding = Bidding.builder().product(product).member(member).price(highestPrice).build();

        int attemptPrice = 1550;
        BiddingAttemptRequest request = new BiddingAttemptRequest(productId, attemptPrice);

        // when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(biddingRepository.findHighestPriceByProduct(product)).thenReturn(Optional.of(existingBidding.getPrice()));

        // then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> biddingService.attemptBidding(member, request));
        assertEquals(BiddingErrorCode.BIDDING_INSUFFICIENT_BID, exception.getErrorCode());
    }
}
