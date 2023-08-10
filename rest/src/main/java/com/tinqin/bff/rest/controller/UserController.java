package com.tinqin.bff.rest.controller;

import com.tinqin.bff.api.operations.user.changepassword.UserChangePasswordOperation;
import com.tinqin.bff.api.operations.user.changepassword.UserChangePasswordRequest;
import com.tinqin.bff.api.operations.user.changepassword.UserChangePasswordResponse;
import com.tinqin.bff.api.operations.user.login.UserLoginOperation;
import com.tinqin.bff.api.operations.user.login.UserLoginRequest;
import com.tinqin.bff.api.operations.user.login.UserLoginResponse;
import com.tinqin.bff.api.operations.user.register.UserRegisterOperation;
import com.tinqin.bff.api.operations.user.register.UserRegisterRequest;
import com.tinqin.bff.api.operations.user.register.UserRegisterResponse;
import com.tinqin.bff.core.annotations.RequestInfoToTextFile;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/bff/users")
public class UserController {

    private final UserRegisterOperation userRegisterOperation;
    private final UserLoginOperation userLoginOperation;
    private final UserChangePasswordOperation userChangePasswordOperation;

    @Autowired
    public UserController(UserRegisterOperation userRegisterOperation, UserLoginOperation userLoginOperation, UserChangePasswordOperation userChangePasswordOperation) {
        this.userRegisterOperation = userRegisterOperation;
        this.userLoginOperation = userLoginOperation;
        this.userChangePasswordOperation = userChangePasswordOperation;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(
            @RequestBody @Valid UserRegisterRequest userRegisterRequest
    ) {
        UserRegisterResponse registeredUserResponse = this.userRegisterOperation.process(userRegisterRequest);

        return ResponseEntity.status(201).body(registeredUserResponse);
    }

    @RequestInfoToTextFile
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @RequestBody @Valid UserLoginRequest userLoginRequest
    ) {
        UserLoginResponse loggedInUserResponse = this.userLoginOperation.process(userLoginRequest);

        return ResponseEntity.ok(loggedInUserResponse);
    }

    @RequestInfoToTextFile
    @PutMapping("/changePassword")
    public ResponseEntity<UserChangePasswordResponse> changePassword(
            @RequestBody @Valid UserChangePasswordRequest userChangePasswordRequest,
            Principal principal
    ) {
        UserChangePasswordRequest userRequest = UserChangePasswordRequest
                .builder()
                .email(principal.getName())
                .password(userChangePasswordRequest.getPassword())
                .build();

        UserChangePasswordResponse userWithChangedPassword = this.userChangePasswordOperation.process(userRequest);

        return ResponseEntity.ok(userWithChangedPassword);
    }
}
