import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Lightbulb, AlertTriangle, CheckCircle, Zap, Search } from 'lucide-react'
import { recomendacoesAPI, veiculosAPI } from '../services/api'
import Modal from '../components/ui/Modal'
import toast from 'react-hot-toast'

const TIPO_LABELS = {
  TROCA_OLEO: 'Troca de Óleo',
  TROCA_CORREIA_DENTADA: 'Correia Dentada',
  TROCA_PASTILHA_FREIO: 'Pastilhas de Freio',
  TROCA_PNEU: 'Troca de Pneus',
  REVISAO_GERAL: 'Revisão Geral',
  ALINHAMENTO: 'Alinhamento',
  BALANCEAMENTO: 'Balanceamento',
}

const TIPO_COLORS = {
  TROCA_OLEO: 'from-amber-900/40 to-amber-800/10 border-amber-700/30',
  TROCA_CORREIA_DENTADA: 'from-red-900/40 to-red-800/10 border-red-700/30',
  TROCA_PASTILHA_FREIO: 'from-orange-900/40 to-orange-800/10 border-orange-700/30',
  TROCA_PNEU: 'from-blue-900/40 to-blue-800/10 border-blue-700/30',
  REVISAO_GERAL: 'from-green-900/40 to-green-800/10 border-green-700/30',
}

export default function RecomendacoesPage() {
  const qc = useQueryClient()
  const [veiculoSelecionado, setVeiculoSelecionado] = useState(null)
  const [modalVeiculo, setModalVeiculo] = useState(false)
  const [termoBusca, setTermoBusca] = useState('')

  const { data: veiculos } = useQuery({
    queryKey: ['veiculos-all'],
    queryFn: () => veiculosAPI.listar({ size: 999 }).then(r => r.data.dados?.content || []),
  })

  const { data: recomendacoes, isLoading } = useQuery({
    queryKey: ['recomendacoes', veiculoSelecionado?.id],
    queryFn: () => recomendacoesAPI.listar(veiculoSelecionado.id).then(r => r.data.dados || []),
    enabled: !!veiculoSelecionado,
  })

  const gerar = useMutation({
    mutationFn: () => recomendacoesAPI.gerar(veiculoSelecionado.id),
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ['recomendacoes', veiculoSelecionado?.id] })
      toast.success(data.data.mensagem)
    },
  })

  const marcarVisualizada = useMutation({
    mutationFn: (id) => recomendacoesAPI.marcarVisualizada(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['recomendacoes', veiculoSelecionado?.id] }),
  })

  const veiculosFiltrados = veiculos?.filter(v =>
    v.placa.toLowerCase().includes(termoBusca.toLowerCase()) ||
    v.modelo.toLowerCase().includes(termoBusca.toLowerCase()) ||
    v.marca.toLowerCase().includes(termoBusca.toLowerCase())
  )

  const urgentes = recomendacoes?.filter(r => r.urgente && !r.visualizada) || []
  const normais = recomendacoes?.filter(r => !r.urgente && !r.visualizada) || []
  const visualizadas = recomendacoes?.filter(r => r.visualizada) || []

  return (
    <div className="space-y-6">
      {/* Seleção de veículo */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Lightbulb size={18} className="text-yellow-400" />
            <h3 className="font-semibold text-white">Motor de Recomendações</h3>
          </div>
          <button className="btn-secondary text-sm" onClick={() => setModalVeiculo(true)}>
            {veiculoSelecionado ? `${veiculoSelecionado.placa} — ${veiculoSelecionado.marca} ${veiculoSelecionado.modelo}` : 'Selecionar Veículo'}
          </button>
        </div>

        {!veiculoSelecionado ? (
          <div className="text-center py-8">
            <Lightbulb size={48} className="text-slate-700 mx-auto mb-3" />
            <p className="text-slate-400">Selecione um veículo para ver e gerar recomendações de manutenção preventiva</p>
          </div>
        ) : (
          <div className="flex items-center justify-between p-4 bg-slate-800 rounded-xl">
            <div>
              <p className="text-white font-semibold">{veiculoSelecionado.placa}</p>
              <p className="text-slate-400 text-sm">{veiculoSelecionado.marca} {veiculoSelecionado.modelo} • {veiculoSelecionado.quilometragemAtual?.toLocaleString('pt-BR')} km</p>
            </div>
            <button
              className="btn-primary flex items-center gap-2"
              onClick={() => gerar.mutate()}
              disabled={gerar.isPending}
            >
              <Zap size={16} />
              {gerar.isPending ? 'Analisando...' : 'Gerar Recomendações'}
            </button>
          </div>
        )}
      </div>

      {/* Recomendações */}
      {veiculoSelecionado && (
        <>
          {isLoading && (
            <div className="flex justify-center py-8"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500" /></div>
          )}

          {urgentes.length > 0 && (
            <div className="space-y-3">
              <div className="flex items-center gap-2">
                <AlertTriangle size={16} className="text-red-400" />
                <h3 className="font-semibold text-red-400">Urgentes ({urgentes.length})</h3>
              </div>
              {urgentes.map(r => <RecomendacaoCard key={r.id} rec={r} onMarcar={() => marcarVisualizada.mutate(r.id)} />)}
            </div>
          )}

          {normais.length > 0 && (
            <div className="space-y-3">
              <div className="flex items-center gap-2">
                <Lightbulb size={16} className="text-yellow-400" />
                <h3 className="font-semibold text-yellow-400">Recomendadas ({normais.length})</h3>
              </div>
              {normais.map(r => <RecomendacaoCard key={r.id} rec={r} onMarcar={() => marcarVisualizada.mutate(r.id)} />)}
            </div>
          )}

          {urgentes.length === 0 && normais.length === 0 && !isLoading && (
            <div className="card text-center py-8">
              <CheckCircle size={48} className="text-green-400 mx-auto mb-3" />
              <p className="text-white font-medium">Veículo em dia!</p>
              <p className="text-slate-400 text-sm mt-1">Nenhuma manutenção pendente no momento</p>
            </div>
          )}

          {visualizadas.length > 0 && (
            <details className="card">
              <summary className="cursor-pointer text-slate-400 text-sm">Histórico visualizado ({visualizadas.length})</summary>
              <div className="mt-3 space-y-2">
                {visualizadas.map(r => <RecomendacaoCard key={r.id} rec={r} visualizada />)}
              </div>
            </details>
          )}
        </>
      )}

      {/* Modal seleção de veículo */}
      <Modal isOpen={modalVeiculo} onClose={() => setModalVeiculo(false)} title="Selecionar Veículo">
        <div className="space-y-3">
          <div className="relative">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input className="input pl-9" placeholder="Buscar veículo..." value={termoBusca} onChange={e => setTermoBusca(e.target.value)} />
          </div>
          <div className="space-y-2 max-h-80 overflow-y-auto">
            {veiculosFiltrados?.map(v => (
              <button
                key={v.id}
                onClick={() => { setVeiculoSelecionado(v); setModalVeiculo(false) }}
                className="w-full flex items-center justify-between p-3 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors text-left"
              >
                <div>
                  <p className="text-white font-medium">{v.placa}</p>
                  <p className="text-slate-400 text-sm">{v.marca} {v.modelo} — {v.clienteNome}</p>
                </div>
                <span className="text-slate-500 text-xs">{v.quilometragemAtual?.toLocaleString('pt-BR')} km</span>
              </button>
            ))}
          </div>
        </div>
      </Modal>
    </div>
  )
}

