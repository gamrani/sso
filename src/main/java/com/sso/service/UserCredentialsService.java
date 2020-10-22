package com.sso.service;

import com.sso.builder.KeycloakBuilder;
import com.sso.dto.UserLoginDTO;
import com.sso.dto.UserRegistrationDTO;
import com.sso.mapper.UserCredentialsMapper;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserCredentialsService {

    private final KeycloakBuilder builder;
    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${credentials.keycloak.sso.client}")
    private String clientId;
    @Value("${credentials.keycloak.sso.secret}")
    private String clientSecret;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCredentialsService.class);

    @Autowired
    public UserCredentialsService(KeycloakBuilder builder) {
        this.builder = builder;
    }

    public ResponseEntity<?> signin(UserLoginDTO dto) {

        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", clientSecret);
        clientCredentials.put("grant_type","password");

        Configuration configuration = new Configuration(authServerUrl, realm, clientId, clientCredentials, null);
        AuthzClient authzClient = AuthzClient.create(configuration);

        AccessTokenResponse response = authzClient.obtainAccessToken(dto.getUsername(), dto.getPassword());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> registration(UserRegistrationDTO dto) throws Exception {
        UsersResource usersResource = builder.getUsersResource();

        UserRepresentation user = UserCredentialsMapper.mapToUserRepresentation(dto);

        Response response = usersResource.create(user);

        if(response != null){
            LOGGER.debug("User creation response : {} - {} - {}",
                    response.getStatus(),
                    response.getStatusInfo(),
                    response.getLocation());

        addUserRoles(user.getEmail(), Collections.singletonList("USER"));

        dto.setStatusCode(response.getStatus());
        dto.setStatus(response.getStatusInfo().toString());
        }

        return ResponseEntity.ok(dto);
    }


    private void addUserRoles(String userName, List<String> roles) throws Exception {
        LOGGER.debug("Add Role to User {}", userName);
        UserResource userResource = findUserResourceByUsername(userName);
        List<RoleRepresentation> availableRoles = userResource.roles().realmLevel().listAvailable();
        validateRole(roles, availableRoles);
        List<RoleRepresentation> rolesToAdd =
                availableRoles.stream()
                        .filter(a -> roles.contains(a.getName()))
                        .collect(Collectors.toList());
        userResource.roles().realmLevel().add(rolesToAdd);
    }

    private void validateRole(List<String> requestedRoles, List<RoleRepresentation> existingRoles)
            throws Exception {
        List<String> mappedExistingRoles =
                existingRoles.stream().map(RoleRepresentation::getName).collect(Collectors.toList());
        boolean roleDoesntExist =
                requestedRoles.stream().anyMatch(r -> !mappedExistingRoles.contains(r));
        if (roleDoesntExist) {
            throw new Exception("user.role.doesnt.exist");
        }
    }

    private UserResource findUserResourceByUsername(String userName) throws Exception {
        UsersResource usersRessource = builder.getUsersResource();
        UserRepresentation userRepresentation =
                usersRessource.search(userName).stream()
                        .findFirst()
                        .orElseThrow(() -> new Exception("username.doesnt.exit"));
        return usersRessource.get(userRepresentation.getId());
    }
}
