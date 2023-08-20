package com.tinqin.bff.restexport;

import java.security.Principal;
import com.tinqin.bff.api.operations.cart.add.CartAddRequest;
import com.tinqin.bff.api.operations.cart.add.CartAddResponse;
import com.tinqin.bff.api.operations.cart.getdetailedview.CartDetailedViewResponse;
import com.tinqin.bff.api.operations.item.getbyid.ItemResponse;
import com.tinqin.bff.api.operations.item.getbytag.ItemGetByTagWithPriceAndQuantityResponse;
import com.tinqin.bff.api.operations.user.changepassword.UserChangePasswordRequest;
import com.tinqin.bff.api.operations.user.changepassword.UserChangePasswordResponse;
import com.tinqin.bff.api.operations.user.login.UserLoginRequest;
import com.tinqin.bff.api.operations.user.login.UserLoginResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
    "Content-Type: application/json"
})
public interface BffRestClient {

    @RequestLine("POST /api/bff/users/login")
    UserLoginResponse login(@Param UserLoginRequest userLoginRequest);

    @RequestLine("PUT /api/bff/users/changePassword")
    UserChangePasswordResponse changePassword(@Param UserChangePasswordRequest userChangePasswordRequest, @Param Principal principal);

    @RequestLine("GET /api/bff/cart")
    CartDetailedViewResponse detailedInformation(@Param Principal principal);

    @RequestLine("POST /api/bff/cart")
    CartAddResponse addItemToCart(@Param CartAddRequest cartAddRequest, @Param Principal principal);

    @RequestLine("GET /api/bff/items/byTag?title={title}&pageNumber={pageNumber}&pageSize={pageSize}")
    ItemGetByTagWithPriceAndQuantityResponse getItemsByTag(@Param String title, @Param Integer pageNumber, @Param Integer pageSize);

    @RequestLine("GET /api/bff/items/{id}")
    ItemResponse getItemById(@Param String id);
}
