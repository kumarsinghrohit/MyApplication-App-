package com.profile.configuration.security;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

/**
 * The configuration class for configuring application security for the Profile application.
 */
@KeycloakConfiguration
@Order(1)
public class ProfileSecurityConfig extends ApplicationAuthConfiguration {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
                // .antMatchers("/apps/**").hasRole("user")
                .anyRequest().permitAll().and()
                .csrf().disable();
    }
}
