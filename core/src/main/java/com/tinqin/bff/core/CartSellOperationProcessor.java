package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.cart.sell.CartSellOperation;
import com.tinqin.bff.api.operations.cart.sell.CartSellRequest;
import com.tinqin.bff.api.operations.cart.sell.CartSellResponse;
import com.tinqin.bff.core.exception.NoSuchUserException;
import com.tinqin.bff.core.exception.NotEnoughQuantityException;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.repository.ShoppingCartRepository;
import com.tinqin.bff.persistence.repository.UserRepository;
import com.tinqin.storage.api.operations.sell.ItemSellDataResponse;
import com.tinqin.storage.api.operations.sell.ItemsSellRequest;
import com.tinqin.storage.api.operations.sell.ItemsSellResponse;
import com.tinqin.storage.restexport.StorageRestClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartSellOperationProcessor implements CartSellOperation {

    private final ShoppingCartRepository shoppingCartRepository;
    private final UserRepository userRepository;
    private final StorageRestClient storageRestClient;

    @Autowired
    public CartSellOperationProcessor(ShoppingCartRepository shoppingCartRepository, UserRepository userRepository, StorageRestClient storageRestClient) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.userRepository = userRepository;
        this.storageRestClient = storageRestClient;
    }

    @Override
    public CartSellResponse process(CartSellRequest input) {
        User user = this.userRepository.findByEmail(input.getEmail())
                .orElseThrow(NoSuchUserException::new);

        List<ItemSellDataResponse> items = this.shoppingCartRepository.findAllByUserId(user.getId())
                .stream()
                .map(item -> ItemSellDataResponse
                        .builder()
                        .userId(item.getUserId().toString())
                        .itemId(item.getItemId().toString())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        ItemsSellRequest requestToStorage = ItemsSellRequest
                .builder()
                .items(items)
                .build();

        ItemsSellResponse itemsSellResponse;
        try {
            itemsSellResponse = this.storageRestClient.sellItems(requestToStorage);
        } catch (FeignException ex) {
            String responseBody = ex.contentUTF8();
            String errorMessage = responseBody.substring(responseBody.indexOf("Not enough quantity for "));

            throw new NotEnoughQuantityException(errorMessage);
        }

        this.shoppingCartRepository.deleteAllByUserId(user.getId());

        return CartSellResponse
                .builder()
                .boughtItems(itemsSellResponse.getItems())
                .build();
    }
}
