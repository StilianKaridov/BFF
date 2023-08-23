package com.tinqin.bff.rest.controller;

import com.tinqin.bff.api.operations.cart.add.CartAddOperation;
import com.tinqin.bff.api.operations.cart.add.CartAddRequest;
import com.tinqin.bff.api.operations.cart.add.CartAddResponse;
import com.tinqin.bff.api.operations.cart.delete.CartDeleteOperation;
import com.tinqin.bff.api.operations.cart.delete.CartDeleteRequest;
import com.tinqin.bff.api.operations.cart.delete.CartDeleteResponse;
import com.tinqin.bff.api.operations.cart.deleteitem.CartDeleteByItemOperation;
import com.tinqin.bff.api.operations.cart.deleteitem.CartDeleteByItemRequest;
import com.tinqin.bff.api.operations.cart.deleteitem.CartDeleteByItemResponse;
import com.tinqin.bff.api.operations.cart.getdetailedview.CartDetailedViewOperation;
import com.tinqin.bff.api.operations.cart.getdetailedview.CartDetailedViewRequest;
import com.tinqin.bff.api.operations.cart.getdetailedview.CartDetailedViewResponse;
import com.tinqin.bff.api.operations.cart.sell.CartSellOperation;
import com.tinqin.bff.api.operations.cart.sell.CartSellRequest;
import com.tinqin.bff.api.operations.cart.sell.CartSellResponse;
import com.tinqin.bff.customannotation.annotation.GenerateRestExport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/bff/cart")
public class ShoppingCartController {

    private final CartAddOperation cartAddOperation;
    private final CartDeleteOperation cartDeleteOperation;
    private final CartDeleteByItemOperation cartDeleteByItemOperation;
    private final CartDetailedViewOperation cartDetailedViewOperation;
    private final CartSellOperation cartSellOperation;

    @Autowired
    public ShoppingCartController(CartAddOperation cartAddOperation, CartDeleteOperation cartDeleteOperation, CartDeleteByItemOperation cartDeleteByItemOperation, CartDetailedViewOperation cartDetailedViewOperation, CartSellOperation cartSellOperation) {
        this.cartAddOperation = cartAddOperation;
        this.cartDeleteOperation = cartDeleteOperation;
        this.cartDeleteByItemOperation = cartDeleteByItemOperation;
        this.cartDetailedViewOperation = cartDetailedViewOperation;
        this.cartSellOperation = cartSellOperation;
    }

