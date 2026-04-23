package com.iesdoctorbalmis.spring.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SeguridadConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:3000"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login", "/webjars/**", "/css/**",
                    "/logo.png", "/favicon.ico", "/images/**", "/js/**",
                    "/swagger-ui/**", "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers("/api/perfil/**").authenticated()
                .requestMatchers("/api/empresa/**").authenticated()
                .requestMatchers("/api/almacen/**").authenticated()
                .requestMatchers("/api/usuarios/*/perfil-transportista").authenticated()
                .requestMatchers("/api/transportistas", "/api/transportistas/**").authenticated()
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/api/rutas/**").hasAnyRole("ADMIN", "GESTOR", "TRANSPORTISTA")
                .requestMatchers("/api/traslados/**").hasAnyRole("ADMIN", "GESTOR", "TRANSPORTISTA", "PRODUCTOR")
                .requestMatchers("/api/recogidas/**").hasAnyRole("ADMIN", "GESTOR", "TRANSPORTISTA", "PRODUCTOR")
                .requestMatchers("/api/centros/**").hasAnyRole("ADMIN", "GESTOR", "PRODUCTOR")
                .requestMatchers("/api/residuos/**").hasAnyRole("ADMIN", "GESTOR", "PRODUCTOR")
                .requestMatchers("/api/direcciones/**").authenticated()
                .requestMatchers("/api/lista-ler/**").authenticated()
                .requestMatchers("/api/estadisticas/**").authenticated()
                .requestMatchers("/api/informes/**").hasAnyRole("ADMIN", "GESTOR")
                .requestMatchers("/api/qr/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }
}