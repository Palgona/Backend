package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.dto.ProductCreateRequest;
import com.palgona.palgona.dto.ProductResponse;
import com.palgona.palgona.dto.ProductUpdateRequest;
import com.palgona.palgona.service.ProductService;
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
    public ResponseEntity<ProductResponse> readProduct(@PathVariable Long productId){
        ProductResponse productResponse = productService.readProduct(productId);

        return ResponseEntity.ok()
                .body(productResponse);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId, @AuthenticationPrincipal CustomMemberDetails member){

        productService.deleteProduct(productId, member);

        return ResponseEntity.ok().build();
    }

    @PutMapping(
            value = "/{productId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
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