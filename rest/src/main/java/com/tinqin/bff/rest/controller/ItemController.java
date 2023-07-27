package com.tinqin.bff.rest.controller;

import com.tinqin.bff.api.operations.item.getbyid.ItemGetByIdOperation;
import com.tinqin.bff.api.operations.item.getbyid.ItemRequest;
import com.tinqin.bff.api.operations.item.getbyid.ItemResponse;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagOperation;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagRequest;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagWithPriceAndQuantityDataResponse;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagWithPriceAndQuantityResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bff/items")
@Validated
public class ItemController {

    private final ItemGetByIdOperation itemGetByIdOperation;
    private final ItemGetByTagOperation itemGetByTagOperation;

    @Autowired
    public ItemController(ItemGetByIdOperation itemGetByIdOperation, ItemGetByTagOperation itemGetByTagOperation) {
        this.itemGetByIdOperation = itemGetByIdOperation;
        this.itemGetByTagOperation = itemGetByTagOperation;
    }

    @GetMapping
    public ResponseEntity<Page<ItemGetByTagWithPriceAndQuantityDataResponse>> getItemsByTag(
            @RequestParam @NotBlank(message = "Title is required.") String title,
            @RequestParam @Min(value = 1, message = "Page number must be positive number.") Integer pageNumber,
            @RequestParam @Min(value = 1, message = "Page size must be positive number.") Integer pageSize
    ) {

        ItemGetByTagRequest itemRequest = ItemGetByTagRequest
                .builder()
                .title(title)
                .pageNumber(pageNumber - 1)
                .pageSize(pageSize)
                .build();

        ItemGetByTagWithPriceAndQuantityResponse response = this.itemGetByTagOperation.process(itemRequest);

        return ResponseEntity.ok(new PageImpl<>(response.getItems()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable String id) {
        ItemRequest itemRequest = ItemRequest
                .builder()
                .id(id)
                .build();

        ItemResponse response = this.itemGetByIdOperation.process(itemRequest);

        return ResponseEntity.ok(response);
    }
}
