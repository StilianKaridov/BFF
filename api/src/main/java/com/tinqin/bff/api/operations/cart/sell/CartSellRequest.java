package com.tinqin.bff.api.operations.cart.sell;

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
public class CartSellRequest implements OperationRequest {

    @JsonIgnore
    private String email;

    @NotBlank(message = "Credit cart number is required.")
    private String creditCardNumber;
}
