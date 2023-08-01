package com.tinqin.bff.api.operations.user.changepassword;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tinqin.bff.api.operations.base.OperationRequest;
import jakarta.validation.constraints.NotBlank;
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
public class UserChangePasswordRequest implements OperationRequest {

    @JsonIgnore
    private String email;

    @NotBlank(message = "Password is required.")
    private String password;
}
