package com.fiap.sprint3sqlserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // sem CSRF na API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**", "/styles.css").permitAll()
                        .requestMatchers("/login").permitAll()        // login sem autenticar
                        .requestMatchers("/api/**").authenticated()   // API protegida
                        .requestMatchers("/", "/index", "/index.html").authenticated() // index só depois do login
                        .anyRequest().authenticated()
                )
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .formLogin(form -> form
                        .loginPage("/login")                 // página de login
                        .loginProcessingUrl("/login")        // POST do formulário
                        .defaultSuccessUrl("/index", true)   // redireciona após sucesso
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder pe) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(pe.encode("admin")).roles("ADMIN").build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
