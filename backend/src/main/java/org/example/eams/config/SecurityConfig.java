package org.example.eams.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.eams.common.Result;
import org.example.eams.enums.ErrorCode;
import org.example.eams.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
 private final JwtAuthenticationFilter jwtAuthenticationFilter;
 private final ObjectMapper objectMapper;

 @Bean
 public PasswordEncoder passwordEncoder() {
     return new BCryptPasswordEncoder();
 }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    )throws Exception {
       http
               .csrf(AbstractHttpConfigurer::disable)
               .sessionManagement(session ->
                       session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
               )
               .exceptionHandling(exception -> exception
                       .authenticationEntryPoint((request, response, error) ->
                               writeError(response, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED)
                       )
                       .accessDeniedHandler((request, response, error) ->
                               writeError(response, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN)
                       )
               )

               .authorizeHttpRequests(auth -> auth
                       .requestMatchers("/api/auth/login", "/error", "/uploads/**").permitAll()
                       .requestMatchers(
                               "/api/users/**",
                               "/api/dashboard/**",
                               "/api/operation-logs/**"
                       ).hasRole("ADMIN")
                       .anyRequest().authenticated()
               )
               .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

       return http.build();
    }

    private void writeError(
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            ErrorCode errorCode
    ) throws IOException {
        response.setStatus(status.value());                        // 设置 HTTP 状态码
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 返回 JSON 格式
        response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // 编码 UTF-8
        objectMapper.writeValue(
                response.getOutputStream(),
                Result.fail(errorCode)       // 把 Result 对象序列化成 JSON 写入响应体
        );
    }
}
