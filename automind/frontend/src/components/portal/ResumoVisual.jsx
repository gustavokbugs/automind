import {
  ResponsiveContainer,
  RadialBarChart, RadialBar, PolarAngleAxis,
  PieChart, Pie, Cell,
  BarChart, Bar, XAxis, YAxis, Tooltip, LabelList,
} from 'recharts'
import { Gauge, PieChart as PieIcon, Wrench, Package, Receipt } from 'lucide-react'

/**
 * Resumo Visual do Portal do Cliente.
 *
 * Em vez de mostrar apenas números soltos, apresenta a informação da OS
 * de forma gráfica (estilo dashboard), usando a biblioteca Recharts:
 *
 *  - Medidor radial (gauge) com o progresso geral do serviço
 *  - Rosca (donut) com a composição do orçamento: Serviços x Peças
 *  - Barras horizontais com o investimento por item
 *
 * É somente leitura — reflete os dados públicos vindos do backend.
 */

// Paleta usada nos gráficos (mesma identidade do portal, tema claro)
const COR_SERVICO = '#2563eb' // azul
const COR_PECA = '#f59e0b'    // âmbar
const CORES_ITENS = ['#2563eb', '#0ea5e9', '#6366f1', '#8b5cf6', '#14b8a6', '#f59e0b']

const moeda = (v) =>
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v || 0)

/**
 * Converte o status da OS (e a aprovação do orçamento) em um percentual
 * de progresso, para alimentar o medidor radial.
 */
function calcularProgresso(status, orcamentoAprovado) {
  switch (status) {
    case 'ABERTA': return { pct: 15, etapa: 'Veículo recebido' }
    case 'EM_ANDAMENTO':
      return orcamentoAprovado
        ? { pct: 60, etapa: 'Serviço em execução' }
        : { pct: 40, etapa: 'Diagnóstico e orçamento' }
    case 'AGUARDANDO_PECA': return { pct: 70, etapa: 'Aguardando peça' }
    case 'EM_FINALIZACAO': return { pct: 88, etapa: 'Finalização e testes' }
    case 'CONCLUIDA': return { pct: 100, etapa: 'Pronto para retirada' }
    default: return { pct: 0, etapa: '—' }
  }
}

