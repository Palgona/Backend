package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.bookmark.Bookmark;
import com.palgona.palgona.domain.image.Image;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductImage;
import com.palgona.palgona.domain.product.ProductState;
import com.palgona.palgona.domain.product.SortType;
import com.palgona.palgona.dto.ProductCreateRequest;
import com.palgona.palgona.dto.ProductDetailResponse;
import com.palgona.palgona.dto.ProductUpdateRequest;
import com.palgona.palgona.repository.*;
import com.palgona.palgona.dto.response.ProductPageResponse;
import com.palgona.palgona.repository.BiddingRepository;
import com.palgona.palgona.repository.ImageRepository;
import com.palgona.palgona.repository.ProductImageRepository;
import com.palgona.palgona.repository.product.ProductRepository;
import com.palgona.palgona.repository.product.querydto.ProductDetailQueryResponse;
import com.palgona.palgona.service.image.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.palgona.palgona.common.error.code.ProductErrorCode.*;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;

    private final ProductImageRepository productImageRepository;
    private final BiddingRepository biddingRepository;
    private final S3Service s3Service;

    @Transactional
    public void createProduct(ProductCreateRequest request, List<MultipartFile> imageFiles, CustomMemberDetails memberDetails) {

        Member member = memberDetails.getMember();

        //1. 상품 정보 유효성 확인
        checkProduct(request.initialPrice(), request.category(), request.deadline());

        //상품 저장
        Product product = Product.builder()
                .name(request.name())
                .initialPrice(request.initialPrice())
                .content(request.content())
                .category(Category.valueOf(request.category()))
                .productState(ProductState.ON_SALE)
                .deadline(request.deadline())
                .member(member)
                .build();

        productRepository.save(product);

        for (MultipartFile imageFile : imageFiles) {
            //이미지 저장
            String imageUrl = s3Service.upload(imageFile);

            Image image = Image.builder()
                    .imageUrl(imageUrl)
                    .build();

            imageRepository.save(image);

            //상품 이미지 연관관계 저장
            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .image(image)
                    .build();

            productImageRepository.save(productImage);
        }
    }

    public ProductDetailResponse readProduct(Long productId){
        //1. 상품 정보 가져오기(상품, 멤버, 최고 입찰가, 북마크 개수)
        //Todo: 1-2. 채팅 개수 정보 가져오기
        ProductDetailQueryResponse queryResponse = productRepository.findProductWithAll(productId)
                .orElseThrow(() -> new IllegalArgumentException());

        //2. 상품이 삭제되었는지 확인
        if(ProductState.valueOf(queryResponse.productState()) == ProductState.DELETED){
            throw new BusinessException(DELETED_PRODUCT);
        }

        //3. 상품 이미지 가져오기
        List<String> imageUrls = productImageRepository.findProductImageUrlsByProduct(queryResponse.productId());

        return ProductDetailResponse.from(queryResponse, imageUrls);
    }

    @Transactional(readOnly = true)
    public SliceResponse<ProductPageResponse> readProducts(SortType sortType,
                                                           Category category,
                                                           String searchWord,
                                                           String cursor,
                                                           int pageSize
    ) {
        return productRepository.findAllByCategoryAndSearchWord(category, searchWord, cursor, sortType, pageSize);
    }

    @Transactional
    public void deleteProduct(Long productId, CustomMemberDetails memberDetails){

        Member member = memberDetails.getMember();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException());


        //1. 상품에 대한 권한 확인
        checkPermission(member, product);

        //2. 입찰 내역에 있는 상품인지 확인
        checkRelatedBidding(product);

        //Todo: 3. 구매 내역에 있는 상품인지 체크

        //4. 상품과 관련된 이미지 및 이미지 연관관계 삭제 (soft delete면 아래 로직 수행 x)
//        List<ProductImage> productImages = productImageRepository.findByProduct(product);
//        for (ProductImage productImage : productImages) {
//            Image image = productImage.getImage();
//            s3Service.deleteFile(image.getImageUrl());
//            productImageRepository.delete(productImage);
//            imageRepository.delete(image);
//        }

        //4. 상품과 관련된 정보들 삭제
        //4-1. 상품 찜 정보 삭제
        bookmarkRepository.deleteByProduct(product);

        //5. 상품의 상태를 DELETED로 업데이트 (soft delete)
        product.updateProductState(ProductState.DELETED);
    }

    @Transactional
    public void updateProduct(
            Long id,
            ProductUpdateRequest request,
            List<MultipartFile> imageFiles,
            CustomMemberDetails memberDetails){

        Member member = memberDetails.getMember();

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException());

        //0. 상품 유효성 확인
        checkProduct(request.initialPrice(), request.category(), request.deadline());

        //1. 상품에 대한 권한 확인
        checkPermission(member, product);

        //2. 입찰 내역에 있는 상품인지 확인
        checkRelatedBidding(product);

        //Todo: 3. 구매 내역에 있는 상품인지 체크


        //4. 상품 이미지 수정
        //4-1. 삭제된 상품 이미지 처리
        List<String> deletedImageUrls = request.deletedImageUrls();

        for (String imageUrl : deletedImageUrls) {
            // 이미지 파일 삭제 (S3 또는 다른 스토리지에 있는 이미지 파일 삭제)
            s3Service.deleteFile(imageUrl);
            // 이미지와 연관된 상품 이미지 및 이미지 삭제
            ProductImage productImage = productImageRepository.findByProductAndImageUrl(product, imageUrl)
                    .orElseThrow(() -> new IllegalArgumentException());

            productImageRepository.delete(productImage);
            imageRepository.delete(productImage.getImage());

        }

        //4-2. 새로 추가된 상품 이미지 저장
        for (MultipartFile imageFile : imageFiles) {
            //이미지 저장
            String imageUrl = s3Service.upload(imageFile);

            Image image = Image.builder()
                    .imageUrl(imageUrl)
                    .build();

            imageRepository.save(image);

            //상품 이미지 연관관계 저장
            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .image(image)
                    .build();

            productImageRepository.save(productImage);
        }

        //5. 상품 정보 수정
        product.updateName(request.name());
        product.updateInitialPrice(request.initialPrice());
        product.updateContent(request.content());
        product.updateCategory(Category.valueOf(request.category()));
        product.updateDeadline(request.deadline());
    }

    private void checkRelatedBidding(Product product){
        if (biddingRepository.existsByProduct(product)) {
            throw new BusinessException(RELATED_BIDDING_EXISTS);
        }
    }

    private void checkPermission(Member member, Product product) {
        if (!(product.isOwner(member) || member.isAdmin())) {
            throw new BusinessException(INSUFFICIENT_PERMISSION);
        }
    }

    private void checkProduct(int price, String category, LocalDateTime deadline){
        //1. 상품 가격이 마이너스인 경우 처리
        if(price < 0){
            throw new BusinessException(INVALID_PRICE);
        }

        //2. 상품 카테고리가 없는 경우
        try {
            Category.valueOf(category);
        } catch (Exception e) {
            throw new BusinessException(INVALID_CATEGORY);
        }


        //3. 상품 판매 기간이 하루 미만일 경우
        if(deadline.isBefore(LocalDateTime.now().plusDays(1))){
            throw new BusinessException(INVALID_DEADLINE);
        }
    }

}