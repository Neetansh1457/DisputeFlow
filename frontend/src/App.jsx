import { Routes, Route, Navigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
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
          <Route path="*" element={
            <div className="text-center py-32">
              <p className="text-white text-2xl font-semibold mb-2">404</p>
              <p className="text-gray-400">Page not found</p>
            </div>
          } />
        </Routes>
      </main>
    </div>
  )
}

export default App