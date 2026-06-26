import { useQuery } from '@tanstack/react-query'
import { Users, Car, ClipboardList, DollarSign, AlertTriangle, TrendingUp, Activity } from 'lucide-react'
import {
  ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Cell, LabelList,
  PieChart, Pie, Legend,
} from 'recharts'
import { dashboardAPI } from '../services/api'
import StatCard from '../components/ui/StatCard'

// Paleta dos gráficos (tema escuro)
const CORES_SERVICOS = ['#3b82f6', '#6366f1', '#8b5cf6', '#0ea5e9', '#14b8a6']
const COR_ESTOQUE = '#ef4444'
const COR_MINIMO = '#475569'

// Estilo padrão do tooltip do recharts no tema escuro
const tooltipStyle = {
  contentStyle: { background: '#0f172a', border: '1px solid #1e293b', borderRadius: 8, color: '#e2e8f0' },
  labelStyle: { color: '#94a3b8' },
  itemStyle: { color: '#e2e8f0' },
}

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

  // ----- Dados derivados para os gráficos -----

  // Serviços mais realizados → barras horizontais
  const servicos = (data?.servicosMaisRealizados ?? []).map(s => ({
    nome: s.tipo.replace(/_/g, ' '),
    quantidade: Number(s.quantidade),
  }))

  // Peças com estoque baixo → barras agrupadas (atual x mínimo)
  const estoque = (data?.pecasEstoqueBaixo ?? []).map(p => ({
    nome: p.nome,
    atual: p.quantidadeEstoque,
    minimo: p.estoqueMinimo,
  }))

  // Situação das OS → rosca (donut)
  const situacaoOS = [
    { nome: 'Abertas', valor: Number(data?.ordensAbertas ?? 0), cor: '#3b82f6' },
    { nome: 'Em andamento', valor: Number(data?.ordensEmAndamento ?? 0), cor: '#eab308' },
  ].filter(d => d.valor > 0)

  return (
    <div className="space-y-6">
      {/* KPIs */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Total de Clientes" value={data?.totalClientes ?? 0} icon={Users} color="blue" />
        <StatCard title="Total de Veículos" value={data?.totalVeiculos ?? 0} icon={Car} color="purple" />
        <StatCard title="OS Abertas" value={data?.ordensAbertas ?? 0} icon={ClipboardList} color="yellow" subtitle={`${data?.ordensEmAndamento ?? 0} em andamento`} />
        <StatCard title="Faturamento Mensal" value={fmt(data?.faturamentoMensal)} icon={DollarSign} color="green" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Serviços mais realizados — barras horizontais */}
        <div className="card lg:col-span-2">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp size={18} className="text-blue-400" />
            <h3 className="font-semibold text-white">Serviços Mais Realizados</h3>
          </div>
          {servicos.length === 0 ? (
            <p className="text-slate-500 text-sm">Nenhum serviço registrado ainda</p>
          ) : (
            <ResponsiveContainer width="100%" height={Math.max(servicos.length * 48, 160)}>
              <BarChart data={servicos} layout="vertical" margin={{ left: 8, right: 40, top: 4, bottom: 4 }}>
                <CartesianGrid horizontal={false} stroke="#1e293b" />
                <XAxis type="number" tick={{ fontSize: 12, fill: '#94a3b8' }} axisLine={{ stroke: '#1e293b' }} tickLine={false} allowDecimals={false} />
                <YAxis type="category" dataKey="nome" width={130} tick={{ fontSize: 12, fill: '#cbd5e1' }} axisLine={false} tickLine={false} />
                <Tooltip cursor={{ fill: '#1e293b' }} {...tooltipStyle} formatter={(v) => [`${v}x`, 'Realizados']} />
                <Bar dataKey="quantidade" radius={[4, 4, 4, 4]} maxBarSize={28}>
                  {servicos.map((s, i) => <Cell key={s.nome} fill={CORES_SERVICOS[i % CORES_SERVICOS.length]} />)}
                  <LabelList dataKey="quantidade" position="right" formatter={(v) => `${v}x`} style={{ fontSize: 12, fill: '#e2e8f0', fontWeight: 600 }} />
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Situação das OS — donut */}
        <div className="card">
          <div className="flex items-center gap-2 mb-4">
            <Activity size={18} className="text-yellow-400" />
            <h3 className="font-semibold text-white">Situação das OS</h3>
          </div>
          {situacaoOS.length === 0 ? (
            <p className="text-slate-500 text-sm">Sem ordens ativas no momento</p>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={situacaoOS} dataKey="valor" nameKey="nome" innerRadius="58%" outerRadius="100%" paddingAngle={3} stroke="none">
                  {situacaoOS.map((d) => <Cell key={d.nome} fill={d.cor} />)}
                </Pie>
                <Tooltip {...tooltipStyle} />
                <Legend wrapperStyle={{ fontSize: 12, color: '#cbd5e1' }} iconType="circle" />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* Peças com estoque baixo — barras agrupadas (atual x mínimo) */}
      <div className="card">
        <div className="flex items-center gap-2 mb-4">
          <AlertTriangle size={18} className="text-red-400" />
          <h3 className="font-semibold text-white">Peças com Estoque Baixo</h3>
        </div>
        {estoque.length === 0 ? (
          <p className="text-slate-500 text-sm">Estoque normalizado ✓</p>
        ) : (
          <ResponsiveContainer width="100%" height={Math.max(estoque.length * 46, 180)}>
            <BarChart data={estoque} layout="vertical" margin={{ left: 8, right: 24, top: 4, bottom: 4 }} barGap={2}>
              <CartesianGrid horizontal={false} stroke="#1e293b" />
              <XAxis type="number" tick={{ fontSize: 12, fill: '#94a3b8' }} axisLine={{ stroke: '#1e293b' }} tickLine={false} allowDecimals={false} />
              <YAxis type="category" dataKey="nome" width={140} tick={{ fontSize: 12, fill: '#cbd5e1' }} axisLine={false} tickLine={false} />
              <Tooltip cursor={{ fill: '#1e293b' }} {...tooltipStyle} />
              <Legend wrapperStyle={{ fontSize: 12, color: '#cbd5e1' }} iconType="circle" />
              <Bar dataKey="atual" name="Estoque atual" fill={COR_ESTOQUE} radius={[4, 4, 4, 4]} maxBarSize={14} />
              <Bar dataKey="minimo" name="Estoque mínimo" fill={COR_MINIMO} radius={[4, 4, 4, 4]} maxBarSize={14} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  )
}
