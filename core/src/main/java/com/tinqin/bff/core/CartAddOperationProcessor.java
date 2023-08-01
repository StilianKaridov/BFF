package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.cart.add.CartAddOperation;
import com.tinqin.bff.api.operations.cart.add.CartAddRequest;
import com.tinqin.bff.api.operations.cart.add.CartAddResponse;
import com.tinqin.bff.api.operations.item.getbyid.ItemGetByIdOperation;
import com.tinqin.bff.api.operations.item.getbyid.ItemRequest;
import com.tinqin.bff.api.operations.item.getbyid.ItemResponse;
import com.tinqin.bff.core.exception.NoSuchItemException;
import com.tinqin.bff.core.exception.NoSuchUserException;
import com.tinqin.bff.core.exception.NotEnoughQuantityException;
import com.tinqin.bff.persistence.entity.ShoppingCart;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.repository.ShoppingCartRepository;
import com.tinqin.bff.persistence.repository.UserRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartAddOperationProcessor implements CartAddOperation {

    private final ItemGetByIdOperation itemGetByIdOperation;
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartAddOperationProcessor(
            ItemGetByIdOperation itemGetByIdOperation,
            ShoppingCartRepository shoppingCartRepository,
            UserRepository userRepository
    ) {
        this.itemGetByIdOperation = itemGetByIdOperation;
        this.shoppingCartRepository = shoppingCartRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CartAddResponse process(CartAddRequest input) {
        int quantityFromInput = input.getQuantity();
        String itemIdFromInput = input.getItemId();

        User user = this.userRepository
                .findByEmail(input.getUsername())
                .orElseThrow(NoSuchUserException::new);

        ItemRequest item = ItemRequest
                .builder()
                .id(itemIdFromInput)
                .build();

        ItemResponse itemFromZooAndStorage;

        try {
            itemFromZooAndStorage = this.itemGetByIdOperation.process(item);
        } catch (FeignException e) {
            throw new NoSuchItemException();
        }

        UUID itemId = UUID.fromString(itemIdFromInput);
        Optional<ShoppingCart> itemFromShoppingCart = this.shoppingCartRepository.findByUserIdAndItemId(user.getId(), itemId);

        if (itemFromShoppingCart.isPresent()) {
            ShoppingCart existing = itemFromShoppingCart.get();

            if (existing.getQuantity() + quantityFromInput > itemFromZooAndStorage.getQuantity()) {
                throw new NotEnoughQuantityException();
            }

            int newQuantity = existing.getQuantity() + quantityFromInput;
            BigDecimal newPrice = itemFromZooAndStorage.getPrice().multiply(BigDecimal.valueOf(newQuantity));

            ShoppingCart existingItem = ShoppingCart
                    .builder()
                    .id(existing.getId())
                    .itemId(itemId)
                    .userId(existing.getUserId())
                    .quantity(newQuantity)
                    .price(newPrice)
                    .build();

            this.shoppingCartRepository.save(existingItem);

            return CartAddResponse
                    .builder()
                    .itemId(itemIdFromInput)
                    .userId(existingItem.getUserId().toString())
                    .price(existingItem.getPrice())
                    .quantity(existingItem.getQuantity())
                    .build();
        }

        if (quantityFromInput > itemFromZooAndStorage.getQuantity()) {
            throw new NotEnoughQuantityException();
        }

        BigDecimal price = itemFromZooAndStorage.getPrice().multiply(BigDecimal.valueOf(quantityFromInput));

        ShoppingCart newItem = ShoppingCart
                .builder()
                .itemId(itemId)
                .userId(user.getId())
                .quantity(quantityFromInput)
                .price(price)
                .build();

        this.shoppingCartRepository.save(newItem);

        return CartAddResponse
                .builder()
                .itemId(itemIdFromInput)
                .userId(newItem.getUserId().toString())
                .price(newItem.getPrice())
                .quantity(newItem.getQuantity())
                .build();
    }
}
