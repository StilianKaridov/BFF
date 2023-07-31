package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.user.login.UserLoginOperation;
import com.tinqin.bff.api.operations.user.login.UserLoginRequest;
import com.tinqin.bff.api.operations.user.login.UserLoginResponse;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserLoginOperationProcessor implements UserLoginOperation {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Autowired
    public UserLoginOperationProcessor(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public UserLoginResponse process(UserLoginRequest input) {
        try {
            this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Bad credentials.");
        }

        User user = this.userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not existing."));

        String token = this.jwtService.generateToken(user);

        return UserLoginResponse
                .builder()
                .token(token)
                .build();
    }
}
