package com.automind.controller;

import com.automind.domain.entity.MidiaOS;
import com.automind.domain.entity.OrdemServico;
import com.automind.dto.response.ApiResponse;
import com.automind.exception.ResourceNotFoundException;
import com.automind.repository.MidiaOSRepository;
import com.automind.repository.OrdemServicoRepository;
import com.automind.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Controller para upload de fotos e vídeos pelo mecânico no painel interno.
 *
 * O mecânico acessa a OS no painel e envia arquivos (fotos do motor,
 * vídeo do freio, etc.) que ficam vinculados à OS e aparecem
 * na linha do tempo do Portal do Cliente em tempo real.
 *
 * Fluxo:
 *   1. Mecânico seleciona arquivo no painel interno
 *   2. POST /api/ordens-servico/{id}/midias (com JWT)
 *   3. Arquivo salvo em disco, MidiaOS salva no banco
 *   4. SSE notifica cliente com a URL da nova mídia
 */
@Slf4j
@RestController
@RequestMapping("/ordens-servico")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Upload de Mídias", description = "Upload de fotos e vídeos pelo mecânico")
public class UploadController {

    private final OrdemServicoRepository ordemServicoRepository;
    private final MidiaOSRepository midiaOSRepository;
    private final SseEmitterService sseEmitterService;

    // Diretório onde os arquivos serão salvos (configurável em application.yml)
    @Value("${upload.dir:uploads}")
    private String uploadDir;

    // URL base para acesso aos arquivos (ex: "http://localhost:8080/api")
    @Value("${upload.base-url:/uploads}")
    private String baseUrl;

    /**
     * Recebe uma foto ou vídeo do mecânico e associa à OS.
     * Tipos aceitos: imagens (JPEG, PNG) e vídeos (MP4).
     */
    @PostMapping("/{id}/midias")
    @Operation(summary = "Mecânico envia foto ou vídeo da OS")
    public ResponseEntity<ApiResponse<Void>> uploadMidia(
        @PathVariable Long id,
        @RequestParam("arquivo") MultipartFile arquivo,
        @RequestParam(value = "legenda", required = false) String legenda
    ) throws IOException {

        OrdemServico os = ordemServicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: " + id));

        // Cria o diretório de uploads se não existir
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Gera nome único para evitar conflitos entre arquivos
        String extensao = getExtensao(arquivo.getOriginalFilename());
        String nomeArquivo = UUID.randomUUID() + extensao;
        Path destino = uploadPath.resolve(nomeArquivo);

        // Salva o arquivo em disco
        arquivo.transferTo(destino.toFile());

        // Determina se é foto ou vídeo pelo tipo MIME
        String tipo = arquivo.getContentType() != null && arquivo.getContentType().startsWith("video")
            ? "VIDEO" : "FOTO";

        String url = baseUrl + "/" + nomeArquivo;

        // Persiste o registro no banco vinculado à OS
        MidiaOS midia = MidiaOS.builder()
            .ordemServico(os)
            .nomeArquivo(nomeArquivo)
            .url(url)
            .tipo(tipo)
            .legenda(legenda)
            .build();
        midiaOSRepository.save(midia);

        // Notifica o cliente via SSE que uma nova mídia foi adicionada
        sseEmitterService.notificar(
            os.getTokenPublico(),
            "midia-adicionada",
            "{\"url\":\"" + url + "\",\"tipo\":\"" + tipo + "\"}"
        );

        log.info("Mídia {} adicionada à OS {} ({})", tipo, os.getNumero(), nomeArquivo);
        return ResponseEntity.ok(ApiResponse.ok("Mídia enviada com sucesso", null));
    }

    private String getExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) return ".bin";
        return nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
    }
}
