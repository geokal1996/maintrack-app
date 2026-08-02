import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

// Tyligei routes pou xreiazontai SUPERVISOR I MANAGER (dld OXI aplo TECHNICIAN)
// (p.x. i selida diaxeirisis xriston). An eisai TECHNICIAN, se gyrnaei piso sto dashboard.
// (To onoma tou arxeiou emeine "RequireSupervisor" gia na min allaxoume imports pantou,
// alla praktika elenxei to "canManageUsers" pou kalyptei kai tous 2 rolous.)
export default function RequireSupervisor() {
  const { canManageUsers } = useAuth();

  if (!canManageUsers) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
