package ujsu;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Включаем CSRF с cookie-based токенами (для работы Thymeleaf)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**")  // API работает без CSRF
            )
            
            .authorizeHttpRequests(authz -> authz
                // API доступно без аутентификации
                .requestMatchers("/api/**").permitAll()
                
                // Статические ресурсы и страницы без аутентификации
                .requestMatchers(
                    "/sign-up", 
                    "/sign-in",
                    "/fragments/**", 
                    "/error", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/h2-console/**"
                ).permitAll()
                
                // Все остальное требует аутентификации
                .anyRequest().authenticated()
            )
            
            // Настройка формы входа
            .formLogin(form -> form
                .loginPage("/sign-in")
                .loginProcessingUrl("/perform-sign-in")
                .defaultSuccessUrl("/vacancies/all", true)
                .failureHandler(customAuthenticationFailureHandler)
                .permitAll()
            )
            
            // Настройка выхода
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/sign-in?logout=true")
                .permitAll()
            );
        
        return http.build();
    }
}