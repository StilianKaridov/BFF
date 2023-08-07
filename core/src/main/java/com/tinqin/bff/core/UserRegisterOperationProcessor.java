package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.user.register.UserRegisterOperation;
import com.tinqin.bff.api.operations.user.register.UserRegisterRequest;
import com.tinqin.bff.api.operations.user.register.UserRegisterResponse;
import com.tinqin.bff.core.exception.ExistingPhoneNumberException;
import com.tinqin.bff.core.exception.UserExistsException;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.entity.enums.Role;
import com.tinqin.bff.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegisterOperationProcessor implements UserRegisterOperation {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegisterOperationProcessor(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserRegisterResponse process(UserRegisterRequest input) {
        this.userRepository
                .findByEmail(input.getEmail())
                .ifPresent(e -> {
                    throw new UserExistsException();
                });

        this.userRepository
                .findByPhoneNumber(input.getPhoneNumber())
                .ifPresent(e -> {
                    throw new ExistingPhoneNumberException();
                });

        Role role = this.userRepository.count() == 0 ? Role.ADMIN : Role.USER;

        String email = input.getEmail();
        String hashedPassword = this.passwordEncoder.encode(input.getPassword());
        String firstName = input.getFirstName();
        String lastName = input.getLastName();
        String phoneNumber = input.getPhoneNumber();

        User user = User
                .builder()
                .email(email)
                .password(hashedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .role(role)
                .build();

        this.userRepository.save(user);

        return UserRegisterResponse
                .builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .build();
    }
}
