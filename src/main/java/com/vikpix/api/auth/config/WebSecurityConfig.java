package com.vikpix.api.auth.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.vikpix.api.auth.jwt.AuthEntryPointJwt;

import jakarta.servlet.http.Cookie;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
  private final AuthEntryPointJwt unauthorizedHandler;

  public WebSecurityConfig(AuthEntryPointJwt unauthorizedHandler) {
    this.unauthorizedHandler = unauthorizedHandler;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/api/test/**").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users", "/api/users/").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/auth/oauth/google").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/auth/oauth/google/callback").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/2fa/verify-login").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/request-password-reset").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/widgets/qrcode/{token}").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .bearerTokenResolver(bearerTokenResolver())
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::extractRealmRoles);
    return converter;
  }

  private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");

    if (realmAccess == null || realmAccess.get("roles") == null) {
      return List.of();
    }

    @SuppressWarnings("unchecked")
    Collection<String> roles = (Collection<String>) realmAccess.get("roles");

    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toSet());
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(List.of("http://localhost:5173"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public BearerTokenResolver bearerTokenResolver() {
    return request -> {
      String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

      if (authorization != null && authorization.startsWith("Bearer ")) {
        return authorization.substring(7);
      }

      Cookie[] cookies = request.getCookies();

      if (cookies == null) {
        return null;
      }

      for (Cookie cookie : cookies) {
        if ("access_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }

      return null;
    };
  }
}