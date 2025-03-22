package com.kyn.spring_backend.modules.product.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.kyn.spring_backend.modules.product.dto.ProductBasDto;
import com.kyn.spring_backend.modules.product.service.ProductService;

import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

    @Autowired
    private ProductService productService;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok().body(productService.findAll(), ProductBasDto.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        return ServerResponse.ok().body(productService.findById(request.pathVariable("id")), ProductBasDto.class);
    }

    public Mono<ServerResponse> findbyProductPaging(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        return ServerResponse.ok().body(productService.findbyProductPaging(page, size), ProductBasDto.class);
    }
}
