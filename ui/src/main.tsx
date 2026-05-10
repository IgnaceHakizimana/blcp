import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import './index.css';

import Login from './components/Login';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Unauthorized from './components/Unauthorized';
import ApplicantDashboard from './components/ApplicantDashboard';
import RegulatorDashboard from './components/RegulatorDashboard.tsx';


function RootRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role === 'APPLICANT') return <Navigate to="/applicant" replace />;
  return <Navigate to="/regulator" replace />;
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          {}
          <Route element={<Layout />}>
            <Route path="/" element={<RootRedirect />} />

            <Route element={<ProtectedRoute allowedRoles={['APPLICANT']} />}>
              <Route path="/applicant" element={<ApplicantDashboard />} />
            </Route>

            <Route element={<ProtectedRoute allowedRoles={['REVIEWER', 'APPROVER']} />}>
              <Route path="/regulator" element={<RegulatorDashboard />} />
            </Route>
          </Route>

        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
);
