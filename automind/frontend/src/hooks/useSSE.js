import { useEffect, useCallback } from 'react'

/**
 * Hook customizado para receber atualizações em tempo real via SSE (Server-Sent Events).
 *
 * SSE é uma tecnologia nativa do navegador que mantém uma conexão HTTP
 * persistente com o servidor. Diferente do WebSocket (bidirecional),
 * SSE é unidirecional: o servidor envia dados, o cliente só lê.
 *
 * Ideal para casos como este: cliente acompanha o status da OS sem precisar
 * ficar recarregando a página ou fazendo polling.
 *
 * @param {string} url - URL do endpoint SSE (ex: /api/public/os/{token}/eventos)
 * @param {Object} handlers - Objeto com handlers por nome de evento:
 *   { 'status-atualizado': (dados) => ..., 'midia-adicionada': (dados) => ... }
 */
export function useSSE(url, handlers) {
  const handlersRef = useCallback(handlers, [])

  useEffect(() => {
    if (!url) return

    // EventSource é a API nativa do navegador para SSE
    const source = new EventSource(url)

    // Ouve cada tipo de evento e chama o handler correspondente
    Object.keys(handlers).forEach(eventName => {
      source.addEventListener(eventName, (event) => {
        try {
          // Os dados chegam como string JSON — fazemos o parse aqui
          const dados = JSON.parse(event.data)
          handlers[eventName](dados)
        } catch {
          // Se não for JSON, passa o texto bruto
          handlers[eventName](event.data)
        }
      })
    })

    // Handler genérico para erros de conexão
    source.onerror = () => {
      // EventSource tenta reconectar automaticamente após erro
      // Não precisamos fazer nada aqui — é comportamento padrão
      console.warn('[SSE] Conexão perdida — tentando reconectar...')
    }

    // Cleanup: fecha a conexão quando o componente for desmontado
    // Evita memory leaks e conexões orphãs no servidor
    return () => {
      source.close()
    }
  }, [url])
}
