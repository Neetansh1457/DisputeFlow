function StatusBadge({ status }) {
  const styles = {
    PENDING:        'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30',
    PROCESSING:     'bg-blue-500/20 text-blue-400 border border-blue-500/30',
    SUCCESS:        'bg-green-500/20 text-green-400 border border-green-500/30',
    FAILED:         'bg-red-500/20 text-red-400 border border-red-500/30',
    PENDING_RETRY:  'bg-orange-500/20 text-orange-400 border border-orange-500/30',
    SKIPPED:        'bg-gray-500/20 text-gray-400 border border-gray-500/30',
    FLAGGED:        'bg-purple-500/20 text-purple-400 border border-purple-500/30',
  }

  return (
    <span className={`px-2 py-1 rounded-md text-xs font-medium ${styles[status] || styles.PENDING}`}>
      {status}
    </span>
  )
}

export default StatusBadge