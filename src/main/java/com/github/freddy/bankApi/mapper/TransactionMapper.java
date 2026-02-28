package com.github.freddy.bankApi.mapper;

import com.github.freddy.bankApi.dto.response.TransferResponse;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.math.BigDecimal;
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mappings({
            @Mapping(target = "transactionId", source = "transaction.id"),
            @Mapping(target = "destinationAccount", source = "destination.accountNumber"),
            @Mapping(target = "destinationOwnerName", source = "destination.user.name"),
            @Mapping(target = "amount", source = "amount"),
            @Mapping(target = "balance", source = "source.balance"),
            @Mapping(target = "currencyCode", source = "source.currencyCode"),
            @Mapping(target = "status", source = "transaction.status"),
            @Mapping(target = "message", source = "transaction.description"),
            @Mapping(target = "timestamp", source = "transaction.createdAt")
    })
    TransferResponse toResponse(Transaction transaction, Account source, Account destination, BigDecimal amount);
}
