package com.kyn.spring_backend.modules.user.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.kyn.spring_backend.modules.user.dto.UserInfoDto;
import com.kyn.spring_backend.modules.user.service.UserService;

import reactor.core.publisher.Mono;

@Component
public class UserHandler {

    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(UserInfoDto.class)
                .flatMap(userService::createUser)
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(UserInfoDto.class)
                .flatMap(userService::login)
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }
}
