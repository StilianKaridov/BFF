package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.cart.delete.CartDeleteOperation;
import com.tinqin.bff.api.operations.cart.delete.CartDeleteRequest;
import com.tinqin.bff.api.operations.cart.delete.CartDeleteResponse;
import com.tinqin.bff.core.exception.EmptyUserCartException;
import com.tinqin.bff.core.exception.NoSuchUserException;
import com.tinqin.bff.persistence.entity.ShoppingCart;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.repository.ShoppingCartRepository;
import com.tinqin.bff.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CartDeleteOperationProcessor implements CartDeleteOperation {

    private final ShoppingCartRepository shoppingCartRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartDeleteOperationProcessor(ShoppingCartRepository shoppingCartRepository, UserRepository userRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CartDeleteResponse process(CartDeleteRequest input) {
        User user = this.userRepository.findByEmail(input.getUsername())
                .orElseThrow(NoSuchUserException::new);
        UUID userId = user.getId();
        checkIfUserCartIsEmpty(userId);

        List<ShoppingCart> allUserItems = this.shoppingCartRepository.findAllByUserId(userId);

        List<String> itemsIds = allUserItems
                .stream()
                .map(i -> i.getItemId().toString())
                .toList();

        this.shoppingCartRepository.deleteAllByUserId(userId);

        return CartDeleteResponse
                .builder()
                .userId(userId.toString())
                .items(itemsIds)
                .build();
    }

    private void checkIfUserCartIsEmpty(UUID userId) {
        this.shoppingCartRepository
                .findByUserId(userId)
                .orElseThrow(EmptyUserCartException::new);
    }
}
