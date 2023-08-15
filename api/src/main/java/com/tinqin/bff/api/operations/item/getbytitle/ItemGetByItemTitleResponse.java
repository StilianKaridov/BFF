package com.tinqin.bff.api.operations.item.getbytitle;

import com.tinqin.bff.api.operations.base.OperationResponse;
import com.tinqin.bff.api.operations.item.getbytag.ItemWithPriceAndQuantityDataResponse;
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
public class ItemGetByItemTitleResponse implements OperationResponse {
    private Integer page;
    private Integer limit;
    private Long totalItems;
    private List<ItemWithPriceAndQuantityDataResponse> items;
}
