package com.github.freddy.bankApi.validation;

import com.github.freddy.bankApi.dto.request.TransferRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DifferentAccountsValidator implements ConstraintValidator<DifferentAccounts, TransferRequest> {
    @Override
    public boolean isValid(TransferRequest dto, ConstraintValidatorContext context) {
        return dto != null && !dto.sourceAccountNumber().equals(dto.destinationAccountNumber());
    }
}
