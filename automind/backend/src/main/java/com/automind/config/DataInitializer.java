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

    /** Serviço de autenticação usado para criar usuário admin inicial. */
    private final AuthService authService;

    /**
     * Inicializador de dados executado no startup da aplicação.
     * - Cria um usuário administrador padrão quando não existe.
     * - Ideal para ambiente de desenvolvimento; em produção revise comportamento e credenciais.
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            try {
                authService.criarUsuarioAdmin("Administrador", "admin@automind.com", "admin123");
                log.info("Usuário admin criado: admin@automind.com / admin123");
            } catch (Exception e) {
                // se já existir lança exceção e aqui apenas logamos e seguimos
                log.info("Admin já existe, pulando criação.");
            }
        };
    }
}
