package com.automind.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração web adicional do Spring MVC.
 *
 * Registra o diretório de uploads como recurso estático.
 * Isso permite que o cliente acesse diretamente as fotos e vídeos
 * enviados pelo mecânico via URL: /api/uploads/{nomeArquivo}
 *
 * Sem esta configuração, o Spring não serviria arquivos do diretório local.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    /**
     * Mapeia requisições GET /uploads/** para o diretório local de uploads.
     * "file:" é o prefixo que indica um caminho no sistema de arquivos local.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
