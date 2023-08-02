package com.tinqin.bff.api.operations.cart.getdetailedview;

import com.tinqin.bff.api.operations.base.OperationResponse;
import com.tinqin.zoostore.api.operations.item.getbytag.VendorGetResponse;
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
public class CartItemsDetailedViewResponse implements OperationResponse {

    private String id;

    private String title;

    private VendorGetResponse vendor;

    private BigDecimal price;

    private Integer quantity;
}
