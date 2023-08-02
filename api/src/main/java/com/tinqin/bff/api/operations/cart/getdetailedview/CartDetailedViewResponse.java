package com.tinqin.bff.api.operations.cart.getdetailedview;

import com.tinqin.bff.api.operations.base.OperationResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
public class CartDetailedViewResponse implements OperationResponse {

    private UserCartInfoResponse user;

    private List<CartItemsDetailedViewResponse> items;

    private BigDecimal totalPrice;
}
