package com.tinqin.bff.api.operations.cart.sell;

import com.tinqin.bff.api.operations.base.OperationResponse;
import com.tinqin.storage.api.operations.sell.ItemSellDataResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
public class CartSellResponse implements OperationResponse {

    private List<ItemSellDataResponse> boughtItems;
}
