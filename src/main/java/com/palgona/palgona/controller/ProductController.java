package com.palgona.palgona.controller;

import com.palgona.palgona.dto.ProductCreateRequest;
import com.palgona.palgona.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            @RequestPart(value = "files") List<MultipartFile> files
    ){

        productService.createProduct(request, files);

        return ResponseEntity.ok()
                .build();
    }

}