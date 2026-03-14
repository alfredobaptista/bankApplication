package com.github.freddy.bankApi.factory;

import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.User;
import com.github.freddy.bankApi.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountFactory {
    public static final BigDecimal AVAILABLEBALANCE = new BigDecimal("12500.00");
    public static final BigDecimal LEDGERBALANCE = new BigDecimal("20000.00");

    public static Account createAccount(User user){
        return Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("AO10262520")
                .accountType(AccountType.CHECKING)
                .currencyCode("AOA")
                .availableBalance(AVAILABLEBALANCE)
                .ledgerBalance(LEDGERBALANCE)
                .user(user)
                .version(1L)
                .build();
    }
}
