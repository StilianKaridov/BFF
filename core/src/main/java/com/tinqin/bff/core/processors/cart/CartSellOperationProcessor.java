package com.tinqin.bff.core.processors.cart;

import com.tinqin.bff.api.operations.cart.sell.CartSellOperation;
import com.tinqin.bff.api.operations.cart.sell.CartSellRequest;
import com.tinqin.bff.api.operations.cart.sell.CartSellResponse;
import com.tinqin.bff.core.exception.EmptyUserCartException;
import com.tinqin.bff.core.exception.NoSuchUserException;
import com.tinqin.bff.core.exception.NotEnoughQuantityException;
import com.tinqin.bff.core.exception.UnsuccessfulPaymentException;
import com.tinqin.bff.persistence.entity.ShoppingCart;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.repository.ShoppingCartRepository;
import com.tinqin.bff.persistence.repository.UserRepository;
import com.tinqin.payment.api.PaymentRequest;
import com.tinqin.payment.api.PaymentResponse;
import com.tinqin.payment.restexport.PaymentRestClient;
import com.tinqin.storage.api.operations.sell.ItemSellDataResponse;
import com.tinqin.storage.api.operations.sell.ItemsSellRequest;
import com.tinqin.storage.api.operations.sell.ItemsSellResponse;
import com.tinqin.storage.api.operations.usercheckfororders.UserCheckForOrdersResponse;
import com.tinqin.storage.restexport.StorageRestClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartSellOperationProcessor implements CartSellOperation {

    private final ShoppingCartRepository shoppingCartRepository;
    private final UserRepository userRepository;
    private final StorageRestClient storageRestClient;
    private final PaymentRestClient paymentRestClient;

    @Autowired
    public CartSellOperationProcessor(ShoppingCartRepository shoppingCartRepository, UserRepository userRepository, StorageRestClient storageRestClient, PaymentRestClient paymentRestClient) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.userRepository = userRepository;
        this.storageRestClient = storageRestClient;
        this.paymentRestClient = paymentRestClient;
    }

    @Override
    public CartSellResponse process(CartSellRequest input) {
        User user = this.userRepository.findByEmail(input.getEmail())
                .orElseThrow(NoSuchUserException::new);

        List<ShoppingCart> allUserItems = this.shoppingCartRepository.findAllByUserId(user.getId());
        if (allUserItems.isEmpty()) throw new EmptyUserCartException();

        double discount = getDiscount(user);

        List<ItemSellDataResponse> items = mapUserItems(allUserItems, discount);
        BigDecimal totalPrice = getTotalPrice(items);

        if (!isPaymentSuccessful(input, totalPrice)) {
            throw new UnsuccessfulPaymentException();
        }

        ItemsSellResponse itemsSellResponse;
        try {
            ItemsSellRequest requestToStorage = ItemsSellRequest
                    .builder()
                    .userId(user.getId().toString())
                    .items(items)
                    .build();
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
                .totalPrice(totalPrice)
                .build();
    }

    private boolean isPaymentSuccessful(CartSellRequest input, BigDecimal totalPrice) {
        PaymentRequest paymentRequest = PaymentRequest
                .builder()
                .cardNumber(input.getCardNumber())
                .expMonth(input.getExpMonth())
                .expYear(input.getExpYear())
                .cvc(input.getCvc())
                .amount(totalPrice)
                .build();
        try {
            PaymentResponse paymentResponse = this.paymentRestClient.processPayment(paymentRequest);
            return paymentResponse.getIsSuccessful();
        } catch (FeignException e) {
            return false;
        }
    }

    private BigDecimal getTotalPrice(List<ItemSellDataResponse> items) {
        return items.stream().map(ItemSellDataResponse::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<ItemSellDataResponse> mapUserItems(List<ShoppingCart> allUserItems, double discount) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        List<ItemSellDataResponse> items = new ArrayList<>();
        for (ShoppingCart item : allUserItems) {
            BigDecimal price = item.getPrice();
            BigDecimal priceWithDiscount = price.multiply(BigDecimal.valueOf(discount));

            ItemSellDataResponse mappedItem = ItemSellDataResponse
                    .builder()
                    .itemId(item.getItemId().toString())
                    .quantity(item.getQuantity())
                    .priceWithDiscount(priceWithDiscount)
                    .price(price)
                    .build();

            items.add(mappedItem);
            totalPrice = totalPrice.add(priceWithDiscount);
        }

        return items;
    }

    private double getDiscount(User user) {
        LocalDateTime oneWeekAfterRegistration = user.getRegisteredOn().toLocalDateTime().plusDays(7);
        LocalDateTime currentTime = LocalDateTime.now();

        UserCheckForOrdersResponse userCheckForOrders = this.storageRestClient
                .checkIfUserHasOrders(user.getId().toString());

        boolean isValidForDiscount = oneWeekAfterRegistration.isAfter(currentTime) &&
                !userCheckForOrders.getHasOrders();
        return isValidForDiscount ? 0.95 : 1;
    }
}
