package user.order_service.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final JwtAuthenticationEntryPoint authenticationEntryPoint;
        private final JwtAccessDeniedHandler accessDeniedHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/v1/auth/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/orders")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/orders/{id}/cancel")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/orders/{id}/pay")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/orders")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/orders/{id}")
                                                .hasAnyRole("USER", "ADMIN")
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}