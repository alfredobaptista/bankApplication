package com.github.freddy.bankApi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "fraud")
@Component
@Data
public class FraudProperties {

    private BigDecimal threshold;
    private Long transactionCount;
    private Long windowMinutes;

}
