package com.automind.controller;

import com.automind.dto.request.LoginRequest;
import com.automind.dto.response.ApiResponse;
import com.automind.dto.response.AuthResponse;
import com.automind.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de login e gestão de usuários")
public class AuthController {
    /**
     * Serviço responsável pela lógica de autenticação (login, geração de token, validações).
     * Injetado pelo Lombok `@RequiredArgsConstructor`.
     */
    private final AuthService authService;

    /**
     * Endpoint de login.
     * Recebe credenciais via `LoginRequest`, delega ao `AuthService` e devolve
     * um `AuthResponse` contendo o token JWT e informações do usuário.
     * @param request dados de login validados (@Valid)
     * @return `ApiResponse` com `AuthResponse` em caso de sucesso
     */
    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica o usuário e retorna o token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        // delega toda a lógica de autenticação ao service e encapsula resposta padrão
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }
}
