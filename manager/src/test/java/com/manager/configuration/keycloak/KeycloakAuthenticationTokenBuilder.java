package com.manager.configuration.keycloak;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.mockito.internal.util.collections.Sets;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Builder with test default values for {@link KeycloakAuthenticationToken}
 */
public class KeycloakAuthenticationTokenBuilder {
    private final AccessToken accessToken = new AccessToken();
    protected final Set<GrantedAuthority> authorities = new HashSet<>();

    private KeycloakDeployment keycloakDeployment = null;
    private IDToken idToken = null;
    private String tokenString = "test.keycloak.token";
    private String idTokenString = null;
    private String refreshTokenString = null;
    protected boolean isInteractive = false;

    public KeycloakAuthenticationTokenBuilder() {
        setName("user");
        this.accessToken.setRealmAccess(new AccessToken.Access());
        this.setRoles("offline_access", "uma_authorization");

    }

    public void keycloakDeployment(KeycloakDeployment keycloakDeployment) {
        this.keycloakDeployment = keycloakDeployment;
    }

    public void setName(String name) {
        this.accessToken.setPreferredUsername(name);
    }

    public void setEmail(String email) {
        this.accessToken.setEmail(email);
    }

    public void setRoles(String... roles) {
        this.accessToken.getRealmAccess().roles(Sets.newSet(roles));
        this.authorities(
                Stream.of(roles).map(r -> "ROLE_" + r).map(SimpleGrantedAuthority::new).collect(Collectors.toSet()));
    }

    public void isIntercative(boolean isInteractive) {
        this.isInteractive = isInteractive;
    }

    public void authorities(Collection<GrantedAuthority> authorities) {
        this.authorities.clear();
        this.authorities.addAll(authorities);
    }

    public KeycloakAuthenticationToken build() {
        final RefreshableKeycloakSecurityContext securityContext = new RefreshableKeycloakSecurityContext(
                keycloakDeployment, null, tokenString, accessToken, idTokenString, idToken, refreshTokenString);

        final Principal principal = new KeycloakPrincipal<>(accessToken.getPreferredUsername(), securityContext);

        final KeycloakAccount account = new SimpleKeycloakAccount(principal, accessToken.getRealmAccess().getRoles(),
                securityContext);

        return new KeycloakAuthenticationToken(account, isInteractive, authorities);
    }
}
