package com.kyn.spring_backend;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.kyn.spring_backend.modules.user.dto.UserAuthDto;
import com.kyn.spring_backend.modules.user.dto.UserInfoDto;
import com.kyn.spring_backend.modules.user.repository.UserAuthRepository;
import com.kyn.spring_backend.modules.user.repository.UserInfoRepository;
import com.kyn.spring_backend.modules.user.service.UserService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class UserTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void createUserTest() {
        // 테스트 데이터 생성
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setUserId("testuser1");
        userInfoDto.setUserName("테스트 사용자");
        userInfoDto.setUserEmail("testuser1@example.com");
        userInfoDto.setUserPassword("password123");

        // 사용자 생성 테스트
        StepVerifier.create(userService.createUser(userInfoDto))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findUserByEmailTest() {
        // 테스트 데이터 생성
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setUserId("testuser2");
        userInfoDto.setUserName("테스트 사용자2");
        userInfoDto.setUserEmail("testuser2@example.com");
        userInfoDto.setUserPassword("password123");

        // 사용자 생성 후 이메일로 조회 테스트
        Mono<UserInfoDto> createdUserMono = userService.createUser(userInfoDto)
                .flatMap(createdUser -> userService.findUserByEmail("testuser2@example.com"));

        StepVerifier.create(createdUserMono)
                .assertNext(foundUser -> {
                    Assertions.assertEquals("testuser2", foundUser.getUserId());
                    Assertions.assertEquals("테스트 사용자2", foundUser.getUserName());
                    Assertions.assertEquals("testuser2@example.com", foundUser.getUserEmail());
                })
                .verifyComplete();
    }

    @Test
    public void updateUserTest() {
        // 테스트 데이터 생성
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setUserId("testuser3");
        userInfoDto.setUserName("테스트 사용자3");
        userInfoDto.setUserEmail("testuser3@example.com");
        userInfoDto.setUserPassword("password123");

        // 사용자 생성 후 정보 수정 테스트
        Mono<UserInfoDto> updatedUserMono = userService.createUser(userInfoDto)
                .flatMap(createdUser -> {
                    UserInfoDto updateDto = new UserInfoDto();
                    updateDto.setId(createdUser.getId());
                    updateDto.setUserName("수정된 사용자3");
                    updateDto.setUserEmail("updated3@example.com");
                    updateDto.setUserPassword("newpassword123");
                    return userService.updateUser(updateDto);
                });

        StepVerifier.create(updatedUserMono)
                .assertNext(updatedUser -> {
                    Assertions.assertEquals("testuser3", updatedUser.getUserId()); // userId는 변경되지 않아야 함
                    Assertions.assertEquals("수정된 사용자3", updatedUser.getUserName());
                    Assertions.assertEquals("updated3@example.com", updatedUser.getUserEmail());
                })
                .verifyComplete();
    }

    @Test
    public void addAndRemoveUserAuthTest() {
        // 테스트 데이터 생성
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setUserId("testuser4");
        userInfoDto.setUserName("테스트 사용자4");
        userInfoDto.setUserEmail("testuser4@example.com");
        userInfoDto.setUserPassword("password123");

        // 사용자 생성
        Mono<UserInfoDto> userWithAuthsMono = userService.createUser(userInfoDto)
                // ADMIN 권한 추가
                .flatMap(createdUser -> {
                    UserAuthDto adminAuth = new UserAuthDto();
                    adminAuth.setUserRole("ADMIN");
                    return userService.addUserAuth(createdUser.getId(), adminAuth);
                })
                // MANAGER 권한 추가
                .flatMap(userWithAdminAuth -> {
                    UserAuthDto managerAuth = new UserAuthDto();
                    managerAuth.setUserRole("MANAGER");
                    return userService.addUserAuth(userWithAdminAuth.getId(), managerAuth);
                })
                // ADMIN 권한 제거
                .flatMap(userWithManyAuths -> {
                    return userService.removeUserAuth(userWithManyAuths.getId(), "ADMIN");
                });

        StepVerifier.create(userWithAuthsMono)
                .assertNext(userWithAuths -> {
                    // 권한 확인 (USER, MANAGER 두 개 남아있어야 함)
                    List<UserAuthDto> auths = userWithAuths.getUserAuths();
                    Assertions.assertEquals(2, auths.size());

                    boolean hasUserRole = false;
                    boolean hasManagerRole = false;
                    boolean hasAdminRole = false;

                    for (UserAuthDto auth : auths) {
                        if ("USER".equals(auth.getUserRole()))
                            hasUserRole = true;
                        if ("MANAGER".equals(auth.getUserRole()))
                            hasManagerRole = true;
                        if ("ADMIN".equals(auth.getUserRole()))
                            hasAdminRole = true;
                    }

                    Assertions.assertTrue(hasUserRole, "USER 권한이 있어야 합니다");
                    Assertions.assertTrue(hasManagerRole, "MANAGER 권한이 있어야 합니다");
                    Assertions.assertTrue(!hasAdminRole, "ADMIN 권한은 제거되어야 합니다");
                })
                .verifyComplete();
    }

}
