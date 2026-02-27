package com.github.freddy.bankApi.util;

public final class AccountNumberUtil {
    private static final String COUNTRY_PREFIX = "AO";
    private static final int BASE_LENGTH = 6;   // sequência com 6 dígitos
    private static final int TOTAL_LENGTH = 7;  // base + check digit

    private AccountNumberUtil() { }

    public static String generateAccountNumber(Long sequenceValue) {
        if (sequenceValue == null || sequenceValue <= 0) {
            throw new IllegalArgumentException("O valor da sequência deve ser positivo");
        }

        // Sequência com 6 dígitos
        String baseNumber = String.format("%0" + BASE_LENGTH + "d", sequenceValue);

        // Dígito verificador
        int checkDigit = calculateCheckDigit(baseNumber);

        String numericPart = baseNumber + checkDigit;

        if (numericPart.length() != TOTAL_LENGTH) {
            throw new IllegalStateException("Número gerado não tem o tamanho esperado");
        }

        return COUNTRY_PREFIX + numericPart; // AO + 7 dígitos = AO + 8 total
    }

    private static int calculateCheckDigit(String number) {
        if (!number.matches("\\d+")) {
            throw new IllegalArgumentException("Número deve conter apenas dígitos");
        }

        int sum = 0;
        int weight = 2;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            sum += digit * weight;
            weight = (weight == 9) ? 2 : weight + 1;
        }
        int remainder = sum % 11;
        int result = (remainder == 0) ? 0 : 11 - remainder;
        return (result >= 10) ? 0 : result;
    }
}
