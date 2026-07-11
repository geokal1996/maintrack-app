import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

// Tyligei routes pou xreiazontai SYGKEKRIMENA rolo SUPERVISOR
// (p.x. i selida diaxeirisis xriston). An eisai TECHNICIAN, se gyrnaei piso sto dashboard.
export default function RequireSupervisor() {
  const { isSupervisor } = useAuth();

  if (!isSupervisor) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
