package br.com.alura.AluraFake.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable).sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API sem estado (Stateless)
                .authorizeHttpRequests(req -> {

                    req.requestMatchers(HttpMethod.POST, "/user/new").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/").permitAll();
                    req.requestMatchers(HttpMethod.POST, "/task/new/**").hasRole("INSTRUCTOR");
                    req.requestMatchers(HttpMethod.POST, "/course/new").hasRole("INSTRUCTOR");
                    req.requestMatchers(HttpMethod.POST, "/course/*/publish").hasRole("INSTRUCTOR");
                    req.requestMatchers(HttpMethod.GET, "/instructor/**").hasRole("INSTRUCTOR");

                    req.anyRequest().authenticated();
                }).httpBasic(org.springframework.security.config.Customizer.withDefaults()) // Habilita login via Basic Auth
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}