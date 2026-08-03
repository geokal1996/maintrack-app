import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

// Auto einai to "periblima" pou emfanizetai se OLES tis selides meta to login:
// panw i mpara plohgisis, kai apo katw to periexomeno tis kathe selidas (<Outlet />).
export default function Layout() {
  const { user, canManageUsers, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <div className="app-shell">
      <header className="navbar">
        <div className="brand">Maintrack</div>
        <nav>
          <NavLink to="/" end>Dashboard</NavLink>
          <NavLink to="/machines">Μηχανές</NavLink>
          <NavLink to="/faults">Βλάβες</NavLink>
          <NavLink to="/pareto">Pareto</NavLink>
          {canManageUsers && <NavLink to="/users">Χρήστες</NavLink>}
        </nav>
        <div className="user-info">
          <span>{user?.fullName} ({user?.role})</span>
          <button className="logout" onClick={handleLogout}>Αποσύνδεση</button>
        </div>
      </header>
      <main className="page-content">
        <Outlet />
      </main>
    </div>
  );
}
