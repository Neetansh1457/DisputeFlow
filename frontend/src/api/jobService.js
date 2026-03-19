import api from './axios'

export const getAllJobs = async (filters = {}) => {
  const params = new URLSearchParams()
  if (filters.userId) params.append('userId', filters.userId)
  if (filters.status) params.append('status', filters.status)

  const res = await api.get(`/jobs?${params.toString()}`)
  return res.data.data
}

export const getJobById = async (id) => {
  const res = await api.get(`/jobs/${id}`)
  return res.data.data
}

export const retryJob = async (id) => {
  const res = await api.patch(`/jobs/${id}/retry`)
  return res.data.data
}

export const cancelJob = async (id) => {
  const res = await api.delete(`/jobs/${id}`)
  return res.data.data
}