package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.bidding.Bidding;
import com.palgona.palgona.domain.image.Image;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.product.Category;
import com.palgona.palgona.domain.product.Product;
import com.palgona.palgona.domain.product.ProductImage;
import com.palgona.palgona.dto.ProductCreateRequest;
import com.palgona.palgona.dto.ProductResponse;
import com.palgona.palgona.dto.ProductUpdateRequest;
import com.palgona.palgona.repository.BiddingRepository;
import com.palgona.palgona.repository.ImageRepository;
import com.palgona.palgona.repository.ProductImageRepository;
import com.palgona.palgona.repository.ProductRepository;
import com.palgona.palgona.service.image.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;

    private final ProductImageRepository productImageRepository;
    private final BiddingRepository biddingRepository;
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

        Member member = memberDetails.getMember();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException());


        //1. 상품에 대한 권한 확인
        if (!hasPermission(member, product)) {
            throw new IllegalStateException("해당 상품에 대한 권한이 없습니다.");
        }


        //2. 입찰 내역에 있는 상품인지 확인
        if (hasRelatedBidding(product)) {
            throw new IllegalStateException("해당 상품과 관련된 입찰 내역이 있어 삭제할 수 없습니다.");
        }

        //Todo: 3. 구매 내역에 있는 상품인지 체크

        //4. 상품과 관련된 이미지 및 이미지 연관관계 삭제
        List<ProductImage> productImages = productImageRepository.findByProduct(product);
        for (ProductImage productImage : productImages) {
            Image image = productImage.getImage();
            s3Service.deleteFile(image.getImageUrl());
            productImageRepository.delete(productImage);
            imageRepository.delete(image);
        }

        //Todo: 4-2. 상품과 관련된 정보들 삭제(찜 정보) + 채팅 정보는 어떻게?

        //5. 상품 삭제
        productRepository.delete(product);
    }

    public void updateProduct(
            Long id,
            ProductUpdateRequest request,
            List<MultipartFile> imageFiles,
            CustomMemberDetails memberDetails){

        Member member = memberDetails.getMember();

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException());


        //1. 상품에 대한 권한 확인
        if (!hasPermission(member, product)) {
            throw new IllegalStateException("해당 상품에 대한 권한이 없습니다.");
        }

        //2. 입찰 내역에 있는 상품인지 확인
        if (hasRelatedBidding(product)) {
            throw new IllegalStateException("해당 상품과 관련된 입찰 내역이 있어 수정할 수 없습니다.");
        }

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

        //5. 상품 정보 수정
        product.updateName(request.name());
        product.updateInitialPrice(request.initialPrice());
        product.updateContent(request.content());
        product.updateCategory(Category.valueOf(request.category()));
        product.updateDeadline(request.deadline());
    }

    private boolean hasRelatedBidding(Product product){
        Pageable pageable = PageRequest.of(0, 1);
        Page<Bidding> biddings = biddingRepository.findAllByProduct(pageable, product);
        return biddings.getTotalElements() > 0;
    }

    private boolean hasPermission(Member member, Product product) {
        return product.getMember().getId().equals(member.getId()) || (member.getRole() == Role.ADMIN);
    }

}