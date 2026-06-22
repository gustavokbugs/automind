import axios from 'axios'
import toast from 'react-hot-toast'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('automind_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const msg = error.response?.data?.mensagem || 'Erro inesperado'
    if (error.response?.status === 401) {
      localStorage.removeItem('automind_token')
      localStorage.removeItem('automind_user')
      window.location.href = '/login'
    } else if (error.response?.status !== 422) {
      toast.error(msg)
    }
    return Promise.reject(error)
  }
)

// Auth
export const authAPI = {
  login: (data) => api.post('/auth/login', data),
}

// Clientes
export const clientesAPI = {
  listar: (params) => api.get('/clientes', { params }),
  buscarPorId: (id) => api.get(`/clientes/${id}`),
  criar: (data) => api.post('/clientes', data),
  atualizar: (id, data) => api.put(`/clientes/${id}`, data),
  inativar: (id) => api.delete(`/clientes/${id}`),
}

// Veículos
export const veiculosAPI = {
  listar: (params) => api.get('/veiculos', { params }),
  buscarPorId: (id) => api.get(`/veiculos/${id}`),
  listarPorCliente: (clienteId) => api.get(`/veiculos/cliente/${clienteId}`),
  criar: (data) => api.post('/veiculos', data),
  atualizar: (id, data) => api.put(`/veiculos/${id}`, data),
}

// Mecânicos
export const mecanicosAPI = {
  listar: () => api.get('/mecanicos'),
  buscarPorId: (id) => api.get(`/mecanicos/${id}`),
  criar: (data) => api.post('/mecanicos', data),
  atualizar: (id, data) => api.put(`/mecanicos/${id}`, data),
}

// Peças
export const pecasAPI = {
  listar: (params) => api.get('/pecas', { params }),
  buscarPorId: (id) => api.get(`/pecas/${id}`),
  criar: (data) => api.post('/pecas', data),
  atualizar: (id, data) => api.put(`/pecas/${id}`, data),
  ajustarEstoque: (id, quantidade) => api.patch(`/pecas/${id}/estoque`, { quantidade }),
}

// Serviços
export const servicosAPI = {
  listar: () => api.get('/servicos'),
}

// Ordens de Serviço
export const ordensAPI = {
  listar: (params) => api.get('/ordens-servico', { params }),
  buscarPorId: (id) => api.get(`/ordens-servico/${id}`),
  criar: (data) => api.post('/ordens-servico', data),
  atualizarStatus: (id, data) => api.patch(`/ordens-servico/${id}/status`, data),
  concluir: (id, data) => api.post(`/ordens-servico/${id}/concluir`, data),
  historicoPorVeiculo: (veiculoId) => api.get(`/ordens-servico/veiculo/${veiculoId}/historico`),
}

// Recomendações
export const recomendacoesAPI = {
  gerar: (veiculoId) => api.post(`/recomendacoes/veiculo/${veiculoId}/gerar`),
  listar: (veiculoId) => api.get(`/recomendacoes/veiculo/${veiculoId}`),
  marcarVisualizada: (id) => api.patch(`/recomendacoes/${id}/visualizar`),
}

// Dashboard
export const dashboardAPI = {
  get: () => api.get('/dashboard'),
}

export default api
