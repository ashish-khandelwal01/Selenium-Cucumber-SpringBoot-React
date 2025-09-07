import { BrowserRouter as Router, Routes, Route, Outlet } from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import RunTests from "./pages/RunTests";
import RerunFailed from "./pages/RerunFailed";
import TestHistory from "./pages/TestHistory";
import Reports from "./pages/Reports";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Features from "./pages/Features";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import { BrowserProvider } from './context/BrowserContext';

// Layout component for protected pages
const ProtectedLayout = () => {
  return (
    <ProtectedRoute>
      <BrowserProvider>
        <div className="app-layout">
          <Navbar />
          <main className="main-content">
            <Outlet />
          </main>
        </div>
      </BrowserProvider>
    </ProtectedRoute>
  );
};

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected routes with shared layout */}
          <Route path="/" element={<ProtectedLayout />}>
            <Route index element={<Home />} />
            <Route path="run-tests" element={<RunTests />} />
            <Route path="rerun-failed" element={<RerunFailed />} />
            <Route path="test-history" element={<TestHistory />} />
            <Route path="reports" element={<Reports />} />
            <Route path="features" element={<Features />} />
          </Route>
        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;