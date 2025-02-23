package com.dkds.payment_processor.api_gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthFilter filter;

    @Autowired
    public GatewayConfig(AuthFilter filter) {
        this.filter = filter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("service-a", r -> r.path("/service-a/**")
                        .filters(f -> f.filter(filter))
                        .uri("http://192.168.39.31:30082"))
                .build();
    }

}