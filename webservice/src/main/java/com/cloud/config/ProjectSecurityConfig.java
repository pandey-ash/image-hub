package com.cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ProjectSecurityConfig extends WebSecurityConfigurerAdapter
{
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		//.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and() /file/upload /env /property-env
		.authorizeRequests().antMatchers("/v1/user").permitAll()
		.antMatchers("/file/upload").permitAll()
		.antMatchers("/env").permitAll()
		.antMatchers("/property-env").permitAll()
		.antMatchers("/v1/user/self").authenticated().antMatchers("/healthz")
				.permitAll().and().formLogin().and().httpBasic();
		http.csrf().disable();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
}
