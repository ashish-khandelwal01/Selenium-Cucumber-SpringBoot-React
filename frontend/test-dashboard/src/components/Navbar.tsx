import React, { useContext, useState, useRef, useEffect } from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import { BarChart3, PlayCircle, RefreshCcw, Clock, FileText, LogIn, LogOut, ChevronDown } from 'lucide-react';
import AuthContext from '../context/AuthContext';

const navItems = [
  { icon: <BarChart3 className="mr-2" />, label: 'Dashboard', to: '/' },
  { icon: <PlayCircle className="mr-2" />, label: 'Run Tests', to: '/run-tests' },
  { icon: <RefreshCcw className="mr-2" />, label: 'Rerun Failed', to: '/rerun-failed' },
  { icon: <Clock className="mr-2" />, label: 'Test History', to: '/test-history' },
  { icon: <FileText className="mr-2" />, label: 'Reports', to: '/reports' },
];

const Navbar = () => {
  const { authTokens, logout } = useContext(AuthContext)!;
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const username = authTokens?.username;

  // Close dropdown if clicked outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="flex flex-col h-screen bg-gray-900 text-white">
      {/* Header */}
      <header className="bg-gray-800 px-6 py-4 shadow-md flex items-center justify-between relative">
        <div className="flex-1 text-center">
          <h1 className="text-xl font-bold text-white">Test Automation Dashboard</h1>
        </div>

        {/* User dropdown */}
        {username ? (
          <div className="relative" ref={dropdownRef}>
            <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center text-sm font-bold text-gray-200 px-3 py-1 rounded hover:bg-gray-700 transition-colors"
            >
              Welcome, {username}
              <ChevronDown
                className={`ml-1 w-4 h-4 transform transition-transform duration-200 ${
                  dropdownOpen ? 'rotate-180' : 'rotate-0'
                }`}
              />
            </button>

            <div
              className={`absolute right-0 mt-2 w-36 bg-gray-800 rounded-lg shadow-xl z-20 transform transition-all duration-300 origin-top-right ${
                dropdownOpen
                  ? 'opacity-100 scale-100 translate-y-0'
                  : 'opacity-0 scale-95 -translate-y-2 pointer-events-none'
              }`}
            >
              <button
                onClick={logout}
                className="w-full flex items-center px-4 py-2 text-sm text-gray-200 hover:bg-red-600 hover:text-white rounded-lg"
              >
                <LogOut className="mr-2 w-4 h-4" /> Logout
              </button>
            </div>
          </div>
        ) : (
          <NavLink
            to="/login"
            className="text-sm font-bold text-gray-200 px-3 py-1 rounded hover:bg-gray-700 transition-colors"
          >
            Login
          </NavLink>
        )}
      </header>

      {/* Sidebar + Main */}
      <div className="flex flex-1 overflow-hidden">
        <aside className="w-64 p-4 bg-gray-800 space-y-4">
          {navItems.map(({ icon, label, to }) => (
            <NavLink
              key={label}
              to={to}
              className={({ isActive }) =>
                `flex items-center px-4 py-2 rounded-xl hover:bg-gray-700 cursor-pointer transition-colors duration-150 ${
                  isActive
                    ? 'bg-blue-500 text-white font-semibold shadow-md'
                    : 'text-gray-200'
                }`
              }
            >
              {icon}
              <span>{label}</span>
            </NavLink>
          ))}
        </aside>

        <main className="flex-1 p-6 space-y-6 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Navbar;
