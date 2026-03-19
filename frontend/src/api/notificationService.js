import api from './axios'

export const getUnreadNotifications = async (userId) => {
  const res = await api.get(`/notifications/${userId}`)
  return res.data.data
}

export const getUnreadCount = async (userId) => {
  const res = await api.get(`/notifications/${userId}/count`)
  return res.data.data
}

export const markAllAsRead = async (userId) => {
  const res = await api.patch(`/notifications/${userId}/read-all`)
  return res.data.data
}