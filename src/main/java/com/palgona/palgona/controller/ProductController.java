package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.dto.ProductCreateRequest;
import com.palgona.palgona.dto.ProductResponse;
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
public class ProductController {

    private final ProductService productService;

    @PostMapping(
            value = "/product",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> createProduct(
            @RequestPart(value = "productReq") ProductCreateRequest request,
            @RequestPart(value = "files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomMemberDetails member
    ){

        productService.createProduct(request, files, member);

        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductResponse> readProduct( @PathVariable Long id ){
        ProductResponse productResponse = productService.readProduct(id);

        return ResponseEntity.ok()
                .body(productResponse);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomMemberDetails member
    ){

        productService.deleteProduct(id, member);

        return ResponseEntity.ok().build();
    }

    @PutMapping(
            value = "/product/{id}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> createProduct(
            @PathVariable Long id,
            @RequestPart(value = "productReq") ProductCreateRequest request,
            @RequestPart(value = "files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomMemberDetails member
    ){

        return ResponseEntity.ok().build();
    }


}