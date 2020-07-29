package com.gildedroses.inventory.operations.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import com.gildedroses.inventory.operations.security.filter.ApiKeySecurityFilter;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

	@Value("${app.http.auth-token-header-name}")
    private String principalRequestHeader;

    @Value("${app.http.auth-token-value}")
    private String principalRequestValue;

    @Override
    protected void configure(HttpSecurity http) throws Exception{
    	ApiKeySecurityFilter filter = new ApiKeySecurityFilter(principalRequestHeader);
        filter.setAuthenticationManager(new AuthenticationManager() {
			
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String principal = (String) authentication.getPrincipal();
                System.out.println("here " + principal + " saved " + principalRequestValue);
                if (!principalRequestValue.equals(principal)) {
                    throw new BadCredentialsException("The API key is invalid or absent.");
                }
                authentication.setAuthenticated(true);
                return authentication;
            }
        });
        
    	// Can enable other mechanisms for csrf like hashed csrf cookie and a request header in each request.
        http.addFilterBefore(filter, AbstractPreAuthenticatedProcessingFilter.class)
		    .authorizeRequests(authorize -> {
				try {
					authorize
					.mvcMatchers("/buy/**")
					.authenticated()
					.and()
					.httpBasic();
				} catch (Exception e) {
					throw new AuthenticationCredentialsNotFoundException("Auth failed");
				}
		    }).cors().and().csrf().disable().logout().deleteCookies("JSESSIONID");
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("tariq").password(passwordEncoder().encode("gilded-roses"))
				.authorities("ROLE_USER");
	}
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
