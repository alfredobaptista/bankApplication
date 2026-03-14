package com.github.freddy.bankApi.factory;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.CardlessWithdrawal;
import com.github.freddy.bankApi.enums.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CardlessWithdrawalFactory {
    public static final String VALID_REFERENCE = "12345678";
    public static final String INVALID_REFERENCE = "87654321";
    public static final String VALID_SECRET    = "123";
    public static final String INVALID_SECRET  = "000";
    public static final BigDecimal AMOUNT      = new BigDecimal("7500.00");

    public static CardlessWithdrawal createApendingCardlessWithdrawal(Account account) {
        return CardlessWithdrawal.builder()
                .id(1L)
                .userId(account.getUser().getId())
                .accountNumber(account.getAccountNumber())
                .referenceCode(VALID_REFERENCE)
                .secretCode(VALID_SECRET)
                .amount(AMOUNT)
                .status(WithdrawalStatus.PENDING)
                .expiry(LocalDateTime.now().plusMinutes(20))
                .build();
    }

    public static CardlessWithdrawal createAnExpiredWithdrawal(Account account) {
        CardlessWithdrawal cw = createApendingCardlessWithdrawal(account);
        cw.setExpiry(LocalDateTime.now().minusMinutes(5));
        return cw;
    }

    public static CardlessWithdrawal createCompletedWithdrawal(CardlessWithdrawal cw) {
        return CardlessWithdrawal.builder()
                .id(cw.getId())
                .userId(cw.getUserId())
                .accountNumber(cw.getAccountNumber())
                .referenceCode(VALID_REFERENCE)
                .secretCode(VALID_SECRET)
                .amount(AMOUNT)
                .status(WithdrawalStatus.COMPLETED)
                .expiry(LocalDateTime.now().plusMinutes(20))
                .build();
    }
}
