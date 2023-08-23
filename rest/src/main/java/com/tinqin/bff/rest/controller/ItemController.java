package com.tinqin.bff.rest.controller;

import com.tinqin.bff.api.operations.item.getbyid.ItemGetByIdOperation;
import com.tinqin.bff.api.operations.item.getbyid.ItemRequest;
import com.tinqin.bff.api.operations.item.getbyid.ItemResponse;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagOperation;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagRequest;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagWithPriceAndQuantityResponse;
import com.tinqin.bff.api.operations.item.getbytitle.ItemGetByItemTitleRequest;
import com.tinqin.bff.api.operations.item.getbytitle.ItemGetByItemTitleResponse;
import com.tinqin.bff.api.operations.item.getbytitle.ItemGetByTitleOperation;
import com.tinqin.bff.customannotation.annotation.GenerateRestExport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Operation(description = "Gets all items having the specified tag and paginates them according the parameters specified.",
            summary = "Gets item by tag title.")
    @ApiResponse(responseCode = "200", description = "Items found.")
    @ApiResponse(responseCode = "400",
            description = "Tag title is required.",
            content = {@Content(examples = @ExampleObject(value = "Title is required."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Negative pageNumber parameter.",
            content = {@Content(examples = @ExampleObject(value = "Page number must be greater than or equal to zero."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Negative pageSize parameter.",
            content = {@Content(examples = @ExampleObject(value = "Page size must be positive number."), mediaType = "text/html")})
    @ApiResponse(responseCode = "403",
            description = "JWT is invalid.",
            content = {@Content(examples = @ExampleObject(value = ""), mediaType = "text/html")})
    @SecurityRequirement(name = "Bearer Authentication")
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

    @Operation(description = "Gets all items having the specified title and paginates them according the parameters specified.",
            summary = "Gets item by title.")
    @ApiResponse(responseCode = "200", description = "Items found.")
    @ApiResponse(responseCode = "400",
            description = "Item title is required.",
            content = {@Content(examples = @ExampleObject(value = "Title is required."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Negative pageNumber parameter.",
            content = {@Content(examples = @ExampleObject(value = "Page number must be greater than or equal to zero."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Negative pageSize parameter.",
            content = {@Content(examples = @ExampleObject(value = "Page size must be positive number."), mediaType = "text/html")})
    @ApiResponse(responseCode = "403",
            description = "JWT is invalid.",
            content = {@Content(examples = @ExampleObject(value = ""), mediaType = "text/html")})
    @SecurityRequirement(name = "Bearer Authentication")
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

    @Operation(description = "Gets item by item id.",
            summary = "Gets item by id.")
    @ApiResponse(responseCode = "200", description = "Item found.")
    @ApiResponse(responseCode = "400",
            description = "Not existing item.",
            content = {@Content(examples = @ExampleObject(value = "No such item."), mediaType = "text/html")})
    @ApiResponse(responseCode = "403",
            description = "JWT is invalid.",
            content = {@Content(examples = @ExampleObject(value = ""), mediaType = "text/html")})
    @SecurityRequirement(name = "Bearer Authentication")
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
