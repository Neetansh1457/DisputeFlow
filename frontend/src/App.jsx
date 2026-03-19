import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import UploadPage from './pages/UploadPage'
import JobsDashboard from './pages/JobsDashboard'
import AuditLogPage from './pages/AuditLogPage'

function App() {
  return (
    <div className="min-h-screen bg-gray-950">
      <Navbar />
      <main className="max-w-7xl mx-auto px-6 py-8">
        <Routes>
          <Route path="/" element={<Navigate to="/upload" replace />} />
          <Route path="/upload" element={<UploadPage />} />
          <Route path="/jobs" element={<JobsDashboard />} />
          <Route path="/audit-logs" element={<AuditLogPage />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
