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
import com.palgona.palgona.dto.ProductCreateRequest;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@ExtendWith(MockitoExtension.class)
public class ProductServiceCreateTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private S3Service s3Service;
    @InjectMocks
    private ProductService productService;

    @Test
    void 상품_등록_성공() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);
        CustomMemberDetails memberDetails = new CustomMemberDetails(member);

        String name = "상품1";
        Integer initialPrice = 10000;
        String content = "이것은 상품 설명 부분";
        String category = Category.BOOK.getKey();
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);

        MockMultipartFile image1 = new MockMultipartFile(
                "image1",
                "product_image1.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "image2",
                "product_image2.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        List<MultipartFile> images = Arrays.asList(image1, image2);

        ProductCreateRequest request = new ProductCreateRequest(
                name,
                initialPrice,
                content,
                category,
                deadline
        );

        // when
        when(productRepository.save(any(Product.class))).thenReturn(null);
        when(imageRepository.save(any(Image.class))).thenReturn(null);
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(null);
        when(s3Service.upload(any(MultipartFile.class))).thenAnswer(invocation -> {
            MultipartFile file = invocation.getArgument(0);
            return "s3_url/" + file.getOriginalFilename();
        });

        productService.createProduct(request, images, memberDetails);

        // then
        verify(productRepository, times(1)).save(any(Product.class));
        verify(s3Service, times(2)).upload(any(MultipartFile.class));
        verify(imageRepository, times(2)).save(any(Image.class));
        verify(productImageRepository, times(2)).save(any(ProductImage.class));
    }

    @Test
    void 실패_유효하지_않은_가격() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);
        CustomMemberDetails memberDetails = new CustomMemberDetails(member);

        String name = "상품1";
        Integer initialPrice = -1; //금액이 마이너스인 경우
        String content = "이것은 상품 설명 부분";
        String category = Category.BOOK.getKey();
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);

        MockMultipartFile image1 = new MockMultipartFile(
                "image1",
                "product_image1.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "image2",
                "product_image2.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        List<MultipartFile> images = Arrays.asList(image1, image2);

        ProductCreateRequest request = new ProductCreateRequest(
                name,
                initialPrice,
                content,
                category,
                deadline
        );

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> productService.createProduct(request, images, memberDetails));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_PRICE);
    }

    @Test
    void 실패_유효하지_않은_카테고리() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);
        CustomMemberDetails memberDetails = new CustomMemberDetails(member);

        String name = "상품1";
        Integer initialPrice = 10000;
        String content = "이것은 상품 설명 부분";
        String category = "유효하지 않은 카테고리";
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);

        MockMultipartFile image1 = new MockMultipartFile(
                "image1",
                "product_image1.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "image2",
                "product_image2.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        List<MultipartFile> images = Arrays.asList(image1, image2);

        ProductCreateRequest request = new ProductCreateRequest(
                name,
                initialPrice,
                content,
                category,
                deadline
        );

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> productService.createProduct(request, images, memberDetails));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_CATEGORY);
    }

    @Test
    void 실패_유효하지_않은_마감시간() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member member = Member.of(mileage, status, socialId, role);
        CustomMemberDetails memberDetails = new CustomMemberDetails(member);

        String name = "상품1";
        Integer initialPrice = 10000;
        String content = "이것은 상품 설명 부분";
        String category = Category.BOOK.getKey();
        LocalDateTime deadline = LocalDateTime.now(); //유효하지 않은 마감시간

        MockMultipartFile image1 = new MockMultipartFile(
                "image1",
                "product_image1.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "image2",
                "product_image2.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes()
        );

        List<MultipartFile> images = Arrays.asList(image1, image2);

        ProductCreateRequest request = new ProductCreateRequest(
                name,
                initialPrice,
                content,
                category,
                deadline
        );

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> productService.createProduct(request, images, memberDetails));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_DEADLINE);
    }
}
