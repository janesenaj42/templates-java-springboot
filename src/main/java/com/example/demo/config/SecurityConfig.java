package com.example.demo.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration
@EnableRSocketSecurity
@EnableWebFluxSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    public static final String NORMAL_USER = "user";
    public static final String ADMIN_USER = "administrator";

    // ✅ HTTP Security
    @Bean
    public SecurityWebFilterChain httpSecurity(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/public").permitAll()
                        .pathMatchers("/admin/**").hasRole(ADMIN_USER)
                        .pathMatchers("/user/**").hasRole(NORMAL_USER)
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
                .build();
    }

    // ✅ RSocket Security
    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(
            RSocketSecurity rsocket,
            JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager// ← auto-configured
    ) {
        // `.anyExchange().permitAll()' allows non-payload-level exchanges like setup frames, authentication metadata, etc., so the client can actually connect and be authenticated.
        rsocket.authorizePayload(authorize ->
                        authorize
                                .route("/normal.*").hasRole(NORMAL_USER)
                                .route("/admin.*").hasRole(ADMIN_USER)
                                .anyRequest().authenticated()
                                .anyExchange().permitAll()
                )
                .jwt(jwtSpec -> jwtSpec.authenticationManager(jwtReactiveAuthenticationManager));
        return rsocket.build();
    }

    @Bean
    public JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(ReactiveJwtDecoder decoder) {
        log.debug("----jwtReactiveAuthenticationManager()");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
            return Stream.concat(
                    jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                    extractRoles(jwt).stream()
            ).collect(Collectors.toSet());
        });
        JwtReactiveAuthenticationManager manager = new JwtReactiveAuthenticationManager(decoder);
        manager.setJwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(converter));
        return manager;
    }

    private Collection<? extends GrantedAuthority> extractRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        Map<String, Object> resource;
        Collection<String> resourceRoles;
        if (resourceAccess == null
                || (resource = (Map<String, Object>) resourceAccess.get(resourceId)) == null
                || (resourceRoles = (Collection<String>) resource.get("roles")) == null) {
            return Collections.emptySet();
        }
        return resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}
