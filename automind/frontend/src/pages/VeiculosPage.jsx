import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Search, Edit, Car, Zap } from 'lucide-react'
import { veiculosAPI, clientesAPI, recomendacoesAPI } from '../services/api'
import { Table, Tr, Td } from '../components/ui/Table'
import Modal from '../components/ui/Modal'
import toast from 'react-hot-toast'

const EMPTY = { placa: '', marca: '', modelo: '', ano: new Date().getFullYear(), cor: '', quilometragemAtual: 0, chassis: '', clienteId: '' }

export default function VeiculosPage() {
  const qc = useQueryClient()
  const [termo, setTermo] = useState('')
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(EMPTY)
  const [editId, setEditId] = useState(null)

  const { data, isLoading } = useQuery({
    queryKey: ['veiculos', termo],
    queryFn: () => veiculosAPI.listar({ termo, size: 20 }).then(r => r.data.dados),
  })

  const { data: clientes } = useQuery({
    queryKey: ['clientes-all'],
    queryFn: () => clientesAPI.listar({ size: 999 }).then(r => r.data.dados?.content || []),
  })

  const salvar = useMutation({
    mutationFn: (f) => editId ? veiculosAPI.atualizar(editId, f) : veiculosAPI.criar(f),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['veiculos'] }); toast.success('Salvo!'); fechar() },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro'),
  })

  const gerarRecomendacoes = async (veiculoId) => {
    const toastId = toast.loading('Gerando recomendações...')
    try {
      const { data } = await recomendacoesAPI.gerar(veiculoId)
      toast.success(data.mensagem, { id: toastId })
    } catch {
      toast.error('Erro ao gerar recomendações', { id: toastId })
    }
  }

  const abrir = (v = null) => {
    setEditId(v?.id || null)
    setForm(v ? { placa: v.placa, marca: v.marca, modelo: v.modelo, ano: v.ano, cor: v.cor, quilometragemAtual: v.quilometragemAtual, chassis: v.chassis || '', clienteId: v.clienteId } : EMPTY)
    setModal(true)
  }

  const fechar = () => { setModal(false); setForm(EMPTY); setEditId(null) }

  const veiculos = data?.content || []

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row gap-3 justify-between">
        <div className="relative flex-1 max-w-sm">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input className="input pl-9" placeholder="Placa, modelo ou marca..." value={termo} onChange={e => setTermo(e.target.value)} />
        </div>
        <button className="btn-primary flex items-center gap-2" onClick={() => abrir()}>
          <Plus size={16} /> Novo Veículo
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {isLoading ? (
          <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500" /></div>
        ) : (
          <Table headers={['Placa', 'Veículo', 'Ano', 'KM Atual', 'Cliente', 'Ações']}>
            {veiculos.map(v => (
              <Tr key={v.id}>
                <Td>
                  <span className="font-mono font-bold text-blue-400 bg-blue-900/30 px-2 py-0.5 rounded">{v.placa}</span>
                </Td>
                <Td>
                  <div className="flex items-center gap-2">
                    <Car size={15} className="text-slate-500" />
                    <span className="text-white">{v.marca} {v.modelo}</span>
                  </div>
                </Td>
                <Td>{v.ano}</Td>
                <Td>{v.quilometragemAtual?.toLocaleString('pt-BR')} km</Td>
                <Td className="text-slate-400">{v.clienteNome}</Td>
                <Td>
                  <div className="flex gap-2">
                    <button onClick={() => abrir(v)} className="text-slate-400 hover:text-blue-400 p-1">
                      <Edit size={15} />
                    </button>
                    <button onClick={() => gerarRecomendacoes(v.id)} title="Gerar recomendações" className="text-slate-400 hover:text-yellow-400 p-1">
                      <Zap size={15} />
                    </button>
                  </div>
                </Td>
              </Tr>
            ))}
          </Table>
        )}
        {veiculos.length === 0 && !isLoading && <p className="text-center text-slate-500 py-8">Nenhum veículo encontrado</p>}
      </div>

      <Modal isOpen={modal} onClose={fechar} title={editId ? 'Editar Veículo' : 'Novo Veículo'}>
        <form onSubmit={(e) => { e.preventDefault(); salvar.mutate({...form, clienteId: Number(form.clienteId), ano: Number(form.ano), quilometragemAtual: Number(form.quilometragemAtual) }) }} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Placa *</label>
              <input className="input uppercase" value={form.placa} onChange={e => setForm({...form, placa: e.target.value.toUpperCase()})} placeholder="ABC1234" required />
            </div>
            <div>
              <label className="label">Cor *</label>
              <input className="input" value={form.cor} onChange={e => setForm({...form, cor: e.target.value})} required />
            </div>
            <div>
              <label className="label">Marca *</label>
              <input className="input" value={form.marca} onChange={e => setForm({...form, marca: e.target.value})} required />
            </div>
            <div>
              <label className="label">Modelo *</label>
              <input className="input" value={form.modelo} onChange={e => setForm({...form, modelo: e.target.value})} required />
            </div>
            <div>
              <label className="label">Ano *</label>
              <input className="input" type="number" value={form.ano} onChange={e => setForm({...form, ano: e.target.value})} min={1900} max={2100} required />
            </div>
            <div>
              <label className="label">Quilometragem *</label>
              <input className="input" type="number" value={form.quilometragemAtual} onChange={e => setForm({...form, quilometragemAtual: e.target.value})} min={0} required />
            </div>
            <div>
              <label className="label">Chassis</label>
              <input className="input" value={form.chassis} onChange={e => setForm({...form, chassis: e.target.value})} maxLength={17} />
            </div>
            <div>
              <label className="label">Cliente *</label>
              <select className="input" value={form.clienteId} onChange={e => setForm({...form, clienteId: e.target.value})} required>
                <option value="">Selecione...</option>
                {clientes?.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}
              </select>
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" className="btn-secondary" onClick={fechar}>Cancelar</button>
            <button type="submit" className="btn-primary" disabled={salvar.isPending}>
              {salvar.isPending ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
