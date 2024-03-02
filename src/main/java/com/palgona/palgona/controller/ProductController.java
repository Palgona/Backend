package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.dto.ProductCreateRequest;
import com.palgona.palgona.dto.ProductResponse;
import com.palgona.palgona.dto.ProductUpdateRequest;
import com.palgona.palgona.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "상품 등록 api", description = "상품 정보와 상품 사진들을 받아서 상품 등록을 진행한다.")
    public ResponseEntity<Void> createProduct(
            @RequestPart(value = "productReq") ProductCreateRequest request,
            @RequestPart(value = "files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomMemberDetails member
    ){

        productService.createProduct(request, files, member);

        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회 api", description = "상품 id를 받아 상품 상세 정보를 보여준다.")
    public ResponseEntity<ProductResponse> readProduct(@PathVariable Long productId){
        ProductResponse productResponse = productService.readProduct(productId);

        return ResponseEntity.ok()
                .body(productResponse);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 삭제 api", description = "상품 id를 받아 해당 상품 삭제 처리를 진행한다. ")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        productService.deleteProduct(productId, member);

        return ResponseEntity.ok().build();
    }

    @PutMapping(
            value = "/{productId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "상품 수정 api", description = "상품 id를 받아 해당 상품 수정 처리를 진행한다.")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long productId,
            @RequestPart(value = "productReq") ProductUpdateRequest request,
            @RequestPart(value = "files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomMemberDetails member
    ){

        productService.updateProduct(productId, request, files, member);

        return ResponseEntity.ok().build();
    }


}