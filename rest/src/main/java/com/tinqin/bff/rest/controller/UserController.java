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
import com.tinqin.bff.customannotation.annotation.GenerateRestExport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Operation(description = "Registers user with firstName, lastName, email, password, phoneNumber",
            summary = "Registers user.")
    @ApiResponse(responseCode = "200", description = "Successfully registered user.")
    @ApiResponse(responseCode = "400",
            description = "User with that email/phoneNumber already exists.",
            content = {@Content(examples = @ExampleObject(value = "User with that email/phoneNumber already exists."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "First name must not be blank.",
            content = {@Content(examples = @ExampleObject(value = "First name is required."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Last name must not be blank.",
            content = {@Content(examples = @ExampleObject(value = "Last name is required."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Email must not be blank.",
            content = {@Content(examples = @ExampleObject(value = "Email is required."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Invalid email format.",
            content = {@Content(examples = @ExampleObject(value = "Invalid email format."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Password must not be blank.",
            content = {@Content(examples = @ExampleObject(value = "Password is required."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Invalid phone number format.",
            content = {@Content(examples = @ExampleObject(value = "Invalid phone number format!"), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Phone number must not be blank.",
            content = {@Content(examples = @ExampleObject(value = "Phone number is required."), mediaType = "text/html")})
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(
            @RequestBody @Valid UserRegisterRequest userRegisterRequest
    ) {
        UserRegisterResponse registeredUserResponse = this.userRegisterOperation.process(userRegisterRequest);

        return ResponseEntity.status(201).body(registeredUserResponse);
    }

    @Operation(description = "Logins user. Authenticate with email and password.",
            summary = "Logins user.")
    @ApiResponse(responseCode = "200", description = "User logged.")
    @ApiResponse(responseCode = "400",
            description = "Bad credentials.",
            content = {@Content(examples = @ExampleObject(value = "Bad credentials"), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "No such username in the database.",
            content = {@Content(examples = @ExampleObject(value = "User not existing."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Email must not be blank.",
            content = {@Content(examples = @ExampleObject(value = "Email is required."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Invalid email format.",
            content = {@Content(examples = @ExampleObject(value = "Invalid email format."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Password must not be blank.",
            content = {@Content(examples = @ExampleObject(value = "Password is required."), mediaType = "text/html")})
    @GenerateRestExport
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @RequestBody @Valid UserLoginRequest userLoginRequest
    ) {
        UserLoginResponse loggedInUserResponse = this.userLoginOperation.process(userLoginRequest);

        return ResponseEntity.ok(loggedInUserResponse);
    }

    @Operation(description = "Changes the password of the current logged in user.",
            summary = "Change password.")
    @ApiResponse(responseCode = "200", description = "Password changed successfully.")
    @ApiResponse(responseCode = "400",
            description = "Not existing user.",
            content = {@Content(examples = @ExampleObject(value = "This user does not exist."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Password must not be blank.",
            content = {@Content(examples = @ExampleObject(value = "Password is required."), mediaType = "text/html")})
    @ApiResponse(responseCode = "403",
            description = "Invalid JWT.",
            content = {@Content(examples = @ExampleObject(value = ""), mediaType = "text/html")})
    @SecurityRequirement(name = "Bearer Authentication")
    @GenerateRestExport
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
