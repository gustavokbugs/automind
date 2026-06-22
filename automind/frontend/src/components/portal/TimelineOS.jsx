import { Check, Clock, Wrench, Package, Star, AlertCircle } from 'lucide-react'

/**
 * Componente de linha do tempo da OS no Portal do Cliente.
 *
 * Exibe as 6 etapas do serviço visualmente, destacando a etapa atual.
 * As etapas anteriores aparecem como concluídas (verde com check).
 * A etapa atual é destacada em azul com animação pulsante.
 * As etapas futuras aparecem em cinza (inativas).
 *
 * @param {string} status - StatusOS atual da OS vindo do backend
 */
export default function TimelineOS({ status }) {
  // Define a ordem das etapas e qual status as ativa
  const etapas = [
    {
      id: 'ABERTA',
      titulo: 'Veículo Recebido',
      descricao: 'Seu veículo deu entrada na oficina e aguarda diagnóstico.',
      icone: Clock,
      cor: 'blue'
    },
    {
      id: 'EM_ANDAMENTO_DIAGNOSTICO', // Estado virtual: EM_ANDAMENTO sem aprovação
      titulo: 'Diagnóstico e Orçamento',
      descricao: 'Estamos avaliando seu veículo e preparando o orçamento.',
      icone: AlertCircle,
      cor: 'blue'
    },
    {
      id: 'EM_ANDAMENTO_EXECUCAO', // Estado virtual: EM_ANDAMENTO com aprovação
      titulo: 'Execução do Serviço',
      descricao: 'Serviço em andamento. Acompanhe as fotos abaixo.',
      icone: Wrench,
      cor: 'blue'
    },
    {
      id: 'AGUARDANDO_PECA',
      titulo: 'Aguardando Peça',
      descricao: 'Pausamos temporariamente para a chegada de uma peça.',
      icone: Package,
      cor: 'yellow'
    },
    {
      id: 'EM_FINALIZACAO',
      titulo: 'Finalizando',
      descricao: 'Serviço concluído! Passando pelo teste de segurança.',
      icone: Star,
      cor: 'blue'
    },
    {
      id: 'CONCLUIDA',
      titulo: 'Pronto para Retirada',
      descricao: 'Tudo pronto! Seu carro está te esperando.',
      icone: Check,
      cor: 'green'
    }
  ]

  /**
   * Calcula qual etapa visual está ativa com base no status da OS.
   * O status EM_ANDAMENTO pode ser tanto Etapa 2 (diagnóstico) quanto
   * Etapa 3 (execução), dependendo se o orçamento foi aprovado.
   * O componente pai passa o status e deixa a lógica aqui.
   */
  const getEtapaAtiva = () => {
    const map = {
      'ABERTA': 0,
      'EM_ANDAMENTO': 1,       // Diagnóstico (sem aprovação — controlado pelo pai)
      'AGUARDANDO_PECA': 3,
      'EM_FINALIZACAO': 4,
      'CONCLUIDA': 5,
      'CANCELADA': -1
    }
    return map[status] ?? 0
  }

  const etapaAtiva = getEtapaAtiva()

  return (
    <div className="bg-white rounded-2xl shadow-sm p-5">
      <h2 className="text-base font-semibold text-slate-700 mb-5">Progresso do serviço</h2>

      <div className="relative">
        {etapas.map((etapa, index) => {
          const Icone = etapa.icone
          const isConcluida = index < etapaAtiva
          const isAtiva = index === etapaAtiva
          const isFutura = index > etapaAtiva

          return (
            <div key={etapa.id} className="flex gap-4 mb-5 last:mb-0">
              {/* Ícone e linha vertical conectora */}
              <div className="flex flex-col items-center">
                <div className={`
                  w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0 z-10
                  ${isConcluida ? 'bg-green-500 text-white' : ''}
                  ${isAtiva ? `bg-blue-600 text-white ${etapa.cor === 'yellow' ? 'bg-yellow-500' : ''} ring-4 ring-blue-100` : ''}
                  ${isFutura ? 'bg-slate-100 text-slate-400' : ''}
                `}>
                  <Icone className="h-4 w-4" />
                </div>
                {/* Linha vertical que conecta os círculos */}
                {index < etapas.length - 1 && (
                  <div className={`w-0.5 h-8 mt-1 ${isConcluida ? 'bg-green-300' : 'bg-slate-200'}`} />
                )}
              </div>

              {/* Texto da etapa */}
              <div className="pb-2">
                <p className={`text-sm font-semibold ${isAtiva ? 'text-blue-600' : isConcluida ? 'text-green-600' : 'text-slate-400'}`}>
                  {etapa.titulo}
                </p>
                {(isAtiva || isConcluida) && (
                  <p className="text-xs text-slate-500 mt-0.5">{etapa.descricao}</p>
                )}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
