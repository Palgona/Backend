package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.image.Image;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductImage;
import com.palgona.palgona.domain.product.ProductState;
import com.palgona.palgona.domain.product.SortType;
import com.palgona.palgona.dto.ProductCreateRequest;
import com.palgona.palgona.dto.ProductResponse;
import com.palgona.palgona.dto.ProductUpdateRequest;
import com.palgona.palgona.dto.response.ProductPageResponse;
import com.palgona.palgona.repository.BiddingRepository;
import com.palgona.palgona.repository.ImageRepository;
import com.palgona.palgona.repository.ProductImageRepository;
import com.palgona.palgona.repository.product.ProductRepository;
import com.palgona.palgona.service.image.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static com.palgona.palgona.common.error.code.ProductErrorCode.INSUFFICIENT_PERMISSION;
import static com.palgona.palgona.common.error.code.ProductErrorCode.RELATED_BIDDING_EXISTS;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;

    private final ProductImageRepository productImageRepository;
    private final BiddingRepository biddingRepository;
    private final S3Service s3Service;

    @Transactional
    public void createProduct(ProductCreateRequest request, List<MultipartFile> imageFiles, CustomMemberDetails memberDetails) {

        Member member = memberDetails.getMember();

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

    public ProductResponse readProduct(Long productId){
        Product product = productRepository.findById(productId).get();

        List<String> imageUrls = productImageRepository.findByProduct(product).stream()
                .map(productImage -> productImage.getImage().getImageUrl())
                .collect(Collectors.toList());

        //Todo: 입찰 정보, 채팅, 찜 정보도 가져오는 로직 추가

        return ProductResponse.from(product, imageUrls);
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

        //Todo: 4-2. 상품과 관련된 정보들 삭제(찜 정보) + 채팅 정보는 어떻게?

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
}