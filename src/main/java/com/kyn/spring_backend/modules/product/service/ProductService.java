package com.kyn.spring_backend.modules.product.service;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCacheReactive;

import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.kyn.spring_backend.base.util.MongoDbUtil;
import com.kyn.spring_backend.modules.product.dto.EntityDtoUtil;
import com.kyn.spring_backend.modules.product.dto.ProductBasDto;
import com.kyn.spring_backend.modules.product.repository.ProductBasRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final ProductBasRepository productBasRepository;

    private final RMapCacheReactive<String, ProductBasDto> productBasMap;

    private static final int CACHE_TTL = 20;

    public ProductService(ProductBasRepository productBasRepository, RedissonClient redissonClient) {
        this.productBasRepository = productBasRepository;
        this.productBasMap = redissonClient.reactive().getMapCache("product:id");
    }

    public Flux<ProductBasDto> findAll() {
        return productBasRepository.findAll().map(EntityDtoUtil::entityToDto);
    }

    public Mono<ProductBasDto> findById(String id) {
        return this.productBasMap.get(id)
                .switchIfEmpty(
                        productBasRepository.findById(MongoDbUtil.toObjectId(id))
                                .map(EntityDtoUtil::entityToDto)
                                .flatMap(dto -> this.productBasMap.fastPut(id, dto, CACHE_TTL, TimeUnit.SECONDS)
                                        .thenReturn(dto)));
    }

    public Flux<ProductBasDto> findbyProductPaging(int page, int size) {
        return productBasRepository.findAllBy(PageRequest.of(page, size)).map(EntityDtoUtil::entityToDto);
    }
}
