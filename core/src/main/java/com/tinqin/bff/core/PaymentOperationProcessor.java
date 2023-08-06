package com.tinqin.bff.core;

import com.tinqin.bff.api.operations.payment.PaymentOperation;
import com.tinqin.bff.api.operations.payment.PaymentRequest;
import com.tinqin.bff.api.operations.payment.PaymentResponse;
import org.springframework.stereotype.Service;

@Service
public class PaymentOperationProcessor implements PaymentOperation {

    @Override
    public PaymentResponse process(PaymentRequest input) {
        return PaymentResponse
                .builder()
                .isSuccessful(true)
                .build();
    }
}
