package com.tinqin.bff.core.processors.cart;

import com.tinqin.bff.api.operations.cart.deleteitem.CartDeleteByItemOperation;
import com.tinqin.bff.api.operations.cart.deleteitem.CartDeleteByItemRequest;
import com.tinqin.bff.api.operations.cart.deleteitem.CartDeleteByItemResponse;
import com.tinqin.bff.core.exception.NoSuchItemException;
import com.tinqin.bff.core.exception.NoSuchUserException;
import com.tinqin.bff.persistence.entity.ShoppingCart;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.repository.ShoppingCartRepository;
import com.tinqin.bff.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CartDeleteByItemOperationProcessor implements CartDeleteByItemOperation {

    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    @Autowired
    public CartDeleteByItemOperationProcessor(UserRepository userRepository, ShoppingCartRepository shoppingCartRepository) {
        this.userRepository = userRepository;
        this.shoppingCartRepository = shoppingCartRepository;
    }

    @Override
    public CartDeleteByItemResponse process(CartDeleteByItemRequest input) {
        User user = this.userRepository.findByEmail(input.getUsername())
                .orElseThrow(NoSuchUserException::new);

        UUID userId = user.getId();
        UUID itemId = UUID.fromString(input.getItemId());
        ShoppingCart cart = this.shoppingCartRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(NoSuchItemException::new);

        this.shoppingCartRepository.delete(cart);

        return CartDeleteByItemResponse
                .builder()
                .userId(userId.toString())
                .itemId(itemId.toString())
                .price(cart.getPrice())
                .quantity(cart.getQuantity())
                .build();
    }
}
