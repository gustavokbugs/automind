import { CheckCircle, Phone, Loader2 } from 'lucide-react'

/**
 * Card de aprovação do orçamento — visível na Etapa 2 (Diagnóstico).
 *
 * Exibe a lista de serviços e peças com seus valores para o cliente revisar.
 * O cliente pode aprovar com um clique, sem precisar ligar ou ir até a oficina.
 *
 * @param {Array} itens - Lista de itens do orçamento (serviços + peças)
 * @param {number} valorTotal - Valor total da OS
 * @param {boolean} aprovando - true enquanto a requisição de aprovação está em andamento
 * @param {Function} onAprovar - Callback chamado quando o cliente clica em Aprovar
 */
export default function OrcamentoAprovacao({ itens, valorTotal, aprovando, onAprovar }) {
  const formatarMoeda = (valor) =>
    new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor || 0)

  return (
    <div className="bg-white rounded-2xl shadow-sm border-2 border-blue-100">
      <div className="p-5">
        <h2 className="text-base font-semibold text-slate-800 mb-1">Orçamento para aprovação</h2>
        <p className="text-sm text-slate-500 mb-4">
          Revise os serviços abaixo e clique em Aprovar para darmos início ao trabalho.
        </p>

        {/* Lista de itens do orçamento */}
        {itens && itens.length > 0 ? (
          <div className="space-y-2 mb-4">
            {itens.map((item, index) => (
              <div key={index} className="flex justify-between items-center py-2 border-b border-slate-100 last:border-0">
                <div>
                  <p className="text-sm font-medium text-slate-800">{item.descricao}</p>
                  {item.quantidade > 1 && (
                    <p className="text-xs text-slate-400">{item.quantidade}x {formatarMoeda(item.precoUnitario)}</p>
                  )}
                </div>
                <p className="text-sm font-semibold text-slate-800">{formatarMoeda(item.subtotal)}</p>
              </div>
            ))}
          </div>
        ) : (
          <div className="py-4 text-center text-sm text-slate-400 mb-4">
            O mecânico ainda está preparando o orçamento detalhado.
          </div>
        )}

        {/* Total */}
        <div className="flex justify-between items-center bg-slate-50 rounded-xl p-3 mb-5">
          <span className="font-semibold text-slate-700">Total estimado</span>
          <span className="text-lg font-bold text-slate-900">{formatarMoeda(valorTotal)}</span>
        </div>

        {/* Botões de ação */}
        <div className="flex gap-3">
          <button
            onClick={onAprovar}
            disabled={aprovando}
            className="flex-1 bg-blue-600 text-white rounded-xl py-3 text-sm font-semibold
                       hover:bg-blue-700 active:bg-blue-800 disabled:opacity-60 disabled:cursor-not-allowed
                       flex items-center justify-center gap-2 transition-colors"
          >
            {aprovando ? (
              <><Loader2 className="h-4 w-4 animate-spin" /> Aprovando...</>
            ) : (
              <><CheckCircle className="h-4 w-4" /> Aprovar orçamento</>
            )}
          </button>

          <button className="flex items-center gap-2 border border-slate-200 rounded-xl px-4 py-3
                              text-sm font-medium text-slate-600 hover:bg-slate-50 transition-colors">
            <Phone className="h-4 w-4" />
            Ligar
          </button>
        </div>
      </div>
    </div>
  )
}
