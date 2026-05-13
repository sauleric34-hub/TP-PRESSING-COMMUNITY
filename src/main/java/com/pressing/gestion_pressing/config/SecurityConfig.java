package com.pressing.gestion_pressing.config;

import com.pressing.gestion_pressing.security.AuthEntryPointJwt;
import com.pressing.gestion_pressing.security.JwtAuthorizationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public JwtAuthorizationFilter authenticationJwtTokenFilter() {
        return new JwtAuthorizationFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/web/login", "/", "/favicon.ico", "/css/**", "/js/**", "/image/**", "/uploads/**").permitAll()
                    .requestMatchers("/web/utilisateurs/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/web/login")
                .loginProcessingUrl("/web/login")
                .defaultSuccessUrl("/web/dashboard", true)
                .failureUrl("/web/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/web/logout")
                .logoutSuccessUrl("/web/login?logout=true")
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
