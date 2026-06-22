import { Sparkles } from 'lucide-react'

/**
 * Card com a explicação dos serviços gerada por IA — visível apenas na Etapa 6 (CONCLUIDA).
 *
 * Transforma linguagem técnica do mecânico em texto simples para o cliente leigo.
 * Gerada uma única vez ao concluir a OS e salva no banco — não é regenerada.
 *
 * Exemplo:
 *   Técnico registrou: "Substituição da correia dentada e tensionador"
 *   Cliente lê: "Trocamos a correia dentada do seu carro, que é a peça responsável
 *                por sincronizar o motor..."
 *
 * @param {string} explicacao - Texto gerado pela IA
 * @param {number} valorTotal - Valor final da OS para exibição
 */
export default function ExplicacaoIA({ explicacao, valorTotal }) {
  const formatarMoeda = (valor) =>
    new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor || 0)

  return (
    <div className="bg-gradient-to-br from-blue-600 to-blue-700 rounded-2xl shadow-sm text-white p-5">
      {/* Badge IA */}
      <div className="flex items-center gap-2 mb-4">
        <div className="bg-white/20 rounded-full p-1.5">
          <Sparkles className="h-4 w-4" />
        </div>
        <span className="text-sm font-semibold">Resumo do serviço</span>
        <span className="ml-auto text-xs bg-white/20 rounded-full px-2 py-0.5">
          Gerado por IA
        </span>
      </div>

      {/* Explicação em linguagem simples */}
      <p className="text-sm leading-relaxed text-blue-50 whitespace-pre-line">
        {explicacao}
      </p>

      {/* Valor final em destaque */}
      {valorTotal && (
        <div className="mt-4 pt-4 border-t border-white/20 flex justify-between items-center">
          <span className="text-sm text-blue-100">Valor final</span>
          <span className="text-xl font-bold">{formatarMoeda(valorTotal)}</span>
        </div>
      )}
    </div>
  )
}
