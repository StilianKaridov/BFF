package com.tinqin.bff.api.operations.payment;

import com.tinqin.bff.api.operations.base.OperationRequest;
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
public class PaymentRequest implements OperationRequest {

    private String userEmail;

    private String creditCardNumber;
}
