package com.tinqin.bff.api.operations.cart.add;

import com.tinqin.bff.api.operations.base.OperationResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
public class CartAddResponse implements OperationResponse {

    private String userId;

    private String itemId;

    private Integer quantity;

    private BigDecimal price;
}
