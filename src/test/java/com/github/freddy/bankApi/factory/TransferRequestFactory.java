package com.github.freddy.bankApi.factory;

import com.github.freddy.bankApi.dto.request.TransferRequest;

import java.math.BigDecimal;

public class TransferRequestFactory {

    public static TransferRequest createTransferRequest(){
        return new TransferRequest("AO2902927", BigDecimal.valueOf(50000.00));
    }
}
