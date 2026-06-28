package com.automind.config;

import com.automind.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class UserDetailsConfig {

    private final UsuarioRepository usuarioRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        // Bean usado pelo Spring Security para carregar dados do usuário durante autenticação.
        // Procura o usuário pelo email (utilizado como username) e lança `UsernameNotFoundException`
        // quando não encontrado, comportamento esperado pelo framework.
        return email -> usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}
