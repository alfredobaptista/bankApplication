package com.github.freddy.bankApi.mapper;

import com.github.freddy.bankApi.dto.request.CardlessWithdrawRequest;
import com.github.freddy.bankApi.dto.response.CardlessWithdrawalDetailsResponse;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.CardlessWithdrawal;
import com.github.freddy.bankApi.enums.WithdrawalStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.UUID;
@Mapper(componentModel = "spring")
public interface CardLessMapper {

    @Mappings({
            @Mapping(target = "id", expression = "java(null)"),
            @Mapping(target = "userId", source = "userId"),
            @Mapping(target = "accountNumber", source = "account.accountNumber"),
            @Mapping(target = "amount", source = "dto.amount"),
            @Mapping(target = "referenceCode", source = "referenceCode"),
            @Mapping(target = "secretCode", source = "dto.secretCode"),
            @Mapping(target = "status", constant = "PENDING"),
            @Mapping(target = "expiry", ignore = true)
    })
    CardlessWithdrawal toEntity(Account account, CardlessWithdrawRequest dto, UUID userId, String referenceCode);

    CardlessWithdrawalDetailsResponse toResponse(CardlessWithdrawal entity);
}