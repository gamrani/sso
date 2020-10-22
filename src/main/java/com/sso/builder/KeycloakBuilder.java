package com.sso.builder;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakBuilder {
    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${credentials.keycloak.client}")
    private String clientId;
    @Value("${credentials.keycloak.secret}")
    private String clientSecret;

    public org.keycloak.admin.client.KeycloakBuilder build() {
        return org.keycloak.admin.client.KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS);
    }

    public UsersResource getUsersResource() {
        return build().build().realm(realm).users();
    }

    public Keycloak getByUsernameAndPassword(String username, String password) {
        return build()
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }
}
