package com.ksk.portal.config;

import com.ksk.portal.service.PortalUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider businessAuthenticationProvider(PortalUserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    InMemoryUserDetailsManager adminUsers(PasswordEncoder encoder, @Value("${ADMIN_PASSWORD:}") String password) {
        if (password == null || password.isBlank())
            throw new IllegalStateException("ADMIN_PASSWORD environment variable must be configured before starting the portal.");
        return new InMemoryUserDetailsManager(User.withUsername("KskMSAdmin").password(encoder.encode(password)).roles("ADMIN").build());
    }

    @Bean
    DaoAuthenticationProvider adminAuthenticationProvider(InMemoryUserDetailsManager adminUsers, PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(adminUsers);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, @Qualifier("businessAuthenticationProvider") DaoAuthenticationProvider businessAuthenticationProvider, @Qualifier("adminAuthenticationProvider") DaoAuthenticationProvider adminAuthenticationProvider) throws Exception {
        http.authenticationProvider(adminAuthenticationProvider).authenticationProvider(businessAuthenticationProvider).authorizeHttpRequests(a -> a
                        .requestMatchers("/signup", "/login", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error", "/sample.csv").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/dashboard", "/settings", "/whatsapp-config", "/api/**", "/image-upload").hasRole("BUSINESS").anyRequest().authenticated())
                .formLogin(f -> f.loginPage("/login").successHandler((request, response, authentication) -> {
                    boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    response.sendRedirect(admin ? "/admin" : "/dashboard");
                }).permitAll())
                .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout").invalidateHttpSession(true).deleteCookies("JSESSIONID"))
                .csrf(c -> {
                    CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
                    repo.setHeaderName("X-CSRF-TOKEN");
                    c.csrfTokenRepository(repo);
                });
        return http.build();
    }
}
