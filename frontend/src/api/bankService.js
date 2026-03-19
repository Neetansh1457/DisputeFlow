import api from './axios'

export const getAllBanks = async () => {
  const res = await api.get('/banks')
  return res.data.data
}

export const createBank = async (bank) => {
  const res = await api.post('/banks', bank)
  return res.data.data
}