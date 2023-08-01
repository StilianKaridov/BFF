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
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Autowired
    public ShoppingCartController(CartAddOperation cartAddOperation, CartDeleteOperation cartDeleteOperation, CartDeleteByItemOperation cartDeleteByItemOperation) {
        this.cartAddOperation = cartAddOperation;
        this.cartDeleteOperation = cartDeleteOperation;
        this.cartDeleteByItemOperation = cartDeleteByItemOperation;
    }

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
