package com.manager.configuration.keycloak;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomKeycloackAuth.Factory.class)
public @interface WithCustomKeycloackAuth {

    @AliasFor("roles")
    String[] value() default { "admin" };

    @AliasFor("value")
    String[] roles() default { "admin" };

    String email() default "test@store.com";

    String name() default "test_user";

    boolean isInteractive() default true;

    public final class Factory implements WithSecurityContextFactory<WithCustomKeycloackAuth> {

        private final KeycloakAuthenticationTokenBuilder builder;

        public Factory() {
            this.builder = new KeycloakAuthenticationTokenBuilder();
        }

        @Autowired(required = false)
        public void setKeycloakDeployment(KeycloakDeployment keycloakDeployment) {
            this.builder.keycloakDeployment(keycloakDeployment);
        }

        @Override
        public SecurityContext createSecurityContext(WithCustomKeycloackAuth annotation) {
            final SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication(annotation));

            return context;
        }

        public KeycloakAuthenticationToken authentication(WithCustomKeycloackAuth annotation) {
            builder.setRoles(annotation.roles());
            builder.setName(annotation.name());
            builder.isIntercative(annotation.isInteractive());
            builder.setEmail(annotation.email());
            return builder.build();
        }
    }
}
