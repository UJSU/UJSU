package ujsu;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
		http.authorizeHttpRequests(authz -> authz.requestMatchers("/sign-up", "/fragments/*", "/error", "/css/**", "/js/**", "/images/**", "/h2-console")
				.permitAll().anyRequest().authenticated())
				.formLogin(form -> form.loginPage("/sign-in").loginProcessingUrl("/perform-sign-in")
						.defaultSuccessUrl("/vacancy", true).failureHandler(customAuthenticationFailureHandler).permitAll())
				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/sign-in?logout=true").permitAll());
		return http.build();
	}
}