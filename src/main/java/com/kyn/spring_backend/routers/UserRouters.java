package com.kyn.spring_backend.routers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.kyn.spring_backend.modules.user.handler.UserHandler;

@Configuration
public class UserRouters {

    private final UserHandler userHandler;

    public UserRouters(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> userRoutes() {
        return RouterFunctions.route()
                .POST("/users/create", userHandler::createUser)
                .build();
    }

}
