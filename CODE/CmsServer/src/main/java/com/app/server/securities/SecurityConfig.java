package com.app.server.securities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig
        extends WebSecurityConfigurerAdapter {
    private AuthenticationFilter authenticationFilter;
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    public void setAuthenticationFilter(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Autowired
    public void setCustomAuthenticationEntryPoint(CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/**").permitAll()
                .antMatchers("/auth/login").permitAll()
                .antMatchers(
//                        "/swagger-ui.html",
                        "/v2/api-docs",
                        "/export/**",
                        "/utility/upload_file_base64",
                        "/webjars/**",
                        "/configuration/ui",
                        "/swagger-resources/**",
                        "/sync/**",
                        "/bravo/**").permitAll()
                .anyRequest().authenticated().and()
                .cors().and()
                .exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}