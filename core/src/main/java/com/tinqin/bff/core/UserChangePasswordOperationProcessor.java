package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.user.changepassword.UserChangePasswordOperation;
import com.tinqin.bff.api.operations.user.changepassword.UserChangePasswordRequest;
import com.tinqin.bff.api.operations.user.changepassword.UserChangePasswordResponse;
import com.tinqin.bff.core.exception.NoSuchUserException;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserChangePasswordOperationProcessor implements UserChangePasswordOperation {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserChangePasswordOperationProcessor(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserChangePasswordResponse process(UserChangePasswordRequest input) {
        User user = this.userRepository.findByEmail(input.getEmail())
                .orElseThrow(NoSuchUserException::new);

        String hashedPassword = this.passwordEncoder.encode(input.getPassword());

        User userWithChangedPassword = User
                .builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(hashedPassword)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();

        this.userRepository.save(userWithChangedPassword);

        return UserChangePasswordResponse
                .builder()
                .email(userWithChangedPassword.getEmail())
                .firstName(userWithChangedPassword.getFirstName())
                .lastName(userWithChangedPassword.getLastName())
                .phoneNumber(userWithChangedPassword.getPhoneNumber())
                .build();
    }
}
