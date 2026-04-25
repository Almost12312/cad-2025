package ru.bsuedu.cad.lab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity(debug = true)
public class ConfigSecurity {

    /**
     * Цепочка фильтров для REST API (/api/**) — Basic Authentication.
     * Порядок 1, чтобы обрабатывалась раньше основной цепочки.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/orders").hasAnyRole("USER", "MANAGER")
                        .requestMatchers("/api/orders/**").hasAnyRole("USER", "MANAGER")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Основная цепочка фильтров для веб-интерфейса — Form Login.
     * user  — только просмотр заказов (GET /orders).
     * manager — все операции с заказами.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/logout").permitAll()
                        // manager — полный доступ к заказам
                        .requestMatchers("/orders/new", "/orders/*/edit", "/orders/*/delete").hasRole("MANAGER")
                        .requestMatchers("/orders/**").hasAnyRole("USER", "MANAGER")
                        .requestMatchers("/orders").hasAnyRole("USER", "MANAGER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/orders", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * In-memory пользователи:
     * - user / 1234    — роль USER   (только просмотр заказов)
     * - manager / 1234 — роль MANAGER (все операции с заказами)
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("user")
                        .password("{noop}1234")
                        .roles("USER")
                        .build(),
                User.withUsername("manager")
                        .password("{noop}1234")
                        .roles("MANAGER")
                        .build()
        );
    }
}
