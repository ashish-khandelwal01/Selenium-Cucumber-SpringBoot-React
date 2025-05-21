import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import RunTests from './pages/RunTests';
import RerunFailed from './pages/RerunFailed';
import TestHistory from './pages/TestHistory';
import Reports from './pages/Reports';
import Login from './pages/Login';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navbar />}>
          <Route index element={<Home />} />
          <Route path="run-tests" element={<RunTests />} />
          <Route path="rerun-failed" element={<RerunFailed />} />
          <Route path="test-history" element={<TestHistory />} />
          <Route path="reports" element={<Reports />} />
          <Route path="login" element={<Login />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;