export default function ResumoVisual({ os }) {
  const { pct, etapa } = calcularProgresso(os.status, os.orcamentoAprovado)
  const itens = os.itens || []

  // Composição do orçamento: soma os subtotais agrupados por tipo (SERVICO / PECA)
  const totalServicos = itens
    .filter((i) => i.tipo === 'SERVICO')
    .reduce((acc, i) => acc + Number(i.subtotal || 0), 0)
  const totalPecas = itens
    .filter((i) => i.tipo === 'PECA')
    .reduce((acc, i) => acc + Number(i.subtotal || 0), 0)

  const composicao = [
    { nome: 'Serviços', valor: totalServicos, cor: COR_SERVICO },
    { nome: 'Peças', valor: totalPecas, cor: COR_PECA },
  ].filter((d) => d.valor > 0)

  const totalGeral = totalServicos + totalPecas

  // Investimento por item — top 6 por valor, do maior para o menor
  const porItem = [...itens]
    .map((i) => ({ nome: i.descricao, valor: Number(i.subtotal || 0) }))
    .filter((i) => i.valor > 0)
    .sort((a, b) => b.valor - a.valor)
    .slice(0, 6)

  // Cor do medidor de progresso conforme a fase
  const corProgresso =
    os.status === 'CONCLUIDA' ? '#16a34a'
    : os.status === 'AGUARDANDO_PECA' ? '#f59e0b'
    : '#2563eb'

  const temOrcamento = composicao.length > 0

  return (
    <div className="bg-white rounded-2xl shadow-sm p-5">
      <div className="flex items-center gap-2 mb-4">
        <Gauge className="h-5 w-5 text-blue-600" />
        <h2 className="text-base font-semibold text-slate-700">Resumo do serviço</h2>
      </div>

      <div className={`grid gap-4 ${temOrcamento ? 'sm:grid-cols-2' : 'grid-cols-1'}`}>
        {/* ---- Medidor radial de progresso ---- */}
        <div className="rounded-xl border border-slate-100 bg-slate-50/60 p-4 flex flex-col items-center">
          <p className="text-xs font-medium text-slate-500 mb-1 self-start">Progresso geral</p>
          <div className="relative w-full" style={{ height: 180 }}>
            <ResponsiveContainer width="100%" height="100%">
              <RadialBarChart
                innerRadius="72%"
                outerRadius="100%"
                data={[{ valor: pct, fill: corProgresso }]}
                startAngle={90}
                endAngle={-270}
              >
                <PolarAngleAxis type="number" domain={[0, 100]} tick={false} />
                <RadialBar background={{ fill: '#e2e8f0' }} dataKey="valor" cornerRadius={20} />
              </RadialBarChart>
            </ResponsiveContainer>
            {/* Texto central sobreposto ao gráfico */}
            <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
              <span className="text-3xl font-bold text-slate-800">{pct}%</span>
              <span className="text-[11px] text-slate-400">concluído</span>
            </div>
          </div>
          <p className="text-sm font-semibold text-slate-700 text-center mt-1">{etapa}</p>
        </div>

        {/* ---- Donut: composição do orçamento ---- */}
        {temOrcamento && (
          <div className="rounded-xl border border-slate-100 bg-slate-50/60 p-4">
            <div className="flex items-center gap-1.5 mb-1">
              <PieIcon className="h-3.5 w-3.5 text-slate-400" />
              <p className="text-xs font-medium text-slate-500">Composição do orçamento</p>
            </div>
            <div className="relative w-full" style={{ height: 180 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={composicao}
                    dataKey="valor"
                    nameKey="nome"
                    innerRadius="60%"
                    outerRadius="100%"
                    paddingAngle={2}
                    stroke="none"
                  >
                    {composicao.map((d) => (
                      <Cell key={d.nome} fill={d.cor} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v) => moeda(v)} />
                </PieChart>
              </ResponsiveContainer>
              <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                <span className="text-[11px] text-slate-400">Total</span>
                <span className="text-base font-bold text-slate-800">{moeda(totalGeral)}</span>
              </div>
            </div>
            {/* Legenda */}
            <div className="flex justify-center gap-4 mt-1">
              <span className="flex items-center gap-1.5 text-xs text-slate-600">
                <Wrench className="h-3 w-3" style={{ color: COR_SERVICO }} />
                Serviços {moeda(totalServicos)}
              </span>
              <span className="flex items-center gap-1.5 text-xs text-slate-600">
                <Package className="h-3 w-3" style={{ color: COR_PECA }} />
                Peças {moeda(totalPecas)}
              </span>
            </div>
          </div>
        )}
      </div>

      {/* ---- Barras: investimento por item ---- */}
      {porItem.length > 0 && (
        <div className="mt-4 rounded-xl border border-slate-100 bg-slate-50/60 p-4">
          <div className="flex items-center gap-1.5 mb-2">
            <Receipt className="h-3.5 w-3.5 text-slate-400" />
            <p className="text-xs font-medium text-slate-500">Investimento por item</p>
          </div>
          <ResponsiveContainer width="100%" height={Math.max(porItem.length * 42, 120)}>
            <BarChart
              data={porItem}
              layout="vertical"
              margin={{ top: 0, right: 64, bottom: 0, left: 8 }}
              barCategoryGap={10}
            >
              <XAxis type="number" hide />
              <YAxis
                type="category"
                dataKey="nome"
                width={120}
                tick={{ fontSize: 12, fill: '#475569' }}
                tickLine={false}
                axisLine={false}
              />
              <Tooltip
                cursor={{ fill: '#f1f5f9' }}
                formatter={(v) => [moeda(v), 'Valor']}
              />
              <Bar dataKey="valor" radius={[4, 4, 4, 4]} maxBarSize={26}>
                {porItem.map((d, i) => (
                  <Cell key={d.nome} fill={CORES_ITENS[i % CORES_ITENS.length]} />
                ))}
                <LabelList
                  dataKey="valor"
                  position="right"
                  formatter={(v) => moeda(v)}
                  style={{ fontSize: 11, fill: '#334155', fontWeight: 600 }}
                />
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  )
}
