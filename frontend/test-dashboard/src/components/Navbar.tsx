import React from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import { BarChart3, PlayCircle, RefreshCcw, Clock, FileText, LogIn } from 'lucide-react';

const navItems = [
  { icon: <BarChart3 className="mr-2" />, label: 'Dashboard', to: '/' },
  { icon: <PlayCircle className="mr-2" />, label: 'Run Tests', to: '/run-tests' },
  { icon: <RefreshCcw className="mr-2" />, label: 'Rerun Failed', to: '/rerun-failed' },
  { icon: <Clock className="mr-2" />, label: 'Test History', to: '/test-history' },
  { icon: <FileText className="mr-2" />, label: 'Reports', to: '/reports' },
  { icon: <LogIn className="mr-2" />, label: 'Login', to: '/login' },
];

const Navbar = () => {
  return (
    <div className="flex flex-col h-screen bg-gray-900 text-white">
      {/* Full-width Header at the Top */}
      <header className="bg-gray-800 px-6 py-4 shadow-md flex items-center justify-between">
        <div className="flex-1 text-center">
          <h1 className="text-xl font-bold text-white">Test Automation Dashboard</h1>
        </div>
        <div className="text-sm font-bold text-gray-300">Welcome, User</div>
      </header>

      {/* Body: Sidebar + Main Content */}
      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar under Header */}
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
            style={({ isActive }) =>
              isActive
                ? { backgroundColor: '#2563eb', color: '#fff' } 
                : {}
            }
          >
            {icon}<span>{label}</span>
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
