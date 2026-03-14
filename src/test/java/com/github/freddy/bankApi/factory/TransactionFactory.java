package com.github.freddy.bankApi.factory;

import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.Transaction;
import com.github.freddy.bankApi.enums.TransactionStatus;
import com.github.freddy.bankApi.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionFactory {
    public static Transaction aCompletedTransaction(Account account, BigDecimal amount, String refCode) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .sourceAccount(account)
                .status(TransactionStatus.COMPLETED)
                .build();
    }
}
