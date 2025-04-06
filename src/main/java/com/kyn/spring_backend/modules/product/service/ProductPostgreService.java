package com.kyn.spring_backend.modules.product.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import com.kyn.spring_backend.modules.product.entity.ProductPostgreEntity;
import com.kyn.spring_backend.modules.product.repository.ProductPostgreRepository;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostgreService {
    
    private final ProductPostgreRepository productRepository;
    
    public Mono<ProductPostgreEntity> createProduct(String name, String category, String description, 
                                                  String price, String image) {
        ProductPostgreEntity product = ProductPostgreEntity.builder()
            .name(name)
            .category(category)
            .description(description)
            .price(price)
            .image(image)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
            
        return productRepository.save(product)
            .doOnSuccess(saved -> log.info("Successfully saved product: {}", saved))
            .doOnError(error -> log.error("Error saving product: {}", error.getMessage()));
    }
    
    public Mono<ProductPostgreEntity> findByName(String name) {
        return productRepository.findByName(name);
    }
}