    @Operation(description = "Gets all items from the current logged in user's cart.",
            summary = "Gets info about user's cart.")
    @ApiResponse(responseCode = "200", description = "All items from the cart.")
    @ApiResponse(responseCode = "400",
            description = "Not existing user.",
            content = {@Content(examples = @ExampleObject(value = "This user does not exist."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "User's cart is empty.",
            content = {@Content(examples = @ExampleObject(value = "User's cart is empty."), mediaType = "text/html")})
    @ApiResponse(responseCode = "403",
            description = "JWT is invalid.",
            content = {@Content(examples = @ExampleObject(value = ""), mediaType = "text/html")})
    @SecurityRequirement(name = "Bearer Authentication")
    @GenerateRestExport
    @GetMapping
    public ResponseEntity<CartDetailedViewResponse> detailedInformation(
            Principal principal
    ) {
        CartDetailedViewRequest userCart = CartDetailedViewRequest
                .builder()
                .email(principal.getName())
                .build();

        CartDetailedViewResponse response = this.cartDetailedViewOperation.process(userCart);

        return ResponseEntity.ok(response);
    }

    @Operation(description = "Adds an item to the current logged in user's cart.",
            summary = "Adds an item to cart.")
    @ApiResponse(responseCode = "201", description = "Item is added to the cart.")
    @ApiResponse(responseCode = "400",
            description = "Not existing user.",
            content = {@Content(examples = @ExampleObject(value = "This user does not exist."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Item does not exists.",
            content = {@Content(examples = @ExampleObject(value = "No such item."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Not enough quantity for the item.",
            content = {@Content(examples = @ExampleObject(value = "Not enough quantity!"), mediaType = "text/html")})
    @ApiResponse(responseCode = "403",
            description = "JWT is invalid.",
            content = {@Content(examples = @ExampleObject(value = ""), mediaType = "text/html")})
    @SecurityRequirement(name = "Bearer Authentication")
    @GenerateRestExport
    @PostMapping
    public ResponseEntity<CartAddResponse> addItemToCart(
            @RequestBody @Valid CartAddRequest cartAddRequest,
            Principal principal
    ) {
        CartAddRequest requestWithUser = CartAddRequest
                .builder()
                .username(principal.getName())
                .itemId(cartAddRequest.getItemId())
                .quantity(cartAddRequest.getQuantity())
                .build();

        CartAddResponse response = this.cartAddOperation.process(requestWithUser);

        return ResponseEntity.status(201).body(response);
    }

    @Operation(description = "Deletes an item from the current logged in user's cart.",
            summary = "Deletes an item from cart.")
    @ApiResponse(responseCode = "200", description = "Item is removed from the cart.")
    @ApiResponse(responseCode = "400",
            description = "Not existing user.",
            content = {@Content(examples = @ExampleObject(value = "This user does not exist."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Item does not exists.",
            content = {@Content(examples = @ExampleObject(value = "No such item."), mediaType = "text/html")})
    @ApiResponse(responseCode = "403",
            description = "JWT is invalid.",
            content = {@Content(examples = @ExampleObject(value = ""), mediaType = "text/html")})
    @SecurityRequirement(name = "Bearer Authentication")
    @Transactional
    @DeleteMapping("/item")
    public ResponseEntity<CartDeleteByItemResponse> deleteItemFromCart(
            @RequestBody @Valid CartDeleteByItemRequest cartDeleteByItemRequest,
            Principal principal
    ) {
        CartDeleteByItemRequest requestWithUser = CartDeleteByItemRequest
                .builder()
                .username(principal.getName())
                .itemId(cartDeleteByItemRequest.getItemId())
                .build();

        CartDeleteByItemResponse response = this.cartDeleteByItemOperation.process(requestWithUser);

        return ResponseEntity.ok(response);
    }

    @Operation(description = "User buys all items from his cart.",
            summary = "User buys all items from his cart.")
    @ApiResponse(responseCode = "200", description = "Returns the bought items and the total price.")
    @ApiResponse(responseCode = "400",
            description = "Not existing user.",
            content = {@Content(examples = @ExampleObject(value = "This user does not exist."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "User's cart is empty.",
            content = {@Content(examples = @ExampleObject(value = "User's cart is empty."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "Not enough quantity for an item from the cart.",
            content = {@Content(examples = @ExampleObject(value = "Not enough quantity for 'item id'"), mediaType = "text/html")})
    @ApiResponse(responseCode = "403",
            description = "JWT is invalid.",
            content = {@Content(examples = @ExampleObject(value = ""), mediaType = "text/html")})
    @SecurityRequirement(name = "Bearer Authentication")
    @Transactional
    @DeleteMapping("/sell")
    public ResponseEntity<CartSellResponse> sellUserCart(Principal principal) {
        CartSellRequest request = CartSellRequest
                .builder()
                .creditCardNumber("12345")
                .email(principal.getName())
                .build();

        CartSellResponse response = this.cartSellOperation.process(request);

        return ResponseEntity.ok(response);
    }

    @Operation(description = "Deletes all items from user's cart.",
            summary = "Deletes user's cart.")
    @ApiResponse(responseCode = "200", description = "Returns the deleted items and current logged in user id.")
    @ApiResponse(responseCode = "400",
            description = "Not existing user.",
            content = {@Content(examples = @ExampleObject(value = "This user does not exist."), mediaType = "text/html")})
    @ApiResponse(responseCode = "400",
            description = "User cart is empty.",
            content = {@Content(examples = @ExampleObject(value = "User's cart is empty."), mediaType = "text/html")})
    @ApiResponse(responseCode = "403",
            description = "JWT is invalid.",
            content = {@Content(examples = @ExampleObject(value = ""), mediaType = "text/html")})
    @SecurityRequirement(name = "Bearer Authentication")
    @Transactional
    @DeleteMapping
    public ResponseEntity<CartDeleteResponse> deleteUserCart(Principal principal) {
        CartDeleteRequest requestWithUser = CartDeleteRequest
                .builder()
                .username(principal.getName())
                .build();

        CartDeleteResponse response = this.cartDeleteOperation.process(requestWithUser);

        return ResponseEntity.ok(response);
    }
}
