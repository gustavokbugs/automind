import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Search, Edit, AlertTriangle, Package } from 'lucide-react'
import { pecasAPI } from '../services/api'
import { Table, Tr, Td } from '../components/ui/Table'
import Modal from '../components/ui/Modal'
import toast from 'react-hot-toast'

const EMPTY = { codigo: '', nome: '', descricao: '', precoCompra: '', precoVenda: '', quantidadeEstoque: 0, estoqueMinimo: 5, fabricante: '' }

export default function PecasPage() {
  const qc = useQueryClient()
  const [termo, setTermo] = useState('')
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(EMPTY)
  const [editId, setEditId] = useState(null)
  const [ajusteModal, setAjusteModal] = useState(null)
  const [ajusteQtd, setAjusteQtd] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['pecas', termo],
    queryFn: () => pecasAPI.listar({ termo, size: 20 }).then(r => r.data.dados),
  })

  const salvar = useMutation({
    mutationFn: (f) => editId ? pecasAPI.atualizar(editId, f) : pecasAPI.criar(f),
    onSuccess: () => { qc.invalidateQueries(['pecas']); toast.success('Salvo!'); fechar() },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro'),
  })

  const ajustar = useMutation({
    mutationFn: ({ id, qtd }) => pecasAPI.ajustarEstoque(id, qtd),
    onSuccess: () => { qc.invalidateQueries(['pecas']); toast.success('Estoque ajustado'); setAjusteModal(null) },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro'),
  })

  const abrir = (p = null) => {
    setEditId(p?.id || null)
    setForm(p ? { codigo: p.codigo, nome: p.nome, descricao: p.descricao || '', precoCompra: p.precoCompra, precoVenda: p.precoVenda, quantidadeEstoque: p.quantidadeEstoque, estoqueMinimo: p.estoqueMinimo, fabricante: p.fabricante || '' } : EMPTY)
    setModal(true)
  }

  const fechar = () => { setModal(false); setForm(EMPTY); setEditId(null) }

  const fmt = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

  const pecas = data?.content || []

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row gap-3 justify-between">
        <div className="relative flex-1 max-w-sm">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input className="input pl-9" placeholder="Código ou nome..." value={termo} onChange={e => setTermo(e.target.value)} />
        </div>
        <button className="btn-primary flex items-center gap-2" onClick={() => abrir()}>
          <Plus size={16} /> Nova Peça
        </button>
      </div>

      <div className="card p-0 overflow-hidden">
        {isLoading ? (
          <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500" /></div>
        ) : (
          <Table headers={['Código', 'Nome', 'Estoque', 'Mínimo', 'Preço Venda', 'Ações']}>
            {pecas.map(p => (
              <Tr key={p.id}>
                <Td><span className="font-mono text-blue-400">{p.codigo}</span></Td>
                <Td>
                  <div className="flex items-center gap-2">
                    <Package size={14} className="text-slate-500" />
                    <div>
                      <p className="text-white">{p.nome}</p>
                      {p.fabricante && <p className="text-xs text-slate-500">{p.fabricante}</p>}
                    </div>
                  </div>
                </Td>
                <Td>
                  <div className="flex items-center gap-1">
                    {p.quantidadeEstoque <= p.estoqueMinimo && <AlertTriangle size={14} className="text-red-400" />}
                    <span className={p.quantidadeEstoque <= p.estoqueMinimo ? 'text-red-400 font-bold' : 'text-white'}>
                      {p.quantidadeEstoque}
                    </span>
                  </div>
                </Td>
                <Td className="text-slate-400">{p.estoqueMinimo}</Td>
                <Td className="text-green-400 font-medium">{fmt(p.precoVenda)}</Td>
                <Td>
                  <div className="flex gap-2">
                    <button onClick={() => abrir(p)} className="text-slate-400 hover:text-blue-400 p-1"><Edit size={15} /></button>
                    <button onClick={() => { setAjusteModal(p); setAjusteQtd(0) }} className="text-slate-400 hover:text-green-400 p-1 text-xs font-medium">±Estoque</button>
                  </div>
                </Td>
              </Tr>
            ))}
          </Table>
        )}
        {pecas.length === 0 && !isLoading && <p className="text-center text-slate-500 py-8">Nenhuma peça encontrada</p>}
      </div>

      {/* Modal Cadastro */}
      <Modal isOpen={modal} onClose={fechar} title={editId ? 'Editar Peça' : 'Nova Peça'}>
        <form onSubmit={(e) => { e.preventDefault(); salvar.mutate({...form, precoCompra: Number(form.precoCompra), precoVenda: Number(form.precoVenda), quantidadeEstoque: Number(form.quantidadeEstoque), estoqueMinimo: Number(form.estoqueMinimo) }) }} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Código *</label>
              <input className="input uppercase" value={form.codigo} onChange={e => setForm({...form, codigo: e.target.value.toUpperCase()})} required />
            </div>
            <div>
              <label className="label">Fabricante</label>
              <input className="input" value={form.fabricante} onChange={e => setForm({...form, fabricante: e.target.value})} />
            </div>
            <div className="col-span-2">
              <label className="label">Nome *</label>
              <input className="input" value={form.nome} onChange={e => setForm({...form, nome: e.target.value})} required />
            </div>
            <div className="col-span-2">
              <label className="label">Descrição</label>
              <textarea className="input" rows={2} value={form.descricao} onChange={e => setForm({...form, descricao: e.target.value})} />
            </div>
            <div>
              <label className="label">Preço de Compra *</label>
              <input className="input" type="number" step="0.01" value={form.precoCompra} onChange={e => setForm({...form, precoCompra: e.target.value})} required />
            </div>
            <div>
              <label className="label">Preço de Venda *</label>
              <input className="input" type="number" step="0.01" value={form.precoVenda} onChange={e => setForm({...form, precoVenda: e.target.value})} required />
            </div>
            <div>
              <label className="label">Qtd. Estoque *</label>
              <input className="input" type="number" value={form.quantidadeEstoque} onChange={e => setForm({...form, quantidadeEstoque: e.target.value})} min={0} required />
            </div>
            <div>
              <label className="label">Estoque Mínimo *</label>
              <input className="input" type="number" value={form.estoqueMinimo} onChange={e => setForm({...form, estoqueMinimo: e.target.value})} min={0} required />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" className="btn-secondary" onClick={fechar}>Cancelar</button>
            <button type="submit" className="btn-primary" disabled={salvar.isPending}>{salvar.isPending ? 'Salvando...' : 'Salvar'}</button>
          </div>
        </form>
      </Modal>

      {/* Modal Ajuste Estoque */}
      <Modal isOpen={!!ajusteModal} onClose={() => setAjusteModal(null)} title="Ajustar Estoque" size="sm">
        {ajusteModal && (
          <div className="space-y-4">
            <p className="text-slate-400">Peça: <span className="text-white font-medium">{ajusteModal.nome}</span></p>
            <p className="text-slate-400">Estoque atual: <span className="text-white font-bold">{ajusteModal.quantidadeEstoque}</span></p>
            <div>
              <label className="label">Quantidade (positivo = entrada, negativo = saída)</label>
              <input className="input" type="number" value={ajusteQtd} onChange={e => setAjusteQtd(Number(e.target.value))} />
            </div>
            <div className="flex justify-end gap-3">
              <button className="btn-secondary" onClick={() => setAjusteModal(null)}>Cancelar</button>
              <button className="btn-primary" onClick={() => ajustar.mutate({ id: ajusteModal.id, qtd: ajusteQtd })} disabled={ajustar.isPending}>
                Confirmar
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}
