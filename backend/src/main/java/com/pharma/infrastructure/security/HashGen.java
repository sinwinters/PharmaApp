package com.pharma.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Пароли, которые хотим захэшировать
        String[] passwords = {"admin", "password"};

        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println(password + " -> " + hash);
        }
    }
}