package com.automind.config;

import com.automind.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final AuthService authService;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            try {
                authService.criarUsuarioAdmin("Administrador", "admin@automind.com", "admin123");
                log.info("Usuário admin criado: admin@automind.com / admin123");
            } catch (Exception e) {
                log.info("Admin já existe, pulando criação.");
            }
        };
    }
}
