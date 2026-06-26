import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, ClipboardList, CheckCircle, X, ExternalLink, Trash2, Wrench, Package } from 'lucide-react'
import { ordensAPI, veiculosAPI, mecanicosAPI, servicosAPI, pecasAPI } from '../services/api'
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
  // Formulário do editor de itens (dentro do modal de detalhe)
  const [itemForm, setItemForm] = useState({ tipo: 'SERVICO', refId: '', quantidade: 1, precoUnitario: '' })

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

  // Catálogos usados no editor de itens da OS
  const { data: servicos } = useQuery({
    queryKey: ['servicos'],
    queryFn: () => servicosAPI.listar().then(r => r.data.dados || []),
  })

  const { data: pecas } = useQuery({
    queryKey: ['pecas-all'],
    queryFn: () => pecasAPI.listar({ size: 999 }).then(r => r.data.dados?.content || []),
  })

  const criar = useMutation({
    mutationFn: (f) => ordensAPI.criar(f),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['ordens'] }); toast.success('OS aberta!'); setModal(false) },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro'),
  })

  const concluir = useMutation({
    mutationFn: ({ id, data }) => ordensAPI.concluir(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['ordens'] }); toast.success('OS concluída!'); setConcluirModal(null) },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro'),
  })

  const cancelar = useMutation({
    mutationFn: (id) => ordensAPI.atualizarStatus(id, { status: 'CANCELADA' }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['ordens'] }); toast.success('OS cancelada') },
  })

  const iniciar = useMutation({
    mutationFn: (id) => ordensAPI.atualizarStatus(id, { status: 'EM_ANDAMENTO' }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['ordens'] }); toast.success('OS em andamento') },
  })

  const addItem = useMutation({
    mutationFn: ({ osId, item }) => ordensAPI.adicionarItem(osId, item),
    onSuccess: (resp) => {
      setDetalheOS(resp.data.dados)               // mantém o modal em sincronia com o total novo
      setItemForm({ tipo: 'SERVICO', refId: '', quantidade: 1, precoUnitario: '' })
      qc.invalidateQueries({ queryKey: ['ordens'] })
      toast.success('Item adicionado ao orçamento')
    },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro ao adicionar item'),
  })

  const removeItem = useMutation({
    mutationFn: ({ osId, itemId }) => ordensAPI.removerItem(osId, itemId),
    onSuccess: (resp) => {
      setDetalheOS(resp.data.dados)
      qc.invalidateQueries({ queryKey: ['ordens'] })
      toast.success('Item removido')
    },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro ao remover item'),
  })

  // Ao escolher um serviço/peça, preenche o preço sugerido automaticamente
  const selecionarRef = (refId) => {
    let preco = ''
    if (refId) {
      if (itemForm.tipo === 'SERVICO') preco = servicos?.find(s => s.id === Number(refId))?.precoBase ?? ''
      else preco = pecas?.find(p => p.id === Number(refId))?.precoVenda ?? ''
    }
    setItemForm(f => ({ ...f, refId, precoUnitario: preco }))
  }

  const submeterItem = () => {
    if (!itemForm.refId) { toast.error('Selecione um serviço ou peça'); return }
    const item = {
      quantidade: Number(itemForm.quantidade),
      precoUnitario: Number(itemForm.precoUnitario),
      ...(itemForm.tipo === 'SERVICO' ? { servicoId: Number(itemForm.refId) } : { pecaId: Number(itemForm.refId) }),
    }
    addItem.mutate({ osId: detalheOS.id, item })
  }

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
            {/* Itens do orçamento — editáveis enquanto a OS não estiver concluída/cancelada */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <h4 className="text-sm font-medium text-slate-300">Itens do orçamento</h4>
                {['CONCLUIDA', 'CANCELADA'].includes(detalheOS.status) && (
                  <span className="text-xs text-slate-500">OS finalizada — somente leitura</span>
                )}
              </div>

              <div className="space-y-2">
                {detalheOS.itens?.length > 0 ? detalheOS.itens.map(item => (
                  <div key={item.id} className="flex justify-between items-center text-sm p-2 bg-slate-800 rounded">
                    <span className="flex items-center gap-2 text-slate-300">
                      {item.servicoId
                        ? <Wrench size={13} className="text-blue-400" />
                        : <Package size={13} className="text-amber-400" />}
                      {item.servicoNome || item.pecaNome} × {item.quantidade}
                      <span className="text-slate-500 text-xs">({fmt(item.precoUnitario)} un.)</span>
                    </span>
                    <span className="flex items-center gap-3">
                      <span className="text-green-400">{fmt(item.subtotal)}</span>
                      {!['CONCLUIDA', 'CANCELADA'].includes(detalheOS.status) && (
                        <button
                          onClick={() => removeItem.mutate({ osId: detalheOS.id, itemId: item.id })}
                          disabled={removeItem.isPending}
                          className="text-slate-500 hover:text-red-400 transition-colors"
                          title="Remover item"
                        >
                          <Trash2 size={14} />
                        </button>
                      )}
                    </span>
                  </div>
                )) : (
                  <p className="text-sm text-slate-500 py-2">Nenhum item no orçamento ainda.</p>
                )}
              </div>
            </div>

            {/* Formulário para adicionar item ao orçamento */}
            {!['CONCLUIDA', 'CANCELADA'].includes(detalheOS.status) && (
              <div className="border border-slate-700 rounded-lg p-3 space-y-3">
                <p className="text-sm font-medium text-slate-300">Adicionar item</p>

                {/* Alterna entre Serviço e Peça */}
                <div className="flex gap-2">
                  {['SERVICO', 'PECA'].map(t => (
                    <button
                      key={t}
                      type="button"
                      onClick={() => setItemForm({ tipo: t, refId: '', quantidade: 1, precoUnitario: '' })}
                      className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${itemForm.tipo === t ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-400 hover:text-white'}`}
                    >
                      {t === 'SERVICO' ? 'Serviço' : 'Peça'}
                    </button>
                  ))}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-12 gap-2 items-end">
                  <div className="sm:col-span-6">
                    <label className="label">{itemForm.tipo === 'SERVICO' ? 'Serviço' : 'Peça'}</label>
                    <select className="input" value={itemForm.refId} onChange={e => selecionarRef(e.target.value)}>
                      <option value="">Selecione...</option>
                      {itemForm.tipo === 'SERVICO'
                        ? servicos?.map(s => <option key={s.id} value={s.id}>{s.nome} — {fmt(s.precoBase)}</option>)
                        : pecas?.map(p => <option key={p.id} value={p.id}>{p.nome} ({p.quantidadeEstoque} em estoque) — {fmt(p.precoVenda)}</option>)}
                    </select>
                  </div>
                  <div className="sm:col-span-2">
                    <label className="label">Qtd</label>
                    <input className="input" type="number" min={1} value={itemForm.quantidade}
                      onChange={e => setItemForm(f => ({ ...f, quantidade: e.target.value }))} />
                  </div>
                  <div className="sm:col-span-2">
                    <label className="label">Preço un.</label>
                    <input className="input" type="number" step="0.01" min="0.01" value={itemForm.precoUnitario}
                      onChange={e => setItemForm(f => ({ ...f, precoUnitario: e.target.value }))} />
                  </div>
                  <div className="sm:col-span-2">
                    <button type="button" onClick={submeterItem} disabled={addItem.isPending}
                      className="btn-primary w-full flex items-center justify-center gap-1">
                      <Plus size={14} /> Add
                    </button>
                  </div>
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
