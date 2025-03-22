package com.kyn.spring_backend.modules.user.service;

import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kyn.spring_backend.modules.user.dto.EntityDtoUtil;
import com.kyn.spring_backend.modules.user.dto.UserAuthDto;
import com.kyn.spring_backend.modules.user.dto.UserInfoDto;
import com.kyn.spring_backend.modules.user.entity.UserAuthEntity;
import com.kyn.spring_backend.modules.user.entity.UserInfoEntity;
import com.kyn.spring_backend.modules.user.repository.UserAuthRepository;
import com.kyn.spring_backend.modules.user.repository.UserInfoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

        private final UserInfoRepository userInfoRepository;
        private final UserAuthRepository userAuthRepository;
        private final PasswordEncoder passwordEncoder;

        public UserService(UserInfoRepository userInfoRepository, UserAuthRepository userAuthRepository,
                        PasswordEncoder passwordEncoder) {
                this.userInfoRepository = userInfoRepository;
                this.userAuthRepository = userAuthRepository;
                this.passwordEncoder = passwordEncoder;
        }

        public Mono<UserInfoDto> createUser(UserInfoDto userInfoDto) {
                // password encode and entity set
                userInfoDto.setUserPassword(passwordEncoder.encode(userInfoDto.getUserPassword()));
                UserInfoEntity userInfoEntity = EntityDtoUtil.dtoToEntity(userInfoDto);
                userInfoEntity.insertDocument(userInfoDto.getUserId());
                return userInfoRepository.save(userInfoEntity)
                                .flatMap(savedUser -> {
                                        UserAuthEntity authEntity = UserAuthEntity.create(null, savedUser.get_id(),
                                                        savedUser.getUserEmail(), "USER");
                                        authEntity.insertDocument(savedUser.getUserId());

                                        return userAuthRepository.save(authEntity)
                                                        .flatMap(savedAuth -> {
                                                                return userAuthRepository
                                                                                .findByUserObjectId(savedUser.get_id())
                                                                                .collectList()
                                                                                .map(auths -> EntityDtoUtil.entityToDto(
                                                                                                savedUser, auths));
                                                        });
                                });
        }

        // 사용자 정보 조회
        public Mono<UserInfoDto> findUserById(String id) {
                return userInfoRepository.findById(new ObjectId(id))
                                .flatMap(user -> userAuthRepository.findByUserObjectId(user.get_id())
                                                .collectList()
                                                .map(auths -> EntityDtoUtil.entityToDto(user, auths)));
        }

        // 이메일로 사용자 정보 조회
        public Mono<UserInfoDto> findUserByEmail(String email) {
                return userInfoRepository.findByUserEmail(email)
                                .flatMap(user -> userAuthRepository.findByUserObjectId(user.get_id())
                                                .collectList()
                                                .map(auths -> EntityDtoUtil.entityToDto(user, auths)));
        }

        // 사용자 ID로 사용자 정보 조회
        public Mono<UserInfoDto> findUserByUserId(String userId) {
                return userInfoRepository.findByUserId(userId)
                                .flatMap(user -> userAuthRepository.findByUserObjectId(user.get_id())
                                                .collectList()
                                                .map(auths -> EntityDtoUtil.entityToDto(user, auths)));
        }

        // 사용자 정보 수정
        public Mono<UserInfoDto> updateUser(UserInfoDto userInfoDto) {
                return userInfoRepository.findById(new ObjectId(userInfoDto.getId()))
                                .flatMap(existingUser -> {
                                        // 비밀번호가 변경되면 암호화
                                        if (userInfoDto.getUserPassword() != null
                                                        && !userInfoDto.getUserPassword().isEmpty()) {
                                                userInfoDto.setUserPassword(
                                                                passwordEncoder.encode(userInfoDto.getUserPassword()));
                                        } else {
                                                userInfoDto.setUserPassword(existingUser.getUserPassword());
                                        }

                                        UserInfoEntity updatedEntity = EntityDtoUtil.dtoToEntity(userInfoDto);
                                        updatedEntity.updateDocument(
                                                        userInfoDto.getUserId() != null ? userInfoDto.getUserId()
                                                                        : existingUser.getUserId());

                                        return userInfoRepository.save(updatedEntity)
                                                        .flatMap(updatedUser -> userAuthRepository
                                                                        .findByUserObjectId(updatedUser.get_id())
                                                                        .collectList()
                                                                        .map(auths -> EntityDtoUtil.entityToDto(
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
                                                                                .map(auths -> EntityDtoUtil.entityToDto(
                                                                                                user, auths));
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
                                                                                                                .map(auths -> EntityDtoUtil
                                                                                                                                .entityToDto(user,
                                                                                                                                                auths)));
                                                                        }));
                                });
        }

        // 사용자 권한 제거
        public Mono<UserInfoDto> removeUserAuth(String userId, String role) {
                return userInfoRepository.findById(new ObjectId(userId))
                                .flatMap(user -> {
                                        // 사용자의 모든 권한 조회
                                        return userAuthRepository.findByUserObjectId(user.get_id())
                                                        .collectList()
                                                        .flatMap(auths -> {
                                                                // 권한이 1개인 경우 삭제 불가 (최소 하나의 권한 필요)
                                                                if (auths.size() <= 1) {
                                                                        return Mono.just(EntityDtoUtil.entityToDto(user,
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
                                                                                                .map(updatedAuths -> EntityDtoUtil
                                                                                                                .entityToDto(user,
                                                                                                                                updatedAuths)));
                                                        });
                                });
        }

        // 사용자의 모든 권한 조회
        public Flux<UserAuthDto> getUserAuths(String userId) {
                return userInfoRepository.findById(new ObjectId(userId))
                                .flatMapMany(user -> userAuthRepository.findByUserObjectId(user.get_id())
                                                .map(EntityDtoUtil::authEntityToDto));
        }
}