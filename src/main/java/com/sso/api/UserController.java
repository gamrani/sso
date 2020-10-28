package com.sso.api;

import com.sso.dto.UserLoginDTO;
import com.sso.dto.UserRegistrationDTO;
import com.sso.service.UserCredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserCredentialsService service;

    @Autowired
    public UserController(UserCredentialsService service) {
        this.service = service;
    }

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody UserRegistrationDTO userRegistrationDTO) throws Exception {
        return service.registration(userRegistrationDTO);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody UserLoginDTO dto) throws Exception {
        return service.signIn(dto);
    }

    @GetMapping("/unprotected-data")
    public String unprotected(){
        return "hello from unprotected endpoint";
    }

    @GetMapping("/protected-data")
    public String protectedEndpoint(){
        return "hello from protected endpoint";

    }
}
