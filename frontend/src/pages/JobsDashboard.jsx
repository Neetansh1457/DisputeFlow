import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getAllJobs, retryJob, cancelJob } from '../api/jobService'
import StatusBadge from '../components/StatusBadge'

const CURRENT_USER_ID = '521de4ca-2de9-4dbd-bf28-2bc26380a9ff'

const STATUS_FILTERS = ['ALL', 'PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'FLAGGED', 'SKIPPED']

function JobsDashboard() {
  const [statusFilter, setStatusFilter] = useState('ALL')
  const queryClient = useQueryClient()

  const { data: jobs = [], isLoading } = useQuery({
    queryKey: ['jobs'],
    queryFn: () => getAllJobs({ userId: CURRENT_USER_ID }),
    refetchInterval: 5000,
  })

  const retryMutation = useMutation({
    mutationFn: retryJob,
    onSuccess: () => queryClient.invalidateQueries(['jobs']),
  })

  const cancelMutation = useMutation({
    mutationFn: cancelJob,
    onSuccess: () => queryClient.invalidateQueries(['jobs']),
  })

  const filtered = statusFilter === 'ALL'
    ? jobs
    : jobs.filter(j => j.status === statusFilter)

  // Stats
  const stats = {
    total:      jobs.length,
    success:    jobs.filter(j => j.status === 'SUCCESS').length,
    failed:     jobs.filter(j => j.status === 'FAILED').length,
    processing: jobs.filter(j => j.status === 'PROCESSING' || j.status === 'PENDING').length,
    flagged:    jobs.filter(j => j.status === 'FLAGGED').length,
  }

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-semibold text-white mb-1">Jobs Dashboard</h1>
        <p className="text-gray-400 text-sm">
          Live view of all upload jobs — auto-refreshes every 5 seconds
        </p>
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-5 gap-4 mb-8">
        {[
          { label: 'Total',      value: stats.total,      color: 'text-white' },
          { label: 'Success',    value: stats.success,    color: 'text-green-400' },
          { label: 'Processing', value: stats.processing, color: 'text-blue-400' },
          { label: 'Failed',     value: stats.failed,     color: 'text-red-400' },
          { label: 'Flagged',    value: stats.flagged,    color: 'text-purple-400' },
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

      {/* Filter Bar */}
      <div className="flex gap-2 mb-6 flex-wrap">
        {STATUS_FILTERS.map(status => (
          <button
            key={status}
            onClick={() => setStatusFilter(status)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
              statusFilter === status
                ? 'bg-blue-600 text-white'
                : 'bg-gray-800 text-gray-400 hover:text-white'
            }`}
          >
            {status}
          </button>
        ))}
      </div>

      {/* Jobs Table */}
      <div className="bg-gray-900 border border-gray-700 rounded-xl overflow-hidden">
        {isLoading ? (
          <div className="text-center py-16">
            <p className="text-gray-400 text-sm">Loading jobs...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-gray-400 text-sm">No jobs found</p>
            <p className="text-gray-600 text-xs mt-1">
              Upload a document to get started
            </p>
          </div>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-700">
                {['Bank', 'Case ID', 'File', 'Action', 'Status', 'Auto', 'Retries', 'Created', 'Actions'].map(h => (
                  <th
                    key={h}
                    className="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase"
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map(job => (
                <tr
                  key={job.id}
                  className="border-b border-gray-800 hover:bg-gray-800/50 transition-colors"
                >
                  {/* Bank */}
                  <td className="px-4 py-4">
                    <span className="text-white text-sm font-medium">{job.bankName}</span>
                  </td>

                  {/* Case ID */}
                  <td className="px-4 py-4">
                    <span className="text-gray-300 text-sm font-mono">{job.caseId}</span>
                  </td>

                  {/* File */}
                  <td className="px-4 py-4">
                    <span className="text-gray-400 text-xs truncate max-w-32 block">
                      {job.fileName}
                    </span>
                  </td>

                  {/* Action Taken */}
                  <td className="px-4 py-4">
                    {job.actionTaken ? (
                      <span className="text-xs text-gray-300">{job.actionTaken}</span>
                    ) : (
                      <span className="text-xs text-gray-600">—</span>
                    )}
                  </td>

                  {/* Status */}
                  <td className="px-4 py-4">
                    <StatusBadge status={job.status} />
                  </td>

                  {/* Auto Processed */}
                  <td className="px-4 py-4">
                    {job.autoProcessed ? (
                      <span className="text-xs text-green-400">✓ Auto</span>
                    ) : (
                      <span className="text-xs text-gray-600">Manual</span>
                    )}
                  </td>

                  {/* Retry Count */}
                  <td className="px-4 py-4">
                    <span className="text-gray-400 text-sm">{job.retryCount}</span>
                  </td>

                  {/* Created At */}
                  <td className="px-4 py-4">
                    <span className="text-gray-400 text-xs">
                      {new Date(job.createdAt).toLocaleString()}
                    </span>
                  </td>

                  {/* Actions */}
                  <td className="px-4 py-4">
                    <div className="flex gap-2">
                      {job.status === 'FAILED' && (
                        <button
                          onClick={() => retryMutation.mutate(job.id)}
                          disabled={retryMutation.isPending}
                          className="px-2 py-1 bg-blue-600/20 text-blue-400 border
                            border-blue-600/30 rounded text-xs hover:bg-blue-600/40
                            transition-colors disabled:opacity-50"
                        >
                          Retry
                        </button>
                      )}
                      {job.status === 'PENDING' && (
                        <button
                          onClick={() => cancelMutation.mutate(job.id)}
                          disabled={cancelMutation.isPending}
                          className="px-2 py-1 bg-red-600/20 text-red-400 border
                            border-red-600/30 rounded text-xs hover:bg-red-600/40
                            transition-colors disabled:opacity-50"
                        >
                          Cancel
                        </button>
                      )}
                      {job.remarks && (
                        <span
                          title={job.remarks}
                          className="px-2 py-1 bg-gray-700 text-gray-400 rounded
                            text-xs cursor-help"
                        >
                          Info
                        </span>
                      )}
                    </div>
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
        <span className="text-gray-500 text-xs">Live — refreshing every 5 seconds</span>
      </div>
    </div>
  )
}

export default JobsDashboard
