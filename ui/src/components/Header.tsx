import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="bg-brand-blue text-white shadow-md border-b-4 border-brand-gold">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <span className="font-extrabold text-xl tracking-tight">Bank Licensing and Compliance Portal</span>
          </div>

          {user && (
            <div className="flex items-center space-x-4">
              <span className="text-sm font-medium">
                {user.email} <span className="text-brand-gold opacity-90 ml-1">[{user.role}]</span>
              </span>
              <button
                onClick={handleLogout}
                className="bg-white text-brand-blue px-3 py-1.5 rounded text-sm font-bold hover:bg-gray-100 transition shadow-sm"
              >
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
