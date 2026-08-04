import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  Cog,
  AlertTriangle,
  BarChart3,
  Users,
  LogOut,
  Menu,
  X,
  Moon,
  Sun,
  Wrench,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";

// Auto einai to "periblima" pou emfanizetai se OLES tis selides meta to login:
// panw i mpara plohgisis, kai apo katw to periexomeno tis kathe selidas (<Outlet />).
export default function Layout() {
  const { user, canManageUsers, logout } = useAuth();
  const { isDark, toggleTheme } = useTheme();
  const [menuOpen, setMenuOpen] = useState(false);
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  // Ta arxika tou onomatos, gia to stroggylo avatar (p.x. "Γιώργος Κ." -> "ΓΚ")
  const initials = (user?.fullName || user?.username || "?")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0])
    .join("")
    .toUpperCase();

  const links = [
    { to: "/", label: "Dashboard", icon: LayoutDashboard, end: true },
    { to: "/machines", label: "Μηχανές", icon: Cog },
    { to: "/faults", label: "Βλάβες", icon: AlertTriangle },
    { to: "/pareto", label: "Pareto", icon: BarChart3 },
    ...(canManageUsers ? [{ to: "/users", label: "Χρήστες", icon: Users }] : []),
  ];

  return (
    <div className="app-shell">
      <header className="navbar">
        <div className="brand">
          <span className="brand-mark">
            <Wrench size={17} />
          </span>
          Maintrack
        </div>

        <nav className={menuOpen ? "open" : ""} onClick={() => setMenuOpen(false)}>
          {links.map(({ to, label, icon: Icon, end }) => (
            <NavLink key={to} to={to} end={end}>
              <Icon size={16} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="user-info">
          <button
            className="icon-btn"
            onClick={toggleTheme}
            title={isDark ? "Φωτεινό θέμα" : "Σκούρο θέμα"}
            aria-label="Εναλλαγή θέματος"
          >
            {isDark ? <Sun size={16} /> : <Moon size={16} />}
          </button>

          <div className="user-chip">
            <span className="avatar">{initials}</span>
            <span className="user-name">
              <strong>{user?.fullName}</strong>
              <span>{user?.role}</span>
            </span>
          </div>

          <button className="icon-btn" onClick={handleLogout} title="Αποσύνδεση" aria-label="Αποσύνδεση">
            <LogOut size={16} />
          </button>
        </div>

        <button
          className="icon-btn nav-toggle"
          onClick={() => setMenuOpen((o) => !o)}
          aria-label="Μενού"
        >
          {menuOpen ? <X size={18} /> : <Menu size={18} />}
        </button>
      </header>

      <main className="page-content">
        <Outlet />
      </main>
    </div>
  );
}
