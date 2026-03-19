import api from './axios'

export const getAllAuditLogs = async () => {
  const res = await api.get('/audit-logs')
  return res.data.data
}

export const getAuditLogsByJob = async (jobId) => {
  const res = await api.get(`/audit-logs/${jobId}`)
  return res.data.data
}