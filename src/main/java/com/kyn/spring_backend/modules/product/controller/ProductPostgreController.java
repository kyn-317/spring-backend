package com.kyn.spring_backend.modules.product.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.kyn.spring_backend.modules.product.dto.ProductBasDto;
import com.kyn.spring_backend.modules.product.entity.ProductPostgreEntity;
import com.kyn.spring_backend.modules.product.repository.ProductPostgreRepository;
import com.kyn.spring_backend.modules.product.service.ProductPostgreService;
import com.kyn.spring_backend.modules.product.service.ProductService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/products/postgre")
@RequiredArgsConstructor
public class ProductPostgreController {
    
    private final ProductPostgreService productService;
    private final ProductService mongoProductService;
    private final ProductPostgreRepository repository;

    @PostMapping
    public Mono<ResponseEntity<ProductPostgreEntity>> createProduct(@RequestBody ProductRequest request) {
        return productService.createProduct(
            request.getName(),
            request.getCategory(),
            request.getDescription(),
            request.getPrice(),
            request.getImage()
        )
        .map(ResponseEntity::ok)
        .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    @GetMapping("/db-connection")
    public Mono<String> testConnection() {
        return repository.count()
            .map(count -> "Successfully connected to database. Total products: " + count)
            .onErrorResume(e -> Mono.just("Connection failed: " + e.getMessage()));
    }
    @GetMapping("/{name}")
    public Mono<ResponseEntity<ProductPostgreEntity>> getProductByName(@PathVariable String name) {
        return productService.findByName(name)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("all")
    public Mono<ResponseEntity<List<ProductPostgreEntity>>> postMethodName() {
        
        
        return mongoProductService.findAll()
        .flatMap(request -> productService.createProduct(request.getProductName(), request.getProductCategory(), request.getProductDescription(), request.getProductPrice(), request.getProductImage()))
        .collectList()
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public ProductRequest toRequest(ProductBasDto dto) {
        return new ProductRequest(dto.getProductName(), dto.getProductCategory(), dto.getProductDescription(), dto.getProductPrice(), dto.getProductImage());
    }

}


// Request DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
class ProductRequest {
    private String name;
    private String category;
    private String description;
    private String price;
    private String image;
}