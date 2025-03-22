package com.kyn.spring_backend.modules.user.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.kyn.spring_backend.modules.user.entity.UserInfoEntity;

import reactor.core.publisher.Mono;

@Repository
public interface UserInfoRepository extends ReactiveMongoRepository<UserInfoEntity, ObjectId> {
    Mono<UserInfoEntity> findByUserEmail(String userEmail);

    Mono<UserInfoEntity> findByUserId(String userId);
}
