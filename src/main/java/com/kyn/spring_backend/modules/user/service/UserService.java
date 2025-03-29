package com.kyn.spring_backend.modules.user.service;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.kyn.spring_backend.modules.user.dto.UserEntityDtoUtil;
import com.kyn.spring_backend.base.dto.ResponseDto;
import com.kyn.spring_backend.base.exception.InvalidTokenException;
import com.kyn.spring_backend.modules.product.dto.ProductBasDto;
import com.kyn.spring_backend.modules.user.dto.UserAuthDto;
import com.kyn.spring_backend.modules.user.dto.UserInfoDto;
import com.kyn.spring_backend.modules.user.entity.UserAuthEntity;
import com.kyn.spring_backend.modules.user.exception.InvalidCredentialsException;
import com.kyn.spring_backend.modules.user.exception.UserNotFoundException;
import com.kyn.spring_backend.modules.user.repository.UserAuthRepository;
import com.kyn.spring_backend.modules.user.repository.UserInfoRepository;
import com.kyn.spring_backend.base.security.JwtTokenProvider;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

@Service
public class UserService {

        private final UserInfoRepository userInfoRepository;
        private final UserAuthRepository userAuthRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final RMapCacheReactive<String, Boolean> jwtBlacklistMap;

        public UserService(UserInfoRepository userInfoRepository, UserAuthRepository userAuthRepository,
                        PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                        RedissonClient redissonClient) {
                this.userInfoRepository = userInfoRepository;
                this.userAuthRepository = userAuthRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtTokenProvider = jwtTokenProvider;
                this.jwtBlacklistMap = redissonClient.reactive().getMapCache("logout:id");
        }

        public Mono<UserInfoDto> createUser(UserInfoDto userInfoDto) {
                // password encode and entity set
                userInfoDto.setUserPassword(passwordEncoder.encode(userInfoDto.getUserPassword()));
                // create userInfoEntity -> saveUserAuthEntity by savedUserInfo -> make UserInfoDto and return 
                return userInfoRepository.save(UserEntityDtoUtil.createUserInfoEntity(userInfoDto))
                                .flatMap(savedUser -> userAuthRepository
                                                .save(UserEntityDtoUtil.createUserAuthEntity(savedUser))
                                                .map(savedAuth -> UserEntityDtoUtil.entityToDto(
                                                                savedUser,
                                                                Collections.singletonList(savedAuth))));
        }

        // search UserInfo
        public Mono<UserInfoDto> findUserById(String id) {
                return userInfoRepository.findById(new ObjectId(id))
                                .flatMap(user -> userAuthRepository.findByUserObjectId(user.get_id())
                                                .collectList()
                                                .map(auths -> UserEntityDtoUtil.entityToDto(user, auths)));
        }

        // search UserInfo by email
        public Mono<UserInfoDto> findUserByEmail(String email) {
                return userInfoRepository.findByUserEmail(email)
                                .flatMap(user -> userAuthRepository.findByUserObjectId(user.get_id())
                                                .collectList()
                                                .map(auths -> UserEntityDtoUtil.entityToDto(user, auths)));
        }

        // search UserInfo by userId
        public Mono<UserInfoDto> findUserByUserId(String userId) {
                return userInfoRepository.findByUserId(userId)
                                .flatMap(user -> userAuthRepository.findByUserObjectId(user.get_id())
                                                .collectList()
                                                .map(auths -> UserEntityDtoUtil.entityToDto(user, auths)));
        }

        // update UserInfo (email, password, name only can update)
        public Mono<UserInfoDto> updateUser(UserInfoDto userInfoDto) {
                return userInfoRepository.findById(new ObjectId(userInfoDto.getId()))
                                .flatMap(existingUser -> {
                                        return userInfoRepository
                                                        .save(UserEntityDtoUtil.updateUserInfoEntity(userInfoDto,
                                                                        existingUser, passwordEncoder))
                                                        .flatMap(updatedUser -> userAuthRepository
                                                                        .findByUserObjectId(updatedUser.get_id())
                                                                        .collectList()
                                                                        .map(auths -> UserEntityDtoUtil.entityToDto(
                                                                                        updatedUser, auths)));
                                });
        }

