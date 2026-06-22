export default function StatCard({ title, value, icon: Icon, color = 'blue', subtitle }) {
  const colors = {
    blue: 'bg-blue-900/30 text-blue-400 border-blue-800/30',
    green: 'bg-green-900/30 text-green-400 border-green-800/30',
    yellow: 'bg-yellow-900/30 text-yellow-400 border-yellow-800/30',
    red: 'bg-red-900/30 text-red-400 border-red-800/30',
    purple: 'bg-purple-900/30 text-purple-400 border-purple-800/30',
  }

  return (
    <div className={`card border ${colors[color]}`}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-slate-400">{title}</p>
          <p className="text-3xl font-bold text-white mt-1">{value}</p>
          {subtitle && <p className="text-xs text-slate-500 mt-1">{subtitle}</p>}
        </div>
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${colors[color]}`}>
          <Icon size={24} />
        </div>
      </div>
    </div>
  )
}
