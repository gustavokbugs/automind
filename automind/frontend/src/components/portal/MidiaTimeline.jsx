import { useState } from 'react'
import { Camera, Video, X } from 'lucide-react'

/**
 * Galeria de fotos e vídeos enviados pelo mecânico — visível na Etapa 3.
 *
 * O cliente vê literalmente o que está sendo feito no carro dele,
 * em tempo real. Isso aumenta a transparência e confiança no serviço.
 *
 * @param {Array} midias - Lista de MidiaPublicaDTO com url, tipo e legenda
 */
export default function MidiaTimeline({ midias }) {
  // Controla o lightbox (visualização ampliada da foto/vídeo)
  const [midiaAberta, setMidiaAberta] = useState(null)

  if (!midias || midias.length === 0) return null

  return (
    <div className="bg-white rounded-2xl shadow-sm p-5">
      <div className="flex items-center gap-2 mb-4">
        <Camera className="h-5 w-5 text-blue-600" />
        <h2 className="text-base font-semibold text-slate-800">
          Acompanhe o serviço
        </h2>
        <span className="ml-auto text-xs text-slate-400">{midias.length} arquivo(s)</span>
      </div>

      {/* Grid de miniaturas */}
      <div className="grid grid-cols-3 gap-2">
        {midias.map((midia) => (
          <button
            key={midia.id}
            onClick={() => setMidiaAberta(midia)}
            className="relative aspect-square rounded-xl overflow-hidden bg-slate-100
                       hover:opacity-90 transition-opacity focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {midia.tipo === 'FOTO' ? (
              <img
                src={midia.url}
                alt={midia.legenda || 'Foto do serviço'}
                className="w-full h-full object-cover"
                onError={(e) => { e.target.style.display = 'none' }}
              />
            ) : (
              // Para vídeos, mostra ícone de play sobre fundo escuro
              <div className="w-full h-full bg-slate-800 flex flex-col items-center justify-center gap-1">
                <Video className="h-6 w-6 text-white" />
                <span className="text-xs text-slate-300">Vídeo</span>
              </div>
            )}
            {/* Ícone indicador do tipo no canto */}
            <div className="absolute top-1.5 left-1.5">
              {midia.tipo === 'VIDEO' && (
                <div className="bg-black/60 rounded-full p-0.5">
                  <Video className="h-3 w-3 text-white" />
                </div>
              )}
            </div>
          </button>
        ))}
      </div>

      {/* Lightbox — visualização ampliada */}
      {midiaAberta && (
        <div
          className="fixed inset-0 bg-black/90 z-50 flex items-center justify-center p-4"
          onClick={() => setMidiaAberta(null)}
        >
          <button
            className="absolute top-4 right-4 text-white hover:text-slate-300"
            onClick={() => setMidiaAberta(null)}
          >
            <X className="h-7 w-7" />
          </button>

          <div onClick={e => e.stopPropagation()} className="max-w-2xl w-full">
            {midiaAberta.tipo === 'FOTO' ? (
              <img
                src={midiaAberta.url}
                alt={midiaAberta.legenda || 'Foto do serviço'}
                className="w-full rounded-xl"
              />
            ) : (
              <video
                src={midiaAberta.url}
                controls
                autoPlay
                className="w-full rounded-xl"
              />
            )}
            {midiaAberta.legenda && (
              <p className="text-white text-sm text-center mt-3">{midiaAberta.legenda}</p>
            )}
            <p className="text-slate-400 text-xs text-center mt-1">
              {new Date(midiaAberta.enviadaEm).toLocaleString('pt-BR')}
            </p>
          </div>
        </div>
      )}
    </div>
  )
}
