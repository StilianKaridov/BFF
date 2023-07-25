package com.tinqin.bff.rest.controller;

import com.tinqin.bff.api.operations.item.ItemGetByIdOperation;
import com.tinqin.bff.api.operations.item.ItemRequest;
import com.tinqin.bff.api.operations.item.ItemResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {

    private final ItemGetByIdOperation itemGetByIdOperation;

    @Autowired
    public ItemController(ItemGetByIdOperation itemGetByIdOperation) {
        this.itemGetByIdOperation = itemGetByIdOperation;
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