function RecomendacaoCard({ rec, onMarcar, visualizada = false }) {
  const bgClass = TIPO_COLORS[rec.tipoServico] || 'from-slate-900/40 to-slate-800/10 border-slate-700/30'
  return (
    <div className={`bg-gradient-to-r ${bgClass} border rounded-xl p-4 flex items-start justify-between gap-4 ${visualizada ? 'opacity-50' : ''}`}>
      <div className="flex-1">
        <div className="flex items-center gap-2 mb-1">
          {rec.urgente && !visualizada && <AlertTriangle size={14} className="text-red-400" />}
          <span className="text-sm font-semibold text-white">{TIPO_LABELS[rec.tipoServico] || rec.tipoServico}</span>
        </div>
        <p className="text-slate-300 text-sm">{rec.descricao}</p>
        <div className="flex gap-3 mt-2 text-xs text-slate-500">
          {rec.kmRecomendado && <span>📍 {rec.kmRecomendado.toLocaleString('pt-BR')} km</span>}
          {rec.dataRecomendada && <span>📅 {new Date(rec.dataRecomendada).toLocaleDateString('pt-BR')}</span>}
        </div>
      </div>
      {onMarcar && !visualizada && (
        <button onClick={onMarcar} className="text-slate-400 hover:text-green-400 transition-colors p-1 flex-shrink-0" title="Marcar como visto">
          <CheckCircle size={18} />
        </button>
      )}
    </div>
  )
}
