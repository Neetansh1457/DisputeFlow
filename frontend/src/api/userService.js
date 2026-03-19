import api from './axios'

export const registerUser = async (user) => {
  const res = await api.post('/users/register', user)
  return res.data.data
}

export const getAllUsers = async () => {
  const res = await api.get('/users')
  return res.data.data
}

export const getUserById = async (id) => {
  const res = await api.get(`/users/${id}`)
  return res.data.data
}