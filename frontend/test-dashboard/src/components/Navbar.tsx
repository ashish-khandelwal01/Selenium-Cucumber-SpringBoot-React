import React, { useContext, useState, useRef, useEffect } from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import {
  BarChart3, PlayCircle, RefreshCcw, Clock, FileText,
  LogIn, LogOut, ChevronDown, Monitor, FileCode
} from 'lucide-react';
import AuthContext from '../context/AuthContext';
import { useBrowser } from '../context/BrowserContext';

const navItems = [
  { icon: <BarChart3 className="mr-2" />, label: 'Dashboard', to: '/' },
  { icon: <PlayCircle className="mr-2" />, label: 'Run Tests', to: '/run-tests' },
  { icon: <RefreshCcw className="mr-2" />, label: 'Rerun Failed', to: '/rerun-failed' },
  { icon: <Clock className="mr-2" />, label: 'Test History', to: '/test-history' },
  { icon: <FileText className="mr-2" />, label: 'Reports', to: '/reports' },
  { icon: <FileCode className="mr-2" />, label: 'Features', to: '/features' },
];

const Navbar = () => {
  const { selectedBrowser, updateBrowser } = useBrowser();

  const browsers = [
    { value: 'chrome', label: 'Chrome', icon: '🌐' },
    { value: 'firefox', label: 'Firefox', icon: '🦊' },
    { value: 'edge', label: 'Edge', icon: '🔷' },
  ];

  const { authTokens, logout } = useContext(AuthContext);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [browserDropdownOpen, setBrowserDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const browserDropdownRef = useRef<HTMLDivElement>(null);

  const username = authTokens?.username;
  const selectedBrowserInfo = browsers.find(b => b.value === selectedBrowser);

  // Close dropdowns if clicked outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
      if (browserDropdownRef.current && !browserDropdownRef.current.contains(event.target as Node)) {
        setBrowserDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="flex flex-col h-screen bg-gray-900 text-white">
      {/* Header */}
      <header className="bg-gray-800 px-6 py-4 shadow-md flex items-center justify-between relative">
        <div className="flex items-center w-full justify-between">
          {/* Left: Browser Selector */}
          <div className="flex items-center">
            <div className="relative" ref={browserDropdownRef}>
              <button
                onClick={() => setBrowserDropdownOpen(!browserDropdownOpen)}
                className="flex items-left text-sm font-medium text-gray-200 px-3 py-2 rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors"
              >
                <Monitor className="mr-2 w-4 h-4" />
                <span className="mr-1">{selectedBrowserInfo?.icon}</span>
                {selectedBrowserInfo?.label}
                <ChevronDown
                  className={`ml-2 w-4 h-4 transform transition-transform duration-200 ${
                    browserDropdownOpen ? 'rotate-180' : 'rotate-0'
                  }`}
                />
              </button>

              <div
                className={`absolute left-0 mt-2 w-48 bg-gray-800 rounded-lg shadow-xl z-30 border border-gray-700 transform transition-all duration-300 origin-top-left ${
                  browserDropdownOpen
                    ? 'opacity-100 scale-100 translate-y-0'
                    : 'opacity-0 scale-95 -translate-y-2 pointer-events-none'
                }`}
              >
                <div className="py-1">
                  <div className="px-3 py-2 text-xs text-gray-400 uppercase tracking-wide border-b border-gray-700">
                    Select Browser
                  </div>
                  {browsers.map((browser) => (
                    <button
                      key={browser.value}
                      onClick={() => {
                        updateBrowser(browser.value);
                        setBrowserDropdownOpen(false);
                      }}
                      className={`w-full flex items-center px-3 py-2 text-sm hover:bg-gray-700 transition-colors ${
                        selectedBrowser === browser.value
                          ? 'bg-blue-500 text-white'
                          : 'text-gray-200'
                      }`}
                    >
                      <span className="mr-2">{browser.icon}</span>
                      {browser.label}
                      {selectedBrowser === browser.value && (
                        <span className="ml-auto text-xs">✓</span>
                      )}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Center: Header Text */}
          <div className="flex-1 flex justify-center">
            <span className="text-2xl font-bold text-white">Test Automation Dashboard</span>
          </div>

          {/* Right: User dropdown */}
          <div className="flex items-center">
            {username ? (
              <div className="relative" ref={dropdownRef}>
                <button
                  onClick={() => setDropdownOpen(!dropdownOpen)}
                  className="flex items-center text-sm font-bold text-gray-200 px-2 py-1 rounded-lg hover:bg-gray-700 transition-colors"
                >
                  <div className="w-7 h-7 bg-blue-600 rounded-full flex items-center justify-center text-white text-xs font-bold mr-2">
                    {username.charAt(0).toUpperCase()}
                  </div>
                  <span className="text-sm">Welcome, {username}</span>
                  <ChevronDown
                    className={`ml-1 w-4 h-4 transform transition-transform duration-200 ${
                      dropdownOpen ? 'rotate-180' : 'rotate-0'
                    }`}
                  />
                </button>

                <div
                  className={`absolute right-0 mt-2 w-36 bg-gray-800 rounded-lg shadow-xl z-20 border border-gray-700 transform transition-all duration-300 origin-top-right ${
                    dropdownOpen
                      ? 'opacity-100 scale-100 translate-y-0'
                      : 'opacity-0 scale-95 -translate-y-2 pointer-events-none'
                  }`}
                >
                  <button
                    onClick={logout}
                    className="w-full flex items-center px-4 py-2 text-sm text-gray-200 hover:bg-red-600 hover:text-white rounded-lg transition-colors"
                  >
                    <LogOut className="mr-2 w-4 h-4" /> Logout
                  </button>
                </div>
              </div>
            ) : (
              <NavLink
                to="/login"
                className="flex items-center text-sm font-bold text-gray-200 px-3 py-2 rounded-lg hover:bg-gray-700 transition-colors"
              >
                <LogIn className="mr-2 w-4 h-4" />
                Login
              </NavLink>
            )}
          </div>
        </div>
      </header>

      {/* Sidebar + Main */}
      <div className="flex flex-1 overflow-hidden">
        <aside className="w-64 p-4 bg-gray-800 space-y-4">
          {/* Browser Status in Sidebar */}
          <div className="bg-gray-800 border border-gray-700 rounded-lg p-3 mb-4">
            <div className="flex items-center text-sm">
              <Monitor className="mr-2 w-4 h-4 text-blue-400" />
              <div>
                <div className="text-gray-400 text-xs">Active Browser</div>
                <div className="text-white font-medium">
                  <span className="mr-1">{selectedBrowserInfo?.icon}</span>
                  {selectedBrowserInfo?.label}
                </div>
              </div>
            </div>
          </div>

          {/* Navigation Items */}
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

          {/* Browser Quick Actions */}
          <div className="pt-4 border-t border-gray-700">
            <div className="text-xs text-gray-400 uppercase tracking-wide mb-2 px-4">
              Quick Actions
            </div>
            <div className="space-y-1">
              {browsers.map((browser) => (
                <button
                  key={browser.value}
                  onClick={() => updateBrowser(browser.value)}
                  className={`w-full flex items-center px-4 py-2 text-sm rounded-lg transition-colors ${
                    selectedBrowser === browser.value
                      ? 'bg-blue-500 text-white'
                      : 'text-gray-400 hover:text-gray-200 hover:bg-gray-700'
                  }`}
                >
                  <span className="mr-2">{browser.icon}</span>
                  <span className="flex-1 text-left">{browser.label}</span>
                  {selectedBrowser === browser.value && (
                    <span className="text-xs">●</span>
                  )}
                </button>
              ))}
            </div>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-6 space-y-6 overflow-auto text-white">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Navbar;
