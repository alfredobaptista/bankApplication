package com.github.freddy.bankApi.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DifferentAccountsValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DifferentAccounts {
    String message() default "{transfer.accounts.different}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
