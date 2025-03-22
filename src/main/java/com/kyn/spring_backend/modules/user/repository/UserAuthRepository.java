package com.kyn.spring_backend.modules.user.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.kyn.spring_backend.modules.user.entity.UserAuthEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserAuthRepository extends ReactiveMongoRepository<UserAuthEntity, ObjectId> {
    Flux<UserAuthEntity> findByUserObjectId(ObjectId userObjectId);

    Mono<UserAuthEntity> findByUserObjectIdAndUserRole(ObjectId userObjectId, String userRole);

    Mono<Void> deleteByUserObjectIdAndUserRole(ObjectId userObjectId, String userRole);
}
