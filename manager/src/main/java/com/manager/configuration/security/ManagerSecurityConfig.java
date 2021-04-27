package com.manager.configuration.security;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * The configuration class for configuring application security for the Application.
 */
@KeycloakConfiguration
@Order(1)
public class ManagerSecurityConfig extends ApplicationAuthConfiguration {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
                .antMatchers("/apps/**").hasRole("user")
                .anyRequest().permitAll().and()
                .csrf().disable();
    }
}
