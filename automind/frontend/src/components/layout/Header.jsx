import { useLocation } from 'react-router-dom'
import { LogOut, User } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

const titles = {
  '/': 'Dashboard',
  '/clientes': 'Clientes',
  '/veiculos': 'Veículos',
  '/pecas': 'Peças',
  '/ordens-servico': 'Ordens de Serviço',
  '/recomendacoes': 'Recomendações',
}

export default function Header() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const title = titles[location.pathname] || 'AutoMind'

  return (
    <header className="h-16 bg-slate-900 border-b border-slate-800 flex items-center justify-between px-6">
      <h2 className="text-lg font-semibold text-white">{title}</h2>
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 text-sm text-slate-400">
          <div className="w-8 h-8 bg-blue-900 rounded-full flex items-center justify-center">
            <User size={16} className="text-blue-300" />
          </div>
          <div className="hidden sm:block">
            <p className="text-white font-medium leading-tight">{user?.nome}</p>
            <p className="text-xs text-slate-500">{user?.perfil}</p>
          </div>
        </div>
        <button
          onClick={logout}
          className="flex items-center gap-2 text-slate-400 hover:text-red-400 transition-colors text-sm px-3 py-1.5 rounded-lg hover:bg-slate-800"
        >
          <LogOut size={16} />
          <span className="hidden sm:block">Sair</span>
        </button>
      </div>
    </header>
  )
}
