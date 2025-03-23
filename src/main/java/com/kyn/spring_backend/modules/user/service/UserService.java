package com.kyn.spring_backend.modules.user.service;

import java.util.Collections;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.kyn.spring_backend.modules.user.dto.UserEntityDtoUtil;
import com.kyn.spring_backend.base.dto.ResponseDto;
import com.kyn.spring_backend.modules.user.dto.UserAuthDto;
import com.kyn.spring_backend.modules.user.dto.UserInfoDto;
import com.kyn.spring_backend.modules.user.entity.UserAuthEntity;
import com.kyn.spring_backend.modules.user.entity.UserInfoEntity;
import com.kyn.spring_backend.modules.user.repository.UserAuthRepository;
import com.kyn.spring_backend.modules.user.repository.UserInfoRepository;
import com.kyn.spring_backend.base.security.JwtTokenProvider;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

        private final UserInfoRepository userInfoRepository;
        private final UserAuthRepository userAuthRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;

        public UserService(UserInfoRepository userInfoRepository, UserAuthRepository userAuthRepository,
                        PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
                this.userInfoRepository = userInfoRepository;
                this.userAuthRepository = userAuthRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtTokenProvider = jwtTokenProvider;
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
                                        // 이미 해당 권한이 있는지 확인
                                        return userAuthRepository
                                                        .findByUserObjectIdAndUserRole(user.get_id(),
                                                                        userAuthDto.getUserRole())
                                                        .flatMap(existingAuth -> {
                                                                // 이미 존재하면 그대로 반환 (중복 추가 방지)
                                                                return userAuthRepository
                                                                                .findByUserObjectId(user.get_id())
                                                                                .collectList()
                                                                                .map(auths -> UserEntityDtoUtil
                                                                                                .entityToDto(
                                                                                                                user,
                                                                                                                auths));
                                                        })
                                                        .switchIfEmpty(
                                                                        // 존재하지 않으면 새 권한 추가
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
                                        // 사용자의 모든 권한 조회
                                        return userAuthRepository.findByUserObjectId(user.get_id())
                                                        .collectList()
                                                        .flatMap(auths -> {
                                                                // 권한이 1개인 경우 삭제 불가 (최소 하나의 권한 필요)
                                                                if (auths.size() <= 1) {
                                                                        return Mono.just(UserEntityDtoUtil.entityToDto(
                                                                                        user,
                                                                                        auths));
                                                                }

                                                                // 권한 삭제
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
                                .flatMap(userInfo -> {
                                        // 비밀번호 검증
                                        if (!passwordEncoder.matches(userInfoDto.getUserPassword(),
                                                        userInfo.getUserPassword())) {
                                                // 비밀번호 불일치
                                                return Mono.just(ResponseDto.create("비밀번호 불일치", "비밀번호가 일치하지 않습니다.",
                                                                HttpStatus.NOT_FOUND));
                                        }

                                        // 사용자 권한 조회
                                        return userAuthRepository.findByUserObjectId(userInfo.get_id())
                                                        .collectList()
                                                        .flatMap(auths -> {
                                                                // 권한 목록 생성
                                                                String authorities = auths.stream()
                                                                                .map(UserAuthEntity::getUserRole)
                                                                                .collect(Collectors.joining(","));

                                                                // 인증 정보 생성
                                                                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                                                                userInfo.getUserId(),
                                                                                null,
                                                                                auths.stream()
                                                                                                .map(auth -> new SimpleGrantedAuthority(
                                                                                                                auth.getUserRole()))
                                                                                                .collect(Collectors
                                                                                                                .toList()));

                                                                // JWT 토큰 생성 (JwtTokenProvider 의존성 주입 필요)
                                                                String token = jwtTokenProvider
                                                                                .createToken(authentication);

                                                                // 토큰을 포함한 응답 생성
                                                                return Mono.just(ResponseDto.create(token, "로그인 성공",
                                                                                HttpStatus.OK));
                                                        });
                                })
                                .switchIfEmpty(Mono.just(
                                                ResponseDto.create(null, "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)));
        }
}