        // 사용자 권한 추가
        public Mono<UserInfoDto> addUserAuth(String userId, UserAuthDto userAuthDto) {
                return userInfoRepository.findById(new ObjectId(userId))
                                .flatMap(user -> {
                                        return userAuthRepository
                                                        .findByUserObjectIdAndUserRole(user.get_id(),
                                                                        userAuthDto.getUserRole())
                                                        .flatMap(existingAuth -> {
                                                                return userAuthRepository
                                                                                .findByUserObjectId(user.get_id())
                                                                                .collectList()
                                                                                .map(auths -> UserEntityDtoUtil
                                                                                                .entityToDto(
                                                                                                                user,
                                                                                                                auths));
                                                        })
                                                        .switchIfEmpty(
                                                                        Mono.defer(() -> {
                                                                                UserAuthEntity authEntity = new UserAuthEntity();
                                                                                authEntity.setUserObjectId(
                                                                                                user.get_id());
                                                                                authEntity.setUserEmail(
                                                                                                user.getUserEmail());
                                                                                authEntity.setUserRole(userAuthDto
                                                                                                .getUserRole());
                                                                                authEntity.insertDocument(
                                                                                                user.getUserId());

                                                                                return userAuthRepository
                                                                                                .save(authEntity)
                                                                                                .then(userAuthRepository
                                                                                                                .findByUserObjectId(
                                                                                                                                user.get_id())
                                                                                                                .collectList()
                                                                                                                .map(auths -> UserEntityDtoUtil
                                                                                                                                .entityToDto(user,
                                                                                                                                                auths)));
                                                                        }));
                                });
        }

        // deleteAuth
        public Mono<UserInfoDto> removeUserAuth(String userId, String role) {
                return userInfoRepository.findById(new ObjectId(userId))
                                .flatMap(user -> {

                                        return userAuthRepository.findByUserObjectId(user.get_id())
                                                        .collectList()
                                                        .flatMap(auths -> {

                                                                if (auths.size() <= 1) {
                                                                        return Mono.just(UserEntityDtoUtil.entityToDto(
                                                                                        user,
                                                                                        auths));
                                                                }

                                                                return userAuthRepository
                                                                                .deleteByUserObjectIdAndUserRole(
                                                                                                user.get_id(), role)
                                                                                .then(userAuthRepository
                                                                                                .findByUserObjectId(user
                                                                                                                .get_id())
                                                                                                .collectList()
                                                                                                .map(updatedAuths -> UserEntityDtoUtil
                                                                                                                .entityToDto(user,
                                                                                                                                updatedAuths)));
                                                        });
                                });
        }

        // getAllAuths
        public Flux<UserAuthDto> getUserAuths(String userId) {
                return userInfoRepository.findById(new ObjectId(userId))
                                .flatMapMany(user -> userAuthRepository.findByUserObjectId(user.get_id())
                                                .map(UserEntityDtoUtil::authEntityToDto));
        }

        public Mono<ResponseDto<String>> login(UserInfoDto userInfoDto) {
                return userInfoRepository.findByUserEmail(userInfoDto.getUserEmail())
                                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                                .flatMap(userInfo -> {
                                        if (!passwordEncoder.matches(userInfoDto.getUserPassword(),
                                                        userInfo.getUserPassword())) {
                                                return Mono.error(new InvalidCredentialsException());
                                        }

                                        return userAuthRepository.findByUserObjectId(userInfo.get_id())
                                                        .collectList()
                                                        .flatMap(auths -> {
                                                                String authorities = auths.stream()
                                                                                .map(UserAuthEntity::getUserRole)
                                                                                .collect(Collectors.joining(","));

                                                                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                                                                userInfo.getUserId(),
                                                                                null,
                                                                                auths.stream()
                                                                                                .map(auth -> new SimpleGrantedAuthority(
                                                                                                                auth.getUserRole()))
                                                                                                .collect(Collectors
                                                                                                                .toList()));

                                                                String token = jwtTokenProvider
                                                                                .createToken(authentication);

                                                                return Mono.just(ResponseDto.create(token,
                                                                                "loginSuccess",
                                                                                HttpStatus.OK));
                                                        });
                                });
        }

        public Mono<ResponseDto<String>> isLogin(String token) {
                return Mono.just(token)
                                .flatMap(t -> (!jwtTokenProvider.validateToken(t) || jwtBlacklistMap.get(t).block())
                                                ? Mono.error(new InvalidTokenException())
                                                : Mono.just(ResponseDto.create("SUCCESS LOGIN",
                                                                "login success", HttpStatus.OK)));
        }

        public Mono<ResponseDto<String>> logout(String token) {
                if (!jwtTokenProvider.validateToken(token)) {
                        return Mono.error(new InvalidTokenException());
                }

                Jws<Claims> claims = jwtTokenProvider.getClaims(token);
                long ttl = (claims.getPayload().getExpiration().getTime()
                                - System.currentTimeMillis()) / 1000;

                return jwtBlacklistMap.fastPut(token, true, ttl, TimeUnit.SECONDS)
                                .map(result -> ResponseDto.create("SUCCESS LOGOUT", "logout success", HttpStatus.OK));
        }
}