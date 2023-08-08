package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.cart.sell.CartSellOperation;
import com.tinqin.bff.api.operations.cart.sell.CartSellRequest;
import com.tinqin.bff.api.operations.cart.sell.CartSellResponse;
import com.tinqin.bff.core.exception.NoSuchUserException;
import com.tinqin.bff.core.exception.NotEnoughQuantityException;
import com.tinqin.bff.persistence.entity.ShoppingCart;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.repository.ShoppingCartRepository;
import com.tinqin.bff.persistence.repository.UserRepository;
import com.tinqin.storage.api.operations.sell.ItemSellDataResponse;
import com.tinqin.storage.api.operations.sell.ItemsSellRequest;
import com.tinqin.storage.api.operations.sell.ItemsSellResponse;
import com.tinqin.storage.api.operations.usercheckfororders.UserCheckForOrdersResponse;
import com.tinqin.storage.restexport.StorageRestClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        UUID userId = user.getId();

        LocalDateTime oneWeekAfterRegistration = user.getRegisteredOn().toLocalDateTime().plusDays(7);
        LocalDateTime currentTime = LocalDateTime.now();

        UserCheckForOrdersResponse userCheckForOrders = this.storageRestClient.checkIfUserHasOrders(userId.toString());

        boolean isValidForDiscount = oneWeekAfterRegistration.isAfter(currentTime) && !userCheckForOrders.getHasOrders();
        double discount = isValidForDiscount ? 0.95 : 1;

        BigDecimal totalPrice = BigDecimal.ZERO;

        List<ItemSellDataResponse> items = new ArrayList<>();
        for (ShoppingCart item : this.shoppingCartRepository.findAllByUserId(userId)) {
            BigDecimal price = item.getPrice();
            BigDecimal priceWithDiscount = price.multiply(BigDecimal.valueOf(discount));

            ItemSellDataResponse build = ItemSellDataResponse
                    .builder()
                    .itemId(item.getItemId().toString())
                    .quantity(item.getQuantity())
                    .priceWithDiscount(priceWithDiscount)
                    .price(price)
                    .build();

            items.add(build);

            totalPrice = totalPrice.add(priceWithDiscount);
        }

        ItemsSellRequest requestToStorage = ItemsSellRequest
                .builder()
                .userId(userId.toString())
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

        this.shoppingCartRepository.deleteAllByUserId(userId);

        return CartSellResponse
                .builder()
                .boughtItems(itemsSellResponse.getItems())
                .totalPrice(totalPrice)
                .build();
    }
}
