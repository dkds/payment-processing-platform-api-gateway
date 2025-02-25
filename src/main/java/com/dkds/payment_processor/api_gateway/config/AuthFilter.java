package com.dkds.payment_processor.api_gateway.config;

import com.dkds.payment_processor.api_gateway.util.JwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@NoArgsConstructor
public class AuthFilter implements GatewayFilterFactory<AuthFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    //    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = resolveToken(exchange);
        JWTClaimsSet jwtClaimsSet = jwtUtil.validateToken(token);
        if (token != null) {
            // Set authentication details in security context
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private String resolveToken(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("gateway filter name " + config.getName());
            String token = resolveToken(exchange);
            JWTClaimsSet jwtClaimsSet = jwtUtil.validateToken(token);
            return chain.filter(exchange);
        };
    }

    @Override
    public Config newConfig() {
        return new Config("AuthFilter");
    }

    @Override
    public Class<Config> getConfigClass() {
        return AuthFilter.Config.class;
    }

    @Data
    @AllArgsConstructor
    public static class Config {
        private String name;
    }
}
