package com.github.freddy.bankApi.mapper;

import com.github.freddy.bankApi.dto.response.AccountResponse;
import com.github.freddy.bankApi.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "initialBalance", source = "balance")
    @Mapping(target = "accountStatus", source = "status")
    AccountResponse toResponse(Account account);
}