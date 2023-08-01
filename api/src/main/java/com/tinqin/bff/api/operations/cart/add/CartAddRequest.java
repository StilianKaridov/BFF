package com.tinqin.bff.api.operations.cart.add;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tinqin.bff.api.operations.base.OperationRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CartAddRequest implements OperationRequest {

    @JsonIgnore
    private String username;

    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "Invalid UUID format!"
    )
    @NotBlank(message = "Item id is required.")
    private String itemId;

    @Min(value = 1, message = "Quantity must be a positive number.")
    @NotNull(message = "Quantity is required.")
    private Integer quantity;
}
