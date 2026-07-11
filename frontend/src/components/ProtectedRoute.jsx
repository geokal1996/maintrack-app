import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

// Tyligei routes pou xreiazontai APLA login (opoiosdipote rolos).
// An den eisai syndedemenos, se stelnei sto /login.
export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
