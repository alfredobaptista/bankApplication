package com.github.freddy.bankApi.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TempPasswordEncoder {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String senhaPlana = "AdminSenha1234";  // Muda aqui para a senha que queres
        String hash = encoder.encode(senhaPlana);
        System.out.println("Hash gerado para Flyway: " + hash);
    }
}
