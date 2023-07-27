package com.tinqin.bff.api.operations.item.getbytag;

import com.tinqin.bff.api.operations.base.OperationResponse;
import com.tinqin.zoostore.api.operations.item.getbytag.MultimediaGetResponse;
import com.tinqin.zoostore.api.operations.item.getbytag.TagGetResponse;
import com.tinqin.zoostore.api.operations.item.getbytag.VendorGetResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
public class ItemGetByTagWithPriceAndQuantityDataResponse implements OperationResponse {

    private String id;

    private String title;

    private String description;

    private VendorGetResponse vendor;

    private Set<MultimediaGetResponse> multimedia;

    private Set<TagGetResponse> tags;

    private BigDecimal price;

    private Integer quantity;
}
