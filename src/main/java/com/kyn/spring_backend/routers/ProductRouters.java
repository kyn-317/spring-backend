package com.kyn.spring_backend.routers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.kyn.spring_backend.modules.product.handler.ProductHandler;

@Configuration
public class ProductRouters {

    @Autowired
    private ProductHandler productHandler;

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route(RequestPredicates.GET("/products"), this.productHandler::findAll)
                .andRoute(RequestPredicates.GET("/products/paging"), this.productHandler::findbyProductPaging)
                .andRoute(RequestPredicates.GET("/products/{id}"), this.productHandler::findById);
    }
}
