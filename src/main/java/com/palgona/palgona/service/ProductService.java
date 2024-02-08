package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.image.Image;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductImage;
import com.palgona.palgona.dto.ProductCreateRequest;
import com.palgona.palgona.dto.ProductResponse;
import com.palgona.palgona.repository.ImageRepository;
import com.palgona.palgona.repository.ProductImageRepository;
import com.palgona.palgona.repository.ProductRepository;
import com.palgona.palgona.service.image.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;

    private final ProductImageRepository productImageRepository;
    private final S3Service s3Service;

    public void createProduct(ProductCreateRequest request, List<MultipartFile> imageFiles, CustomMemberDetails memberDetails) {

        Member member = memberDetails.getMember();

        //상품 저장
        Product product = Product.builder()
                .name(request.name())
                .initialPrice(request.initialPrice())
                .content(request.content())
                .category(Category.valueOf(request.category()))
                .deadline(request.deadline())
                .member(member)
                .build();

        productRepository.save(product);

        for (MultipartFile imageFile : imageFiles) {
            //이미지 저장
            String imageUrl = s3Service.upload(imageFile);

            Image image = Image.builder()
                    .imageUrl(imageUrl)
                    .member(member)
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

    public void deleteProduct(Long productId, CustomMemberDetails memberDetails){

        //Todo: 입찰이나 구매 완료 상품일때 예외 처리 로직 추가

        Member member = memberDetails.getMember();

        productRepository.findById(productId)
                .filter(product -> product.getMember().getId().equals(member.getId()))
                .ifPresent(productRepository::delete);

        //Todo: 상품 이미지, 찜, 채팅 정보 cascade delete 로직 추가
    }

    public void updateProduct(
            Long id,
            ProductCreateRequest request,
            List<MultipartFile> imageFiles,
            CustomMemberDetails memberDetails){

        //Todo: 입찰이나 구매 완료 상품일때 수정 불가능 처리 로직 추가

        Member member = memberDetails.getMember();

        // 상품 정보 수정
        Product product = productRepository.findById(id).get();

        product.updateName(request.name());
        product.updateInitialPrice(request.initialPrice());
        product.updateContent(request.content());
        product.updateCategory(Category.valueOf(request.category()));
        product.updateDeadline(request.deadline());

        //Todo: 상품 이미지 수정 로직 추가 ( 이미지를 전체적으로 제거하고 다시 올리기 or 수정된 이미지만 처리 )

    }

}