import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, ClipboardList, CheckCircle, X, ExternalLink } from 'lucide-react'
import { ordensAPI, veiculosAPI, mecanicosAPI } from '../services/api'
import { Table, Tr, Td } from '../components/ui/Table'
import Modal from '../components/ui/Modal'
import toast from 'react-hot-toast'

const STATUS_BADGE = {
  ABERTA: 'badge-aberta',
  EM_ANDAMENTO: 'badge-andamento',
  AGUARDANDO_PECA: 'badge-andamento',
  EM_FINALIZACAO: 'badge-andamento',
  CONCLUIDA: 'badge-concluida',
  CANCELADA: 'badge-cancelada',
}

/** Gera o link público do Portal do Cliente para uma OS */
const getLinkPortal = (os) => {
  const base = import.meta.env.VITE_PORTAL_URL || window.location.origin
  return `${base}/os/${os.tokenPublico}`
}

export default function OrdensServicoPage() {
  const qc = useQueryClient()
  const [statusFiltro, setStatusFiltro] = useState('')
  const [modal, setModal] = useState(false)
  const [detalheOS, setDetalheOS] = useState(null)
  const [form, setForm] = useState({ veiculoId: '', mecanicoId: '', quilometragemEntrada: 0, observacoes: '', itens: [] })
  const [concluirModal, setConcluirModal] = useState(null)
  const [kmSaida, setKmSaida] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['ordens', statusFiltro],
    queryFn: () => ordensAPI.listar({ status: statusFiltro || undefined, size: 20, sort: 'abertaEm,desc' }).then(r => r.data.dados),
  })

  const { data: veiculos } = useQuery({
    queryKey: ['veiculos-all'],
    queryFn: () => veiculosAPI.listar({ size: 999 }).then(r => r.data.dados?.content || []),
  })

  const { data: mecanicos } = useQuery({
    queryKey: ['mecanicos'],
    queryFn: () => mecanicosAPI.listar().then(r => r.data.dados || []),
  })

  const criar = useMutation({
    mutationFn: (f) => ordensAPI.criar(f),
    onSuccess: () => { qc.invalidateQueries(['ordens']); toast.success('OS aberta!'); setModal(false) },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro'),
  })

  const concluir = useMutation({
    mutationFn: ({ id, data }) => ordensAPI.concluir(id, data),
    onSuccess: () => { qc.invalidateQueries(['ordens']); toast.success('OS concluída!'); setConcluirModal(null) },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro'),
  })

  const cancelar = useMutation({
    mutationFn: (id) => ordensAPI.atualizarStatus(id, { status: 'CANCELADA' }),
    onSuccess: () => { qc.invalidateQueries(['ordens']); toast.success('OS cancelada') },
  })

  const iniciar = useMutation({
    mutationFn: (id) => ordensAPI.atualizarStatus(id, { status: 'EM_ANDAMENTO' }),
    onSuccess: () => { qc.invalidateQueries(['ordens']); toast.success('OS em andamento') },
  })

  const fmt = (v) => v ? new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v) : '—'
  const fmtDate = (d) => d ? new Date(d).toLocaleDateString('pt-BR') : '—'

  const ordens = data?.content || []

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row gap-3 justify-between">
        <div className="flex gap-2 flex-wrap">
          {['', 'ABERTA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'].map(s => (
            <button
              key={s}
              onClick={() => setStatusFiltro(s)}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${statusFiltro === s ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-400 hover:text-white'}`}
            >
              {s || 'Todas'}
            </button>
          ))}
        </div>
        <button className="btn-primary flex items-center gap-2" onClick={() => setModal(true)}>
          <Plus size={16} /> Nova OS
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {isLoading ? (
          <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500" /></div>
        ) : (
          <Table headers={['Número', 'Veículo', 'Cliente', 'Mecânico', 'Status', 'Valor', 'Data', 'Ações']}>
            {ordens.map(os => (
              <Tr key={os.id} onClick={() => setDetalheOS(os)}>
                <Td><span className="font-mono text-blue-400 text-xs">{os.numero}</span></Td>
                <Td>
                  <p className="text-white font-medium">{os.veiculoPlaca}</p>
                  <p className="text-xs text-slate-500">{os.veiculoModelo}</p>
                </Td>
                <Td className="text-slate-400">{os.clienteNome}</Td>
                <Td className="text-slate-400">{os.mecanicoNome || '—'}</Td>
                <Td><span className={STATUS_BADGE[os.status] || 'badge-aberta'}>{os.status.replace('_', ' ')}</span></Td>
                <Td className="text-green-400">{fmt(os.valorTotal)}</Td>
                <Td className="text-slate-500 text-xs">{fmtDate(os.abertaEm)}</Td>
                <Td onClick={e => e.stopPropagation()}>
                  <div className="flex gap-1 items-center">
                    {/* Link do Portal do Cliente — copiado com um clique */}
                    {os.tokenPublico && (
                      <button
                        title="Abrir link do Portal do Cliente"
                        onClick={() => {
                          const link = getLinkPortal(os)
                          navigator.clipboard.writeText(link).then(() => toast.success('Link copiado!'))
                          window.open(link, '_blank')
                        }}
                        className="text-slate-400 hover:text-blue-400 p-1"
                      >
                        <ExternalLink size={14} />
                      </button>
                    )}
                    {os.status === 'ABERTA' && (
                      <>
                        <button title="Iniciar" onClick={() => iniciar.mutate(os.id)} className="text-slate-400 hover:text-yellow-400 p-1">▶</button>
                        <button title="Cancelar" onClick={() => cancelar.mutate(os.id)} className="text-slate-400 hover:text-red-400 p-1"><X size={14} /></button>
                      </>
                    )}
                    {os.status === 'EM_ANDAMENTO' && (
                      <button title="Concluir" onClick={() => { setConcluirModal(os); setKmSaida(os.quilometragemEntrada) }} className="text-slate-400 hover:text-green-400 p-1">
                        <CheckCircle size={14} />
                      </button>
                    )}
                  </div>
                </Td>
              </Tr>
            ))}
          </Table>
        )}
        {ordens.length === 0 && !isLoading && <p className="text-center text-slate-500 py-8">Nenhuma OS encontrada</p>}
      </div>

      {/* Modal Nova OS */}
      <Modal isOpen={modal} onClose={() => setModal(false)} title="Abrir Ordem de Serviço">
        <form onSubmit={(e) => { e.preventDefault(); criar.mutate({...form, veiculoId: Number(form.veiculoId), mecanicoId: form.mecanicoId ? Number(form.mecanicoId) : null, quilometragemEntrada: Number(form.quilometragemEntrada), itens: [] }) }} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="label">Veículo *</label>
              <select className="input" value={form.veiculoId} onChange={e => setForm({...form, veiculoId: e.target.value})} required>
                <option value="">Selecione...</option>
                {veiculos?.map(v => <option key={v.id} value={v.id}>{v.placa} — {v.marca} {v.modelo} ({v.clienteNome})</option>)}
              </select>
            </div>
            <div>
              <label className="label">Mecânico</label>
              <select className="input" value={form.mecanicoId} onChange={e => setForm({...form, mecanicoId: e.target.value})}>
                <option value="">A definir</option>
                {mecanicos?.map(m => <option key={m.id} value={m.id}>{m.nome}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Quilometragem de Entrada *</label>
              <input className="input" type="number" value={form.quilometragemEntrada} onChange={e => setForm({...form, quilometragemEntrada: e.target.value})} min={0} required />
            </div>
            <div className="col-span-2">
              <label className="label">Observações</label>
              <textarea className="input" rows={3} value={form.observacoes} onChange={e => setForm({...form, observacoes: e.target.value})} placeholder="Descreva o problema relatado pelo cliente..." />
            </div>
          </div>
          <div className="flex justify-end gap-3">
            <button type="button" className="btn-secondary" onClick={() => setModal(false)}>Cancelar</button>
            <button type="submit" className="btn-primary" disabled={criar.isPending}>{criar.isPending ? 'Abrindo...' : 'Abrir OS'}</button>
          </div>
        </form>
      </Modal>

      {/* Modal Concluir */}
      <Modal isOpen={!!concluirModal} onClose={() => setConcluirModal(null)} title="Concluir OS" size="sm">
        {concluirModal && (
          <div className="space-y-4">
            <p className="text-slate-400">OS: <span className="text-white font-mono">{concluirModal.numero}</span></p>
            <div>
              <label className="label">Quilometragem de Saída *</label>
              <input className="input" type="number" value={kmSaida} onChange={e => setKmSaida(Number(e.target.value))} min={concluirModal.quilometragemEntrada} required />
            </div>
            <div className="flex justify-end gap-3">
              <button className="btn-secondary" onClick={() => setConcluirModal(null)}>Cancelar</button>
              <button className="btn-primary" onClick={() => concluir.mutate({ id: concluirModal.id, data: { quilometragemSaida: kmSaida } })} disabled={concluir.isPending}>
                Concluir OS
              </button>
            </div>
          </div>
        )}
      </Modal>

      {/* Modal Detalhe */}
      <Modal isOpen={!!detalheOS} onClose={() => setDetalheOS(null)} title={`OS ${detalheOS?.numero}`} size="lg">
        {detalheOS && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div><span className="text-slate-400">Veículo:</span> <span className="text-white">{detalheOS.veiculoPlaca} — {detalheOS.veiculoModelo}</span></div>
              <div><span className="text-slate-400">Cliente:</span> <span className="text-white">{detalheOS.clienteNome}</span></div>
              <div><span className="text-slate-400">KM Entrada:</span> <span className="text-white">{detalheOS.quilometragemEntrada?.toLocaleString('pt-BR')} km</span></div>
              <div><span className="text-slate-400">KM Saída:</span> <span className="text-white">{detalheOS.quilometragemSaida?.toLocaleString('pt-BR') || '—'} km</span></div>
              {detalheOS.observacoes && <div className="col-span-2"><span className="text-slate-400">Obs:</span> <span className="text-white">{detalheOS.observacoes}</span></div>}
              {detalheOS.diagnostico && <div className="col-span-2"><span className="text-slate-400">Diagnóstico:</span> <span className="text-white">{detalheOS.diagnostico}</span></div>}
            </div>
            {detalheOS.itens?.length > 0 && (
              <div>
                <h4 className="text-sm font-medium text-slate-300 mb-2">Itens da OS</h4>
                <div className="space-y-2">
                  {detalheOS.itens.map(item => (
                    <div key={item.id} className="flex justify-between text-sm p-2 bg-slate-800 rounded">
                      <span className="text-slate-300">{item.servicoNome || item.pecaNome} × {item.quantidade}</span>
                      <span className="text-green-400">{fmt(item.subtotal)}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
            <div className="flex justify-between items-center p-3 bg-slate-800 rounded-lg">
              <span className="text-slate-300 font-medium">Total</span>
              <span className="text-green-400 font-bold text-lg">{fmt(detalheOS.valorTotal)}</span>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}
