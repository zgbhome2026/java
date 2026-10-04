package com.example.shopping;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(JdbcTemplate db) {
        return username -> db.query(
                "SELECT username, password_hash FROM shop_users WHERE username = ?",
                (rs, row) -> User.withUsername(rs.getString("username"))
                    .password(rs.getString("password_hash")).roles("USER").build(), username)
            .stream().findFirst().orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/login", "/error", "/style.css", "/actuator/health/**").permitAll()
                .anyRequest().authenticated())
            .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/", false).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
            .build();
    }
}
