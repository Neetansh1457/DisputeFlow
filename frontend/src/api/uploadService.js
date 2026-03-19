import api from './axios'

export const singleUpload = async (file, userId, bankId, caseId, documentType = 'REPRESENTATION') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', userId)
  formData.append('bankId', bankId)
  formData.append('caseId', caseId)
  formData.append('documentType', documentType)

  const res = await api.post('/upload/single', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data.data
}

export const previewBatch = async (fileNames) => {
  const res = await api.post('/upload/batch/preview', { fileNames })
  return res.data.data
}

export const batchUpload = async (userId, files) => {
  const formData = new FormData()
  formData.append('userId', userId)
  files.forEach(file => formData.append('files', file))

  const res = await api.post('/upload/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data.data
}