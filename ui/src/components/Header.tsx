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
        <div className="flex justify-between items-center h-16 relative">

          <div className="flex-1"></div>

          <div className="absolute left-1/2 transform -translate-x-1/2 flex items-center text-center w-full justify-center md:w-auto">
            <span className="font-extrabold text-xl tracking-tight">Bank Licensing and Compliance Portal</span>
          </div>

          <div className="flex-1 flex justify-end items-center space-x-4">
            {user && (
              <>
                <span className="text-sm font-medium hidden sm:block">
                  {user.email} <span className="text-brand-gold opacity-90 ml-1">[{user.role}]</span>
                </span>
                <button
                  onClick={handleLogout}
                  className="bg-white text-brand-blue px-3 py-1.5 rounded text-sm font-bold hover:bg-gray-100 transition shadow-sm whitespace-nowrap"
                >
                  Logout
                </button>
              </>
            )}
          </div>

        </div>
      </div>
    </header>
  );
}
