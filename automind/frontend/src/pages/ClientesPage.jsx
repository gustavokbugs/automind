import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Search, Edit, Trash2, User } from 'lucide-react'
import { clientesAPI } from '../services/api'
import { Table, Tr, Td } from '../components/ui/Table'
import Modal from '../components/ui/Modal'
import toast from 'react-hot-toast'

const EMPTY = { nome: '', cpf: '', email: '', telefone: '', endereco: '' }

export default function ClientesPage() {
  const qc = useQueryClient()
  const [termo, setTermo] = useState('')
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(EMPTY)
  const [editId, setEditId] = useState(null)
  const [page, setPage] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['clientes', termo, page],
    queryFn: () => clientesAPI.listar({ termo, page, size: 10 }).then(r => r.data.dados),
  })

  const salvar = useMutation({
    mutationFn: (f) => editId ? clientesAPI.atualizar(editId, f) : clientesAPI.criar(f),
    onSuccess: () => {
      qc.invalidateQueries(['clientes'])
      toast.success(editId ? 'Cliente atualizado!' : 'Cliente cadastrado!')
      fechar()
    },
    onError: (err) => toast.error(err.response?.data?.mensagem || 'Erro ao salvar'),
  })

  const inativar = useMutation({
    mutationFn: (id) => clientesAPI.inativar(id),
    onSuccess: () => { qc.invalidateQueries(['clientes']); toast.success('Cliente inativado') },
  })

  const abrir = (cliente = null) => {
    setEditId(cliente?.id || null)
    setForm(cliente ? { nome: cliente.nome, cpf: cliente.cpf, email: cliente.email, telefone: cliente.telefone, endereco: cliente.endereco || '' } : EMPTY)
    setModal(true)
  }

  const fechar = () => { setModal(false); setForm(EMPTY); setEditId(null) }

  const handleSubmit = (e) => { e.preventDefault(); salvar.mutate(form) }

  const clientes = data?.content || []
  const totalPages = data?.totalPages || 0

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex flex-col sm:flex-row gap-3 justify-between">
        <div className="relative flex-1 max-w-sm">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            className="input pl-9"
            placeholder="Buscar por nome, CPF ou e-mail..."
            value={termo}
            onChange={e => { setTermo(e.target.value); setPage(0) }}
          />
        </div>
        <button className="btn-primary flex items-center gap-2" onClick={() => abrir()}>
          <Plus size={16} /> Novo Cliente
        </button>
      </div>

      {/* Table */}
      <div className="card p-0 overflow-hidden">
        {isLoading ? (
          <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500" /></div>
        ) : (
          <>
            <Table headers={['Nome', 'CPF', 'E-mail', 'Telefone', 'Veículos', 'Ações']}>
              {clientes.map(c => (
                <Tr key={c.id}>
                  <Td>
                    <div className="flex items-center gap-2">
                      <div className="w-8 h-8 bg-blue-900 rounded-full flex items-center justify-center flex-shrink-0">
                        <User size={14} className="text-blue-300" />
                      </div>
                      <span className="font-medium text-white">{c.nome}</span>
                    </div>
                  </Td>
                  <Td>{c.cpf}</Td>
                  <Td>{c.email}</Td>
                  <Td>{c.telefone}</Td>
                  <Td><span className="badge-aberta">{c.totalVeiculos} veículo(s)</span></Td>
                  <Td>
                    <div className="flex gap-2">
                      <button onClick={() => abrir(c)} className="text-slate-400 hover:text-blue-400 transition-colors p-1">
                        <Edit size={15} />
                      </button>
                      <button onClick={() => inativar.mutate(c.id)} className="text-slate-400 hover:text-red-400 transition-colors p-1">
                        <Trash2 size={15} />
                      </button>
                    </div>
                  </Td>
                </Tr>
              ))}
            </Table>
            {clientes.length === 0 && (
              <p className="text-center text-slate-500 py-8">Nenhum cliente encontrado</p>
            )}
          </>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex justify-center gap-2 p-4 border-t border-slate-800">
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i}
                onClick={() => setPage(i)}
                className={`w-8 h-8 rounded-lg text-sm ${page === i ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-400 hover:text-white'}`}
              >{i + 1}</button>
            ))}
          </div>
        )}
      </div>

      {/* Modal */}
      <Modal isOpen={modal} onClose={fechar} title={editId ? 'Editar Cliente' : 'Novo Cliente'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="sm:col-span-2">
              <label className="label">Nome completo *</label>
              <input className="input" value={form.nome} onChange={e => setForm({...form, nome: e.target.value})} required minLength={3} />
            </div>
            <div>
              <label className="label">CPF *</label>
              <input className="input" value={form.cpf} onChange={e => setForm({...form, cpf: e.target.value})} placeholder="000.000.000-00" required />
            </div>
            <div>
              <label className="label">Telefone *</label>
              <input className="input" value={form.telefone} onChange={e => setForm({...form, telefone: e.target.value})} placeholder="(11) 99999-9999" required />
            </div>
            <div className="sm:col-span-2">
              <label className="label">E-mail *</label>
              <input className="input" type="email" value={form.email} onChange={e => setForm({...form, email: e.target.value})} required />
            </div>
            <div className="sm:col-span-2">
              <label className="label">Endereço</label>
              <input className="input" value={form.endereco} onChange={e => setForm({...form, endereco: e.target.value})} />
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
