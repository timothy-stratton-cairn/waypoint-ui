package com.cairn.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		MvcRequestMatcher mvc = new MvcRequestMatcher(null,"/");
		MvcRequestMatcher mvc2 = new MvcRequestMatcher(null,"/*.css");
		MvcRequestMatcher mvc3 = new MvcRequestMatcher(null,"/error");
		MvcRequestMatcher mvc4 = new MvcRequestMatcher(null,"/jars/**");
		MvcRequestMatcher mvc5 = new MvcRequestMatcher(null,"/img/**");
		MvcRequestMatcher mvc6 = new MvcRequestMatcher(null,"/bootstrap-5.0.2-dist/**");
		MvcRequestMatcher mvc7 = new MvcRequestMatcher(null,"/css/**");
		MvcRequestMatcher mvc8 = new MvcRequestMatcher(null,"/js/**");
		MvcRequestMatcher mvc9 = new MvcRequestMatcher(null,"/registerUser");
		MvcRequestMatcher mvc10 = new MvcRequestMatcher(null,"/health");
		MvcRequestMatcher mvc11 = new MvcRequestMatcher(null,"/password-reset");
		MvcRequestMatcher mvc12 = new MvcRequestMatcher(null,"/forgotPassword");
		MvcRequestMatcher mvc13 = new MvcRequestMatcher(null,"/resetPassword");
		MvcRequestMatcher mvc14 = new MvcRequestMatcher(null,"/newUserPassword/");
		http
			.authorizeHttpRequests((requests) -> requests
				.requestMatchers(mvc).permitAll()
				.requestMatchers(mvc2).permitAll()
				.requestMatchers(mvc3).permitAll()
				.requestMatchers(mvc4).permitAll()
				.requestMatchers(mvc5).permitAll()
				.requestMatchers(mvc6).permitAll()
				.requestMatchers(mvc7).permitAll()
				.requestMatchers(mvc8).permitAll()
				.requestMatchers(mvc9).permitAll()
				.requestMatchers(mvc10).permitAll()
				.requestMatchers(mvc11).permitAll()
				.requestMatchers(mvc12).permitAll()
				.requestMatchers(mvc13).permitAll()
				.requestMatchers(mvc14).permitAll()
				.anyRequest().authenticated()
			)
			.formLogin((form) -> form
				.loginPage("/login")
				.permitAll()
		        .loginProcessingUrl("/login")
		        .defaultSuccessUrl("/dashboard",true)
		        .failureUrl("/login?error=true")
			)
			.logout((logout) -> logout.permitAll());

		return http.build();
	}
	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}
}