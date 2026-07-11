import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import RequireSupervisor from "./components/RequireSupervisor";
import Layout from "./components/Layout";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import MachinesPage from "./pages/MachinesPage";
import FaultsPage from "./pages/FaultsPage";
import FaultDetailPage from "./pages/FaultDetailPage";
import UsersPage from "./pages/UsersPage";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          {/* Ola ta parakato routes xreiazontai login (ProtectedRoute) */}
          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/machines" element={<MachinesPage />} />
              <Route path="/faults" element={<FaultsPage />} />
              <Route path="/faults/:id" element={<FaultDetailPage />} />

              {/* Auto to route xreiazetai EPIPLEON kai rolo SUPERVISOR */}
              <Route element={<RequireSupervisor />}>
                <Route path="/users" element={<UsersPage />} />
              </Route>
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
