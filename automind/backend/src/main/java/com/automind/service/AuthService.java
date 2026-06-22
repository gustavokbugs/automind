package com.automind.service;

import com.automind.domain.entity.Usuario;
import com.automind.domain.enums.PerfilUsuario;
import com.automind.dto.request.LoginRequest;
import com.automind.dto.response.AuthResponse;
import com.automind.exception.BusinessException;
import com.automind.repository.UsuarioRepository;
import com.automind.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        String token = jwtService.gerarToken(usuario);

        return AuthResponse.builder()
            .token(token)
            .tipo("Bearer")
            .usuarioId(usuario.getId())
            .nome(usuario.getNome())
            .email(usuario.getEmail())
            .perfil(usuario.getPerfil())
            .build();
    }

    public void criarUsuarioAdmin(String nome, String email, String senha) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("E-mail já cadastrado");
        }
        usuarioRepository.save(Usuario.builder()
            .nome(nome)
            .email(email)
            .senha(passwordEncoder.encode(senha))
            .perfil(PerfilUsuario.ADMIN)
            .ativo(true)
            .build());
    }
}
