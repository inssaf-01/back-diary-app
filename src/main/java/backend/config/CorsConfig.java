package backend.config;

import jakarta.servlet.http.HttpServletResponse;
import backend.auth.JwtAuthenticationFilter;
import backend.auth.JwtService;
import backend.user.AppUserRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:4200"));

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"));

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"));

        configuration.setExposedHeaders(
                List.of("Authorization"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtService jwtService, AppUserRepository users) throws Exception {

        return http
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, users), UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()))

                .csrf(csrf -> csrf.disable())

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))

                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (
                                request,
                                response,
                                exception) -> response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentification obligatoire")))

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/refresh")
                        .permitAll()

                        .requestMatchers("/api/test")
                        .permitAll()

                        .anyRequest()
                        .authenticated())

                .build();
    }
}