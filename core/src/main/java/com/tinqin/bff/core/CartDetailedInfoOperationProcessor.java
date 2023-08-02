package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.cart.getdetailedview.CartDetailedViewOperation;
import com.tinqin.bff.api.operations.cart.getdetailedview.CartDetailedViewRequest;
import com.tinqin.bff.api.operations.cart.getdetailedview.CartDetailedViewResponse;
import com.tinqin.bff.api.operations.cart.getdetailedview.CartItemsDetailedViewResponse;
import com.tinqin.bff.api.operations.cart.getdetailedview.UserCartInfoResponse;
import com.tinqin.bff.core.exception.EmptyUserCartException;
import com.tinqin.bff.core.exception.NoSuchUserException;
import com.tinqin.bff.persistence.entity.ShoppingCart;
import com.tinqin.bff.persistence.entity.User;
import com.tinqin.bff.persistence.repository.ShoppingCartRepository;
import com.tinqin.bff.persistence.repository.UserRepository;
import com.tinqin.zoostore.api.operations.item.getlistofitems.GetListOfItemsRequest;
import com.tinqin.zoostore.api.operations.item.getlistofitems.GetListOfItemsResponse;
import com.tinqin.zoostore.restexport.ZooStoreRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartDetailedInfoOperationProcessor implements CartDetailedViewOperation {

    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ZooStoreRestClient zooStoreRestClient;

    @Autowired
    public CartDetailedInfoOperationProcessor(UserRepository userRepository, ShoppingCartRepository shoppingCartRepository, ZooStoreRestClient zooStoreRestClient) {
        this.userRepository = userRepository;
        this.shoppingCartRepository = shoppingCartRepository;
        this.zooStoreRestClient = zooStoreRestClient;
    }

    @Override
    public CartDetailedViewResponse process(CartDetailedViewRequest input) {
        User user = this.userRepository.findByEmail(input.getEmail())
                .orElseThrow(NoSuchUserException::new);

        List<ShoppingCart> userItems = this.shoppingCartRepository.findAllByUserId(user.getId());

        if (userItems.isEmpty()) {
            throw new EmptyUserCartException();
        }

        List<String> itemIds = userItems
                .stream()
                .map(i -> i.getItemId().toString())
                .toList();

        GetListOfItemsRequest requestToZooStore = GetListOfItemsRequest
                .builder()
                .ids(itemIds)
                .build();

        GetListOfItemsResponse itemsFromZooStore = this.zooStoreRestClient.getListOfItemsByIds(requestToZooStore);

        UserCartInfoResponse userResponse = UserCartInfoResponse
                .builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .build();

        List<CartItemsDetailedViewResponse> itemsResponse = new ArrayList<>();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (ShoppingCart userItem : userItems) {
            itemsFromZooStore.getItems().stream()
                    .filter(i -> i.getId().equals(userItem.getItemId().toString()))
                    .map(item -> CartItemsDetailedViewResponse
                            .builder()
                            .id(item.getId())
                            .title(item.getTitle())
                            .vendor(item.getVendor())
                            .quantity(userItem.getQuantity())
                            .price(userItem.getPrice())
                            .build())
                    .forEach(itemsResponse::add);

            totalPrice = totalPrice.add(userItem.getPrice());
        }


        return CartDetailedViewResponse
                .builder()
                .user(userResponse)
                .items(itemsResponse)
                .totalPrice(totalPrice)
                .build();
    }
}
