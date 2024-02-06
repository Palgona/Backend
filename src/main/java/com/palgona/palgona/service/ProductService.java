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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;

    private final ProductImageRepository productImageRepository;

    public void createProduct(ProductCreateRequest request, List<MultipartFile> imageFiles){

        //시큐리티 컨텍스트 홀더에서 인증 객체 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomMemberDetails){

            //인증 객체에 저장된 멤버 정보 추출
            Member member = ((CustomMemberDetails) authentication.getPrincipal()).getMember();

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

            for(MultipartFile imageFile : imageFiles) {
                //이미지 저장
                String imageUrl = saveImage(imageFile, member.getId(), request.name());

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

        } else {
            // 사용자가 인증되지 않았거나 'CustomMemberDetails'를 사용할 수 없는 경우
            // 예외 처리 코드 삽입
        }

    }

    public ProductResponse readProduct(Long productId){
        Product product = productRepository.findById(productId).get();

        List<String> imageUrls = productImageRepository.findByProduct(product).stream()
                .map(productImage -> productImage.getImage().getImageUrl())
                .collect(Collectors.toList());

        return ProductResponse.from(product, imageUrls);
    }

    //테스트용) 로컬 resources/img폴더에 사진 저장
    private String saveImage(MultipartFile imageFile, Long memberId, String productName) {
        String uploadDir = getClass().getClassLoader().getResource("img").getPath();
        uploadDir = uploadDir.replaceFirst("/", "");

        String fileName = "/"+ System.currentTimeMillis() + "_" + productName + "_" + imageFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);

        try {
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

}