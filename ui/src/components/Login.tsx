import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { useAuth } from '../context/AuthContext';
import Header from './Header';
import Footer from './Footer';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();
  const { checkAuth } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      await apiClient.post('/auth/login', { email, password });
      await checkAuth();
      navigate('/');
    } catch (err: any) {
      if (err.response?.status === 401 || err.response?.status === 403) {
        setError('Invalid email or password');
      } else {
        setError('An unexpected error occurred');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-brand-light font-sans">
      <Header />

      <div className="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full bg-white p-8 rounded-xl shadow-lg border-t-4 border-brand-gold">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-extrabold text-brand-blue">Sign In</h2>
            <p className="mt-2 text-sm text-gray-600">Access the Licensing Portal</p>
          </div>

          <form className="space-y-6" onSubmit={handleSubmit}>
            {error && (
              <div className="text-red-600 text-sm text-center bg-red-50 p-3 rounded border border-red-200 font-medium">
                {error}
              </div>
            )}

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-brand-blue mb-1">Email address</label>
                <input
                  id="email-address"
                  name="email"
                  type="email"
                  required
                  className="appearance-none relative block w-full px-3 py-2.5 border border-gray-300 placeholder-gray-400 text-gray-900 rounded-md focus:outline-none focus:ring-2 focus:ring-brand-gold focus:border-transparent sm:text-sm transition"
                  placeholder="Enter your email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-brand-blue mb-1">Password</label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  required
                  className="appearance-none relative block w-full px-3 py-2.5 border border-gray-300 placeholder-gray-400 text-gray-900 rounded-md focus:outline-none focus:ring-2 focus:ring-brand-gold focus:border-transparent sm:text-sm transition"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
            </div>

            <div className="pt-2">
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full flex justify-center py-2.5 px-4 border border-transparent rounded-md shadow-sm text-sm font-bold text-white bg-brand-blue hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-blue disabled:opacity-50 transition"
              >
                {isSubmitting ? 'Authenticating...' : 'Login'}
              </button>
            </div>
          </form>
        </div>
      </div>

      <Footer />
    </div>
  );
}
