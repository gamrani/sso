package com.sso.service;

import com.sso.builder.KeycloakBuilder;
import com.sso.dto.UserRegistrationDTO;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final KeycloakBuilder builder;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(KeycloakBuilder builder) {
        this.builder = builder;
    }

    public ResponseEntity<?> registration(UserRegistrationDTO dto) throws Exception {
        UsersResource usersResource = builder.getUsersResource();

        UserRepresentation user = getUserRepresentation(dto);

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

    private UserRepresentation getUserRepresentation(UserRegistrationDTO dto) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(dto.getFirstName()+dto.getLastName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(dto.getPassword());
        user.setCredentials(Collections.singletonList(passwordCred));
        return user;
    }

    public void addUserRoles(String userName, List<String> roles) throws Exception {
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
        Boolean roleDoesntExist =
                requestedRoles.stream().anyMatch(r -> !mappedExistingRoles.contains(r));
        // TODO this is not a BusinessException
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

    private CredentialRepresentation generateCredential(String password) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        return passwordCred;
    }
}
