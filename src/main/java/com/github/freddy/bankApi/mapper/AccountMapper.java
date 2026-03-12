package com.github.freddy.bankApi.mapper;

import com.github.freddy.bankApi.dto.response.AccountResponse;
import com.github.freddy.bankApi.dto.response.BalanceResponse;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.entity.User;
import com.github.freddy.bankApi.enums.AccountStatus;
import com.github.freddy.bankApi.enums.AccountType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring", imports = {RoundingMode.class, BigDecimal.class, AccountStatus.class})
public interface AccountMapper {

    @Mapping(target = "accountStatus", source = "status")
    @Mapping(target = "leadgerBalance", source = "ledgerBalance")
    @Mapping(target = "userName", source = "user.name")
    AccountResponse toResponse(Account account);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "accountType", source = "accountType")
    @Mapping(target = "availableBalance", expression = "java(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))")
    @Mapping(target = "ledgerBalance", expression = "java(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))")
    @Mapping(target = "status", expression = "java(AccountStatus.ACTIVE)")
    @Mapping(target = "currencyCode", constant = "AOA")
    Account toEntity(User user, AccountType accountType);


    BalanceResponse toBalanceResponse(Account account);
}