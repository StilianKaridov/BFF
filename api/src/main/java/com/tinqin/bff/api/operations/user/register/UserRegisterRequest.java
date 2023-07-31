package com.tinqin.bff.api.operations.user.register;

import com.tinqin.bff.api.operations.base.OperationRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
public class UserRegisterRequest implements OperationRequest {

    @NotBlank(message = "First name is required.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    private String lastName;

    @Email(message = "Invalid email format.", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
    @NotBlank(message = "Email is required.")
    private String email;

    @NotBlank(message = "Password is required.")
    private String password;

    @Pattern(
            regexp = "(\\+)?(359|0)8[789]\\d{1}(|-| )\\d{3}(|-| )\\d{3}",
            message = "Invalid phone number format!"
    )
    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;
}
