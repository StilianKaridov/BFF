package com.tinqin.bff.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqin.bff.api.operations.user.changepassword.UserChangePasswordRequest;
import com.tinqin.bff.api.operations.user.login.UserLoginRequest;
import com.tinqin.bff.api.operations.user.register.UserRegisterRequest;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.entity.enums.Role;
import com.tinqin.bff.persistence.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User
                .builder()
                .phoneNumber("0895070092")
                .firstName("Test")
                .lastName("Test")
                .email("test@test.com")
                .role(Role.USER)
                .password("test")
                .build();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void test_UserRegister_Successfully() throws Exception {
        UserRegisterRequest userRegisterRequest = UserRegisterRequest
                .builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPassword())
                .build();

        mockMvc.perform(
                        post("/api/bff/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userRegisterRequest))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$['email']").value(user.getEmail()))
                .andExpect(jsonPath("$['firstName']").value(user.getFirstName()))
                .andExpect(jsonPath("$['lastName']").value(user.getLastName()))
                .andExpect(jsonPath("$['phoneNumber']").value(user.getPhoneNumber()));
    }

    @Test
    void test_UserRegister_FailedRequestValidation_Returns_BadRequest() throws Exception {
        UserRegisterRequest userWithInvalidFields = UserRegisterRequest
                .builder()
                .firstName("  ")
                .lastName("  ")
                .email("test@test")
                .phoneNumber("0895070092222")
                .password("  ")
                .build();

        mockMvc.perform(
                        post("/api/bff/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userWithInvalidFields))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void test_UserRegister_ExistingUser_Returns_BadRequest() throws Exception {
        userRepository.save(user);

        UserRegisterRequest existingUser = UserRegisterRequest
                .builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPassword())
                .build();

        mockMvc.perform(
                        post("/api/bff/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(existingUser))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void test_UserLogin_Successfully() throws Exception {
        mockMvc.perform(
                post("/api/bff/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
                        .accept(MediaType.APPLICATION_JSON)
        );

        UserLoginRequest userLoginRequest = UserLoginRequest
                .builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        mockMvc.perform(
                        post("/api/bff/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLoginRequest))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void test_UserLogin_Fail_Returns_Forbidden() throws Exception {
        UserLoginRequest userLoginRequest = UserLoginRequest
                .builder()
                .email("notexisting@abv.bg")
                .password(user.getPassword())
                .build();

        mockMvc.perform(
                        post("/api/bff/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userLoginRequest))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void test_UserChangePassword_Successfully() throws Exception {
        userRepository.save(user);

        UserChangePasswordRequest userRequest = UserChangePasswordRequest
                .builder()
                .password("new_password")
                .build();

        mockMvc.perform(
                        put("/api/bff/users/changePassword")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userRequest))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void test_UserChangePassword_Unsuccessfully_Returns_Forbidden() throws Exception {
        UserChangePasswordRequest userRequest = UserChangePasswordRequest
                .builder()
                .password("new_password")
                .build();

        mockMvc.perform(
                        put("/api/bff/users/changePassword")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userRequest))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }
}