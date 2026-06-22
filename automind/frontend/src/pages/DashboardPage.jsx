import { useQuery } from '@tanstack/react-query'
import { Users, Car, ClipboardList, DollarSign, AlertTriangle, TrendingUp } from 'lucide-react'
import { dashboardAPI } from '../services/api'
import StatCard from '../components/ui/StatCard'

export default function DashboardPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: () => dashboardAPI.get().then(r => r.data.dados),
    refetchInterval: 30000,
  })

  if (isLoading) return (
    <div className="flex items-center justify-center h-64">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-500" />
    </div>
  )

  const fmt = (v) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v || 0)

  return (
    <div className="space-y-6">
      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Total de Clientes" value={data?.totalClientes ?? 0} icon={Users} color="blue" />
        <StatCard title="Total de Veículos" value={data?.totalVeiculos ?? 0} icon={Car} color="purple" />
        <StatCard title="OS Abertas" value={data?.ordensAbertas ?? 0} icon={ClipboardList} color="yellow" subtitle={`${data?.ordensEmAndamento ?? 0} em andamento`} />
        <StatCard title="Faturamento Mensal" value={fmt(data?.faturamentoMensal)} icon={DollarSign} color="green" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Peças com estoque baixo */}
        <div className="card">
          <div className="flex items-center gap-2 mb-4">
            <AlertTriangle size={18} className="text-red-400" />
            <h3 className="font-semibold text-white">Peças com Estoque Baixo</h3>
          </div>
          {data?.pecasEstoqueBaixo?.length === 0 ? (
            <p className="text-slate-500 text-sm">Estoque normalizado ✓</p>
          ) : (
            <div className="space-y-3">
              {data?.pecasEstoqueBaixo?.map((p) => (
                <div key={p.id} className="flex items-center justify-between p-3 bg-slate-800 rounded-lg">
                  <div>
                    <p className="text-sm font-medium text-white">{p.nome}</p>
                    <p className="text-xs text-slate-400">{p.codigo}</p>
                  </div>
                  <div className="text-right">
                    <span className="text-red-400 font-bold text-sm">{p.quantidadeEstoque}</span>
                    <p className="text-xs text-slate-500">mín: {p.estoqueMinimo}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Serviços mais realizados */}
        <div className="card">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp size={18} className="text-blue-400" />
            <h3 className="font-semibold text-white">Serviços Mais Realizados</h3>
          </div>
          {data?.servicosMaisRealizados?.length === 0 ? (
            <p className="text-slate-500 text-sm">Nenhum serviço registrado ainda</p>
          ) : (
            <div className="space-y-3">
              {data?.servicosMaisRealizados?.map((s, i) => (
                <div key={s.tipo} className="flex items-center gap-3">
                  <span className="text-xs font-bold text-slate-500 w-5">#{i + 1}</span>
                  <div className="flex-1">
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-slate-300">{s.tipo.replace(/_/g, ' ')}</span>
                      <span className="text-white font-medium">{s.quantidade}x</span>
                    </div>
                    <div className="h-1.5 bg-slate-700 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-blue-500 rounded-full"
                        style={{ width: `${Math.min((s.quantidade / (data.servicosMaisRealizados[0]?.quantidade || 1)) * 100, 100)}%` }}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
