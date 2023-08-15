package com.tinqin.bff.rest.controller;

import com.tinqin.bff.api.operations.item.getbyid.ItemGetByIdOperation;
import com.tinqin.bff.api.operations.item.getbyid.ItemRequest;
import com.tinqin.bff.api.operations.item.getbyid.ItemResponse;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagOperation;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagRequest;
import com.tinqin.bff.api.operations.item.getbytag.ItemWithPriceAndQuantityDataResponse;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagWithPriceAndQuantityResponse;
import com.tinqin.bff.api.operations.item.getbytitle.ItemGetByItemTitleRequest;
import com.tinqin.bff.api.operations.item.getbytitle.ItemGetByItemTitleResponse;
import com.tinqin.bff.api.operations.item.getbytitle.ItemGetByTitleOperation;
import com.tinqin.bff.core.annotations.GenerateRestExport;
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
    private final ItemGetByTitleOperation itemGetByTitleOperation;

    @Autowired
    public ItemController(ItemGetByIdOperation itemGetByIdOperation, ItemGetByTagOperation itemGetByTagOperation, ItemGetByTitleOperation itemGetByTitleOperation) {
        this.itemGetByIdOperation = itemGetByIdOperation;
        this.itemGetByTagOperation = itemGetByTagOperation;
        this.itemGetByTitleOperation = itemGetByTitleOperation;
    }

    @GenerateRestExport
    @GetMapping("/byTag")
    public ResponseEntity<ItemGetByTagWithPriceAndQuantityResponse> getItemsByTag(
            @RequestParam @NotBlank(message = "Title is required.") String title,
            @RequestParam @Min(value = 0, message = "Page number must be greater than or equal to zero.") Integer pageNumber,
            @RequestParam @Min(value = 1, message = "Page size must be positive number.") Integer pageSize
    ) {

        ItemGetByTagRequest itemRequest = ItemGetByTagRequest
                .builder()
                .title(title)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();

        ItemGetByTagWithPriceAndQuantityResponse response = this.itemGetByTagOperation.process(itemRequest);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/byItem")
    public ResponseEntity<ItemGetByItemTitleResponse> getItemsByTitle(
            @RequestParam @NotBlank(message = "Title is required.") String title,
            @RequestParam @Min(value = 0, message = "Page number must be greater than or equal to zero.") Integer pageNumber,
            @RequestParam @Min(value = 1, message = "Page size must be positive number.") Integer pageSize
    ) {

        ItemGetByItemTitleRequest itemRequest = ItemGetByItemTitleRequest
                .builder()
                .title(title)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();

        ItemGetByItemTitleResponse response = this.itemGetByTitleOperation.process(itemRequest);

        return ResponseEntity.ok(response);
    }

    @GenerateRestExport
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
