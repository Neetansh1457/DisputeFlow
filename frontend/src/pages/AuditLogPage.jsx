import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getAllAuditLogs } from '../api/auditService'

function AuditLogPage() {
  const [search, setSearch] = useState('')
  const [actionFilter, setActionFilter] = useState('ALL')

  const { data: logs = [], isLoading } = useQuery({
    queryKey: ['audit-logs'],
    queryFn: getAllAuditLogs,
    refetchInterval: 10000,
  })

  const actions = ['ALL', ...new Set(logs.map(l => l.action))]

  const filtered = logs.filter(log => {
    const matchesAction = actionFilter === 'ALL' || log.action === actionFilter
    const matchesSearch = !search ||
      log.userName?.toLowerCase().includes(search.toLowerCase()) ||
      log.action?.toLowerCase().includes(search.toLowerCase()) ||
      log.details?.toLowerCase().includes(search.toLowerCase())
    return matchesAction && matchesSearch
  })

  const getActionStyle = (action) => {
    if (action?.includes('FAILED'))     return 'bg-red-500/20 text-red-400 border-red-500/30'
    if (action?.includes('COMPLETED'))  return 'bg-green-500/20 text-green-400 border-green-500/30'
    if (action?.includes('CREATED'))    return 'bg-blue-500/20 text-blue-400 border-blue-500/30'
    if (action?.includes('RETRY'))      return 'bg-orange-500/20 text-orange-400 border-orange-500/30'
    if (action?.includes('CANCELLED'))  return 'bg-gray-500/20 text-gray-400 border-gray-500/30'
    if (action?.includes('PROCESSING')) return 'bg-purple-500/20 text-purple-400 border-purple-500/30'
    return 'bg-gray-500/20 text-gray-400 border-gray-500/30'
  }

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-semibold text-white mb-1">Audit Log</h1>
        <p className="text-gray-400 text-sm">
          Complete history of every action taken by the system and investigators
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-4 gap-4 mb-8">
        {[
          { label: 'Total Events',   value: logs.length,                                          color: 'text-white' },
          { label: 'Jobs Created',   value: logs.filter(l => l.action === 'JOB_CREATED').length,  color: 'text-blue-400' },
          { label: 'Completed',      value: logs.filter(l => l.action === 'JOB_COMPLETED').length, color: 'text-green-400' },
          { label: 'Failed Events',  value: logs.filter(l => l.action === 'JOB_FAILED').length,   color: 'text-red-400' },
        ].map(stat => (
          <div
            key={stat.label}
            className="bg-gray-900 border border-gray-700 rounded-xl p-4 text-center"
          >
            <p className={`text-2xl font-bold ${stat.color}`}>{stat.value}</p>
            <p className="text-gray-400 text-xs mt-1">{stat.label}</p>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div className="flex gap-4 mb-6">
        {/* Search */}
        <input
          type="text"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search by user, action, or details..."
          className="flex-1 bg-gray-900 border border-gray-700 rounded-lg
            px-4 py-2 text-white text-sm placeholder-gray-500
            focus:outline-none focus:border-blue-500"
        />

        {/* Action Filter */}
        <select
          value={actionFilter}
          onChange={e => setActionFilter(e.target.value)}
          className="bg-gray-900 border border-gray-700 rounded-lg
            px-3 py-2 text-white text-sm focus:outline-none
            focus:border-blue-500"
        >
          {actions.map(action => (
            <option key={action} value={action}>{action}</option>
          ))}
        </select>
      </div>

      {/* Log Table */}
      <div className="bg-gray-900 border border-gray-700 rounded-xl overflow-hidden">
        {isLoading ? (
          <div className="text-center py-16">
            <p className="text-gray-400 text-sm">Loading audit logs...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-gray-400 text-sm">No audit logs found</p>
          </div>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-700">
                {['Time', 'User', 'Action', 'Details'].map(h => (
                  <th
                    key={h}
                    className="text-left px-6 py-3 text-xs font-medium text-gray-400 uppercase"
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map((log, index) => (
                <tr
                  key={log.id || index}
                  className="border-b border-gray-800 hover:bg-gray-800/50 transition-colors"
                >
                  {/* Time */}
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-gray-400 text-xs">
                      {new Date(log.createdAt).toLocaleString()}
                    </span>
                  </td>

                  {/* User */}
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 bg-gray-700 rounded-full flex items-center justify-center">
                        <span className="text-white text-xs">
                          {log.userName?.charAt(0) || '?'}
                        </span>
                      </div>
                      <span className="text-gray-300 text-sm">{log.userName}</span>
                    </div>
                  </td>

                  {/* Action */}
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-md text-xs font-medium border ${getActionStyle(log.action)}`}>
                      {log.action}
                    </span>
                  </td>

                  {/* Details */}
                  <td className="px-6 py-4">
                    <span className="text-gray-400 text-sm">{log.details}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Live indicator */}
      <div className="mt-4 flex items-center gap-2">
        <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"/>
        <span className="text-gray-500 text-xs">
          Live — refreshing every 10 seconds · {filtered.length} events shown
        </span>
      </div>
    </div>
  )
}

export default AuditLogPage
