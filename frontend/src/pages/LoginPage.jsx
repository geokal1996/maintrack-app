import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Wrench, LogIn, AlertCircle, Loader2, KeyRound } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import IndustrialBackground from "../components/IndustrialBackground";

// Oi demo logariasmoi tou seed. Yparxoun gia na mporei opoiosdipote (p.x. o
// kathigitis pou vathmologei) na mpei amesa xoris na psaxnei kodikous.
const DEMO_ACCOUNTS = [
  { username: "admin", password: "Admin123!", role: "Διευθυντής" },
  { username: "m.nikolaou", password: "Manager123!", role: "Επόπτης" },
  { username: "k.konstantinou", password: "Tech123!", role: "Τεχνικός" },
];

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showDemo, setShowDemo] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  // Symplironei ta pedia me pataima, anti na ta grafei o xristis sto xeri
  function fillDemo(account) {
    setUsername(account.username);
    setPassword(account.password);
    setError("");
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(username, password);
      navigate("/");
    } catch (err) {
      if (err.response && err.response.status === 401) {
        setError("Λάθος username ή password");
      } else {
        setError("Κάτι πήγε στραβά, δοκίμασε ξανά");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-wrapper">
      <IndustrialBackground />

      <div className="login-box">
        <div className="login-logo">
          <Wrench size={26} />
        </div>
        <h1>Maintrack</h1>
        <p className="login-sub">Σύστημα Καταγραφής Βλαβών &amp; Συντήρησης</p>

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="π.χ. admin"
              required
              autoFocus
            />
          </div>
          <div className="form-row">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>

          {error && (
            <p className="error-text">
              <AlertCircle size={15} />
              {error}
            </p>
          )}

          <button className="btn" type="submit" disabled={loading} style={{ width: "100%", marginTop: "0.35rem" }}>
            {loading ? (
              <>
                <Loader2 size={16} className="spin" style={{ animation: "spin 0.9s linear infinite" }} />
                Σύνδεση...
              </>
            ) : (
              <>
                <LogIn size={16} />
                Σύνδεση
              </>
            )}
          </button>
        </form>

        <div className="login-hint">
          <button type="button" className="demo-toggle" onClick={() => setShowDemo((s) => !s)}>
            <KeyRound size={13} />
            {showDemo ? "Απόκρυψη δοκιμαστικών λογαριασμών" : "Δοκιμαστικοί λογαριασμοί"}
          </button>

          {showDemo && (
            <div className="demo-list">
              {DEMO_ACCOUNTS.map((a) => (
                <button key={a.username} type="button" className="demo-account" onClick={() => fillDemo(a)}>
                  <span>
                    <code>{a.username}</code> / <code>{a.password}</code>
                  </span>
                  <span className="demo-role">{a.role}</span>
                </button>
              ))}
              <p className="demo-note">Πάτα σε έναν λογαριασμό για αυτόματη συμπλήρωση.</p>
            </div>
          )}
        </div>
      </div>

      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}
