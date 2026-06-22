import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Layout from './components/layout/Layout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import ClientesPage from './pages/ClientesPage'
import VeiculosPage from './pages/VeiculosPage'
import PecasPage from './pages/PecasPage'
import OrdensServicoPage from './pages/OrdensServicoPage'
import RecomendacoesPage from './pages/RecomendacoesPage'
import PortalClientePage from './pages/PortalClientePage'

// Rota privada — redireciona para /login se não autenticado
function PrivateRoute({ children }) {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <div className="flex items-center justify-center h-screen"><div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-500" /></div>
  return isAuthenticated ? children : <Navigate to="/login" replace />
}

function AppRoutes() {
  const { isAuthenticated } = useAuth()
  return (
    <Routes>
      {/* Rota pública do Portal do Cliente — sem autenticação */}
      <Route path="/os/:token" element={<PortalClientePage />} />

      <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/" element={<PrivateRoute><Layout /></PrivateRoute>}>
        <Route index element={<DashboardPage />} />
        <Route path="clientes" element={<ClientesPage />} />
        <Route path="veiculos" element={<VeiculosPage />} />
        <Route path="pecas" element={<PecasPage />} />
        <Route path="ordens-servico" element={<OrdensServicoPage />} />
        <Route path="recomendacoes" element={<RecomendacoesPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}
