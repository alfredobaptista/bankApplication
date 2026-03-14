package com.github.freddy.bankApi.factory;

import com.github.freddy.bankApi.dto.response.TransferResponse;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.enums.TransactionStatus;
import com.github.freddy.bankApi.enums.TransactionType;

import java.math.BigDecimal;

public class TransferResponseFactory {

    public static TransferResponse createTransferResponse(Account fromAccount, Account toAccount, BigDecimal amount){
        return new TransferResponse(
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                amount,
                fromAccount.getLedgerBalance(),
                "AOA",
                TransactionStatus.COMPLETED,
                TransactionType.WITHDRAWAL,
                "Transferência realizado com sucesso"
        );
    }
}
