package com.palgona.palgona.service.product;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.code.ProductErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.image.Image;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductImage;
import com.palgona.palgona.domain.product.ProductState;
import com.palgona.palgona.dto.ProductUpdateRequest;
import com.palgona.palgona.repository.BiddingRepository;
import com.palgona.palgona.repository.ImageRepository;
import com.palgona.palgona.repository.ProductImageRepository;
import com.palgona.palgona.repository.product.ProductRepository;
import com.palgona.palgona.service.ProductService;
import com.palgona.palgona.service.image.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUpdateTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private BiddingRepository biddingRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    void 성공_일반_유저_상품_수정(){
        //given
        //멤버
        Member owner = createMember();
        CustomMemberDetails memberDetails = new CustomMemberDetails(owner);

        //기존에 있던 상품
        Long productId = 1L;
        Product product = createProduct(owner);

        List<Image> images = Arrays.asList(
            Image.builder()
                  .imageUrl("image1")
                  .build(),
            Image.builder()
                .imageUrl("image2")
                .build()
        );

        //업데이트 요청 내용
        ProductUpdateRequest request = createDto();

        MockMultipartFile image3 = new MockMultipartFile(
                "image3",
                "product_image1.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        MockMultipartFile image4 = new MockMultipartFile(
                "image4",
                "product_image2.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );
        List<MultipartFile> update_images = Arrays.asList(image3, image4);

        // when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(imageRepository.findImageByImageUrls(request.deletedImageUrls())).thenReturn(images);
        productService.updateProduct(productId, request, update_images, memberDetails);

        // then
        //4-1. 삭제된 상품 이미지 처리
        verify(imageRepository, times(1)).findImageByImageUrls(request.deletedImageUrls());
        verify(productImageRepository, times(1)).deleteByImageIds(images);
        verify(imageRepository, times(1)).deleteByImageUrls(request.deletedImageUrls());
        verify(s3Service, times(request.deletedImageUrls().size())).deleteFile(any(String.class));

        //4-2. 새로 추가된 상품 이미지 저장
        verify(s3Service, times(update_images.size())).upload(any(MultipartFile.class));
        verify(imageRepository, times(update_images.size())).save(any(Image.class));
        verify(productImageRepository, times(update_images.size())).save(any(ProductImage.class));

        //5. 상품 정보 수정
        assertThat(product.getName()).isEqualTo(request.name());
        assertThat(product.getInitialPrice()).isEqualTo(request.initialPrice());
        assertThat(product.getContent()).isEqualTo(request.content());
        assertThat(product.getCategory()).isEqualTo(Category.valueOf(request.category()));
        assertThat(product.getDeadline()).isEqualTo(request.deadline());
    }


    @Test
    void 성공_관리자_상품_수정(){
        //given
        //멤버
        Member owner = createMember();
        Member admin = Member.of(1000, Status.ACTIVE, "3333", Role.ADMIN);
        CustomMemberDetails memberDetails = new CustomMemberDetails(admin);

        //기존에 있던 상품
        Long productId = 1L;
        Product product = createProduct(owner);

        List<Image> images = Arrays.asList(
                Image.builder()
                        .imageUrl("image1")
                        .build(),
                Image.builder()
                        .imageUrl("image2")
                        .build()
        );

        //업데이트 요청 내용
        ProductUpdateRequest request = createDto();

        MockMultipartFile image3 = new MockMultipartFile(
                "image3",
                "product_image1.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        MockMultipartFile image4 = new MockMultipartFile(
                "image4",
                "product_image2.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );
        List<MultipartFile> update_images = Arrays.asList(image3, image4);

        // when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(imageRepository.findImageByImageUrls(request.deletedImageUrls())).thenReturn(images);
        productService.updateProduct(productId, request, update_images, memberDetails);

        // then
        //4-1. 삭제된 상품 이미지 처리
        verify(imageRepository, times(1)).findImageByImageUrls(request.deletedImageUrls());
        verify(productImageRepository, times(1)).deleteByImageIds(images);
        verify(imageRepository, times(1)).deleteByImageUrls(request.deletedImageUrls());
        verify(s3Service, times(request.deletedImageUrls().size())).deleteFile(any(String.class));

        //4-2. 새로 추가된 상품 이미지 저장
        verify(s3Service, times(update_images.size())).upload(any(MultipartFile.class));
        verify(imageRepository, times(update_images.size())).save(any(Image.class));
        verify(productImageRepository, times(update_images.size())).save(any(ProductImage.class));

        //5. 상품 정보 수정
        assertThat(product.getName()).isEqualTo(request.name());
        assertThat(product.getInitialPrice()).isEqualTo(request.initialPrice());
        assertThat(product.getContent()).isEqualTo(request.content());
        assertThat(product.getCategory()).isEqualTo(Category.valueOf(request.category()));
        assertThat(product.getDeadline()).isEqualTo(request.deadline());
    }

    @Test
    void 실패_유효하지_않은_상품id() {
        // given
        Member member = createMember();
        CustomMemberDetails memberDetails = new CustomMemberDetails(member);

        // 상품
        Long productId = 1L;

        // 수정 정보
        ProductUpdateRequest request = createDto();
        List<MultipartFile> images = new ArrayList<>();

        // when
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // then
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(productId, request, images, memberDetails));
    }

    @Test
    void 실패_권한이_없는_유저(){
        //given
        // 멤버
        Member owner = createMember();
        Member member2 = Member.of(1000,Status.ACTIVE,"2222", Role.USER);
        CustomMemberDetails memberDetails = new CustomMemberDetails(member2); //다른 유저

        //상품
        Long productId = 1L;
        Product product = createProduct(owner);

        //수정 정보
        ProductUpdateRequest request = createDto();
        List<MultipartFile> images = new ArrayList<>();

        //when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.updateProduct(productId, request, images, memberDetails));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INSUFFICIENT_PERMISSION);
    }

    @Test
    void 실패_입찰_내역에_있는_상품(){
        //given
        //멤버
        Member owner = createMember();
        CustomMemberDetails memberDetails = new CustomMemberDetails(owner);

        //상품
        Long productId = 1L;
        Product product = createProduct(owner);

        //상품 수정 정보
        ProductUpdateRequest request = createDto();

        // 수정할 이미지
        List<MultipartFile> images = new ArrayList<>();

        //when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(biddingRepository.existsByProduct(product)).thenReturn(true);
        BusinessException exception = assertThrows(BusinessException.class,
                ()->productService.updateProduct(productId, request, images, memberDetails));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.RELATED_BIDDING_EXISTS);
    }

    @Test
    void 실패_구매_내역에_있는_상품(){

    }

    @Test
    void 실패_유효하지_않은_사진_경로(){

    }

    @Test
    void 실패_유효하지_않은_가격(){
        //given
        //멤버
        Member owner = createMember();
        CustomMemberDetails memberDetails = new CustomMemberDetails(owner);

        //기존에 있던 상품
        Long productId = 1L;
        Product product = createProduct(owner);

        //업데이트 요청 내용
        String name = "수정된 상품";
        Integer initialPrice = -1; //유효하지 않은 가격
        String content = "수정된 상품 설명입니다.";
        String category = Category.BOOK.getKey();
        LocalDateTime deadline = LocalDateTime.now().plusDays(6);
        List<String> deletedImageUrls = Arrays.asList("image1", "image2");

        ProductUpdateRequest request = new ProductUpdateRequest(
                name,
                initialPrice,
                content,
                category,
                deadline,
                deletedImageUrls
        );
        List<MultipartFile> update_images = new ArrayList<>();

        //when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.updateProduct(productId,request,update_images,memberDetails));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_PRICE);
    }

    @Test
    void 실패_유효하지_않은_카테고리(){
        //given
        //멤버
        Member owner = createMember();
        CustomMemberDetails memberDetails = new CustomMemberDetails(owner);

        //기존에 있던 상품
        Long productId = 1L;
        Product product = createProduct(owner);

        //업데이트 요청 내용
        String name = "수정된 상품";
        Integer initialPrice = 10000;
        String content = "수정된 상품 설명입니다.";
        String category = "유효하지 않은 카테고리";
        LocalDateTime deadline = LocalDateTime.now().plusDays(6);
        List<String> deletedImageUrls = Arrays.asList("image1", "image2");

        ProductUpdateRequest request = new ProductUpdateRequest(
                name,
                initialPrice,
                content,
                category,
                deadline,
                deletedImageUrls
        );
        List<MultipartFile> update_images = new ArrayList<>();

        //when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.updateProduct(productId,request,update_images,memberDetails));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_CATEGORY);
    }

    @Test
    void 실패_유효하지_않은_마감시간(){
        //given
        //멤버
        Member owner = createMember();
        CustomMemberDetails memberDetails = new CustomMemberDetails(owner);

        //기존에 있던 상품
        Long productId = 1L;
        Product product = createProduct(owner);

        //업데이트 요청 내용
        String name = "수정된 상품";
        Integer initialPrice = 10000;
        String content = "수정된 상품 설명입니다.";
        String category = Category.BOOK.getKey();
        LocalDateTime deadline = LocalDateTime.now();
        List<String> deletedImageUrls = Arrays.asList("image1", "image2");

        ProductUpdateRequest request = new ProductUpdateRequest(
                name,
                initialPrice,
                content,
                category,
                deadline,
                deletedImageUrls
        );
        List<MultipartFile> update_images = new ArrayList<>();

        //when
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.updateProduct(productId,request,update_images,memberDetails));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_DEADLINE);
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

    private ProductUpdateRequest createDto(){
        String name = "수정된 상품";
        Integer initialPrice = 10000;
        String content = "수정된 상품 설명입니다.";
        String category = Category.BOOK.getKey();
        LocalDateTime deadline = LocalDateTime.now().plusDays(6);
        List<String> deletedImageUrls = Arrays.asList("image1", "image2");

        ProductUpdateRequest request = new ProductUpdateRequest(
                name,
                initialPrice,
                content,
                category,
                deadline,
                deletedImageUrls
        );

        return request;
    }


}
