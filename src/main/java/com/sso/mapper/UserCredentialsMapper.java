package com.sso.mapper;

import com.sso.dto.UserRegistrationDTO;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;

public class UserCredentialsMapper {

    public static UserRepresentation mapToUserRepresentation(UserRegistrationDTO dto) {
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
}
