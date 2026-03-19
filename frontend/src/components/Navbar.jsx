import { Link, useLocation } from 'react-router-dom'

function Navbar() {
  const location = useLocation()

  const navItems = [
    { path: '/upload', label: 'Upload' },
    { path: '/jobs', label: 'Jobs' },
    { path: '/audit-logs', label: 'Audit Log' },
  ]

  return (
    <nav className="bg-gray-900 border-b border-gray-700 px-6 py-4 flex items-center justify-between">
      {/* Logo */}
      <div className="flex items-center gap-2">
        <div className="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center">
          <span className="text-white font-bold text-sm">DF</span>
        </div>
        <span className="text-white font-semibold text-lg">DisputeFlow</span>
      </div>

      {/* Nav Links */}
      <div className="flex items-center gap-1">
        {navItems.map(item => (
          <Link
            key={item.path}
            to={item.path}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              location.pathname === item.path
                ? 'bg-blue-600 text-white'
                : 'text-gray-400 hover:text-white hover:bg-gray-800'
            }`}
          >
            {item.label}
          </Link>
        ))}
      </div>

      {/* Right side — user info */}
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 bg-gray-600 rounded-full flex items-center justify-center">
          <span className="text-white text-xs font-medium">TI</span>
        </div>
        <span className="text-gray-400 text-sm">Test Investigator</span>
      </div>
    </nav>
  )
}

export default Navbar