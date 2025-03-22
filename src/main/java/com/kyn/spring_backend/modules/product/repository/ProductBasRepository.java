package com.kyn.spring_backend.modules.product.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.kyn.spring_backend.modules.product.entity.ProductBasEntity;

import reactor.core.publisher.Flux;

@Repository
public interface ProductBasRepository extends ReactiveMongoRepository<ProductBasEntity, ObjectId> {
    Flux<ProductBasEntity> findAllBy(Pageable pageable);
}
