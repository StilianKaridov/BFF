package com.tinqin.bff.api.operations.cart.getdetailedview;

import com.tinqin.bff.api.operations.base.OperationResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
public class UserCartInfoResponse implements OperationResponse {

    private String id;

    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;
}
