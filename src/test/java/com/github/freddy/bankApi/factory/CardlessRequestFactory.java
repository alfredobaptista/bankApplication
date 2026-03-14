package com.github.freddy.bankApi.factory;

import com.github.freddy.bankApi.dto.request.AtmCardlessRequest;

public class CardlessRequestFactory {

    public static AtmCardlessRequest createAtmCardlessRequest() {
        return new AtmCardlessRequest(CardlessWithdrawalFactory.VALID_REFERENCE, "123");
    }

    public static AtmCardlessRequest createAtmCardlessRequestInvalidReferenceCode() {
        return new AtmCardlessRequest(CardlessWithdrawalFactory.INVALID_REFERENCE, "123");
    }

    public static AtmCardlessRequest createAtmCardlessRequestInvalidSecretCode() {
        return new AtmCardlessRequest(CardlessWithdrawalFactory.VALID_REFERENCE, "321");
    }
}
