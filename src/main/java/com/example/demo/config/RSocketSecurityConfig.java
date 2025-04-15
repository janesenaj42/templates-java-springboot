package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableRSocketSecurity
public class RSocketSecurityConfig {

    private JwtConverter jwtConverter;

    public RSocketSecurityConfig(JwtConverter jwtConverter) {
        this.jwtConverter = jwtConverter;
    }

    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rsocket, JwtReactiveAuthenticationManager jwtAuthManager) {
        rsocket.authorizePayload(authorize ->
                        authorize
                                .route("normal.*").hasAuthority("normaluser")
                                .route("admin.*").hasAuthority("superuser")
                                .anyRequest().authenticated()
                                .anyExchange().permitAll()
                )
                .jwt(jwt -> jwt.authenticationManager(jwtAuthManager));
        return rsocket.build();
    }

    /**
     * Spring Boot’s auto-configured ReactiveJwtDecoder only kicks in for HTTP security, not for RSocket security by default.
     * @param issuerLocation issuerLocation.
     * @return ReactiveJwtDecoder
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerLocation) {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerLocation);
    }

    @Bean
    public JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(ReactiveJwtDecoder decoder) {
        JwtReactiveAuthenticationManager manager = new JwtReactiveAuthenticationManager(decoder);
        manager.setJwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(this.jwtConverter));
        return manager;
    }
}