import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import axios from 'axios'
import { Car, AlertCircle, Loader2 } from 'lucide-react'
import { useSSE } from '../hooks/useSSE'
import TimelineOS from '../components/portal/TimelineOS'
import OrcamentoAprovacao from '../components/portal/OrcamentoAprovacao'
import MidiaTimeline from '../components/portal/MidiaTimeline'
import ExplicacaoIA from '../components/portal/ExplicacaoIA'

// Em produção (Docker/Nginx) usa URL relativa — o Nginx proxeia /api para o backend
// Em dev local (vite dev server) o proxy do vite.config.js faz o mesmo
const API_BASE = import.meta.env.VITE_API_URL || '/api'

/**
 * Página principal do Portal do Cliente.
 *
 * Acessada via URL pública: /os/{token}
 * Não exige login — qualquer pessoa com o link pode ver.
 *
 * Exibe uma linha do tempo progressiva que se atualiza automaticamente
 * via SSE (Server-Sent Events) conforme o mecânico avança no serviço.
 */
export default function PortalClientePage() {
  const { token } = useParams()
  const [os, setOs] = useState(null)
  const [loading, setLoading] = useState(true)
  const [erro, setErro] = useState(null)
  const [aprovando, setAprovando] = useState(false)

  // Busca os dados da OS ao carregar a página
  useEffect(() => {
    buscarOS()
  }, [token])

  const buscarOS = async () => {
    try {
      setLoading(true)
      const { data } = await axios.get(`${API_BASE}/public/os/${token}`)
      setOs(data.data)
    } catch (err) {
      setErro('Link inválido ou OS não encontrada. Verifique o link enviado pela oficina.')
    } finally {
      setLoading(false)
    }
  }

  // SSE: recebe atualizações em tempo real quando o mecânico muda o status ou envia mídias
  useSSE(os ? `${API_BASE}/public/os/${token}/eventos` : null, {
    'status-atualizado': (dados) => {
      // Atualiza apenas o status sem buscar tudo de novo
      setOs(prev => prev ? { ...prev, status: dados.status } : prev)
    },
    'orcamento-aprovado': () => {
      setOs(prev => prev ? { ...prev, orcamentoAprovado: true } : prev)
    },
    'midia-adicionada': () => {
      // Quando chega nova mídia, busca os dados completos para ter a URL
      buscarOS()
    }
  })

  const handleAprovarOrcamento = async () => {
    try {
      setAprovando(true)
      await axios.post(`${API_BASE}/public/os/${token}/aprovar-orcamento`)
      setOs(prev => prev ? { ...prev, orcamentoAprovado: true } : prev)
    } catch (err) {
      alert('Erro ao aprovar orçamento. Tente novamente.')
    } finally {
      setAprovando(false)
    }
  }

  // ===== Renderização =====

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="h-10 w-10 text-blue-600 animate-spin mx-auto mb-4" />
          <p className="text-slate-600">Carregando informações do seu veículo...</p>
        </div>
      </div>
    )
  }

  if (erro) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-2xl shadow-sm p-8 max-w-md w-full text-center">
          <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-slate-800 mb-2">Link não encontrado</h2>
          <p className="text-slate-500">{erro}</p>
        </div>
      </div>
    )
  }

  if (!os) return null

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Cabeçalho da página — sem dados internos da oficina */}
      <header className="bg-white border-b border-slate-200">
        <div className="max-w-2xl mx-auto px-4 py-5">
          <div className="flex items-center gap-3">
            <div className="bg-blue-600 rounded-xl p-2">
              <Car className="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-slate-900">Acompanhe seu veículo</h1>
              <p className="text-sm text-slate-500">OS {os.numero}</p>
            </div>
          </div>
        </div>
      </header>

      {/* Informações do veículo */}
      <div className="max-w-2xl mx-auto px-4 py-6 space-y-6">
        <div className="bg-white rounded-2xl shadow-sm p-5">
          <h2 className="text-base font-semibold text-slate-700 mb-3">Seu veículo</h2>
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div>
              <span className="text-slate-400">Veículo</span>
              <p className="font-medium text-slate-800">{os.veiculoMarca} {os.veiculoModelo}</p>
            </div>
            <div>
              <span className="text-slate-400">Placa</span>
              <p className="font-medium text-slate-800">{os.veiculoPlaca}</p>
            </div>
            <div>
              <span className="text-slate-400">Cor</span>
              <p className="font-medium text-slate-800">{os.veiculoCor}</p>
            </div>
            <div>
              <span className="text-slate-400">Ano</span>
              <p className="font-medium text-slate-800">{os.veiculoAno}</p>
            </div>
            {os.mecanicoNome && (
              <div className="col-span-2">
                <span className="text-slate-400">Técnico responsável</span>
                <p className="font-medium text-slate-800">{os.mecanicoNome}</p>
              </div>
            )}
            <div>
              <span className="text-slate-400">Entrada</span>
              <p className="font-medium text-slate-800">
                {new Date(os.abertaEm).toLocaleString('pt-BR', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' })}
              </p>
            </div>
            {os.previsaoEntrega && (
              <div>
                <span className="text-slate-400">Previsão</span>
                <p className="font-medium text-slate-800">
                  {new Date(os.previsaoEntrega).toLocaleString('pt-BR', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' })}
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Linha do tempo do progresso */}
        <TimelineOS status={os.status} />

        {/* Orçamento — visível para aprovação na Etapa 2 */}
        {os.status === 'EM_ANDAMENTO' && !os.orcamentoAprovado && (
          <OrcamentoAprovacao
            itens={os.itens}
            valorTotal={os.valorTotal}
            aprovando={aprovando}
            onAprovar={handleAprovarOrcamento}
          />
        )}

        {/* Mídias (fotos/vídeos) — visíveis da Etapa 3 em diante */}
        {['EM_ANDAMENTO', 'AGUARDANDO_PECA', 'EM_FINALIZACAO', 'CONCLUIDA'].includes(os.status) &&
          os.orcamentoAprovado && os.midias?.length > 0 && (
          <MidiaTimeline midias={os.midias} />
        )}

        {/* Explicação IA — visível apenas quando CONCLUIDA */}
        {os.status === 'CONCLUIDA' && os.explicacaoCliente && (
          <ExplicacaoIA
            explicacao={os.explicacaoCliente}
            valorTotal={os.valorTotal}
          />
        )}
      </div>

      {/* Rodapé simples */}
      <footer className="max-w-2xl mx-auto px-4 pb-8 text-center">
        <p className="text-xs text-slate-400">
          Esta página atualiza automaticamente. Não é necessário recarregar.
        </p>
      </footer>
    </div>
  )
}
