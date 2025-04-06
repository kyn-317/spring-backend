package com.kyn.spring_backend.modules.product.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.kyn.spring_backend.modules.product.entity.ProductPostgreEntity;
import reactor.core.publisher.Mono;

@Repository
public interface ProductPostgreRepository extends ReactiveCrudRepository<ProductPostgreEntity, Long> {
    Mono<ProductPostgreEntity> findByName(String name);
}