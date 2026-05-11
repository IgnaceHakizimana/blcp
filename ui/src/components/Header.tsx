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
    <header className="bg-bnr-blue text-white shadow-md border-b-4 border-bnr-gold">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <span className="font-extrabold text-xl tracking-tight">National Bank of Rwanda</span>
            <span className="ml-3 px-2 py-0.5 bg-bnr-gold text-bnr-blue text-xs font-bold rounded shadow-sm">BLCP</span>
          </div>

          {user && (
            <div className="flex items-center space-x-4">
              <span className="text-sm font-medium">
                {user.email} <span className="text-bnr-gold opacity-90 ml-1">[{user.role}]</span>
              </span>
              <button
                onClick={handleLogout}
                className="bg-white text-bnr-blue px-3 py-1.5 rounded text-sm font-bold hover:bg-gray-100 transition shadow-sm"
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
