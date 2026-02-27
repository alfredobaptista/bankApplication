package com.github.freddy.bankApi.dto;

public record SuspiciousAccount(String accountNumber, Long transactionCount) {
}
