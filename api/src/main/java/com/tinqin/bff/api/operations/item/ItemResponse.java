package com.tinqin.bff.api.operations.item;

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
public class ItemResponse implements OperationResponse {

    private String itemId;

    private String title;

    private BigDecimal price;

    private Integer quantity;
}
