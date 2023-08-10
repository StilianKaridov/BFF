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
import com.tinqin.bff.core.annotations.RequestInfoToTextFile;
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

    @RequestInfoToTextFile
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

    @RequestInfoToTextFile
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
