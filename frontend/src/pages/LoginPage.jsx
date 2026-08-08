import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Wrench,
  LogIn,
  AlertCircle,
  Loader2,
  KeyRound,
  UserPlus,
  ArrowLeft,
  CheckCircle2,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { register } from "../api/authApi";
import IndustrialBackground from "../components/IndustrialBackground";

// Oi demo logariasmoi tou seed. Yparxoun gia na mporei opoiosdipote (p.x. o
// kathigitis pou vathmologei) na mpei amesa xoris na psaxnei kodikous.
const DEMO_ACCOUNTS = [
  { username: "admin", password: "Admin123!", role: "Διευθυντής" },
  { username: "m.nikolaou", password: "Manager123!", role: "Επόπτης" },
  { username: "k.konstantinou", password: "Tech123!", role: "Τεχνικός" },
];

const emptyRegister = { username: "", password: "", fullName: "", jobTitle: "" };

export default function LoginPage() {
  const [mode, setMode] = useState("login"); // "login" | "register" | "registered"
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [regForm, setRegForm] = useState(emptyRegister);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showDemo, setShowDemo] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

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
      // To backend stelnei 403 me sygkekrimeno minima an o logariasmos einai anenergos
      const message = err.response?.data?.message;
      if (message) {
        setError(message);
      } else if (err.response?.status === 401) {
        setError("Λάθος username ή κωδικός");
      } else {
        setError("Κάτι πήγε στραβά, δοκίμασε ξανά");
      }
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register(regForm);
      setMode("registered");
      setRegForm(emptyRegister);
    } catch (err) {
      const data = err.response?.data;
      // Ta lathi validation erxontai san { pedio: "minima" }
      if (data && typeof data === "object" && !data.message) {
        setError(Object.values(data).join(" · "));
      } else {
        setError(data?.message || "Δεν ήταν δυνατή η εγγραφή");
      }
    } finally {
      setLoading(false);
    }
  }

  function switchMode(next) {
    setMode(next);
    setError("");
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

        {/* ---------- Epityxis eggrafi ---------- */}
        {mode === "registered" && (
          <>
            <div
              style={{
                display: "flex",
                gap: "0.7rem",
                alignItems: "flex-start",
                background: "rgba(22, 163, 74, 0.16)",
                border: "1px solid rgba(74, 222, 128, 0.35)",
                borderRadius: "var(--radius-sm)",
                padding: "0.9rem",
                color: "#bbf7d0",
                fontSize: "0.88rem",
                lineHeight: 1.5,
              }}
            >
              <CheckCircle2 size={18} style={{ flexShrink: 0, marginTop: 2 }} />
              <span>
                Ο λογαριασμός σου δημιουργήθηκε. <strong>Δεν μπορείς να συνδεθείς ακόμα</strong> —
                ένας επόπτης πρέπει πρώτα να τον εγκρίνει.
              </span>
            </div>
            <button
              className="btn secondary"
              onClick={() => switchMode("login")}
              style={{ width: "100%", marginTop: "1.1rem" }}
            >
              <ArrowLeft size={16} /> Επιστροφή στη σύνδεση
            </button>
          </>
        )}

        {/* ---------- Syndesi ---------- */}
        {mode === "login" && (
          <>
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
                <p className="error-text" style={{ alignItems: "flex-start", lineHeight: 1.45 }}>
                  <AlertCircle size={15} style={{ flexShrink: 0, marginTop: 2 }} />
                  <span>{error}</span>
                </p>
              )}

              <button
                className="btn"
                type="submit"
                disabled={loading}
                style={{ width: "100%", marginTop: "0.35rem" }}
              >
                {loading ? (
                  <>
                    <Loader2 size={16} style={{ animation: "spin 0.9s linear infinite" }} />
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

            <button
              className="btn secondary"
              onClick={() => switchMode("register")}
              style={{ width: "100%", marginTop: "0.6rem" }}
            >
              <UserPlus size={16} /> Δημιουργία λογαριασμού
            </button>

            <div className="login-hint">
              <button type="button" className="demo-toggle" onClick={() => setShowDemo((s) => !s)}>
                <KeyRound size={13} />
                {showDemo ? "Απόκρυψη δοκιμαστικών λογαριασμών" : "Δοκιμαστικοί λογαριασμοί"}
              </button>

              {showDemo && (
                <div className="demo-list">
                  {DEMO_ACCOUNTS.map((a) => (
                    <button
                      key={a.username}
                      type="button"
                      className="demo-account"
                      onClick={() => fillDemo(a)}
                    >
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
          </>
        )}

        {/* ---------- Eggrafi ---------- */}
        {mode === "register" && (
          <form onSubmit={handleRegister}>
            <div className="form-row">
              <label>Ονοματεπώνυμο</label>
              <input
                required
                autoFocus
                placeholder="π.χ. Γιώργος Καλοκαιρινός"
                value={regForm.fullName}
                onChange={(e) => setRegForm({ ...regForm, fullName: e.target.value })}
              />
            </div>
            <div className="form-row">
              <label>Username</label>
              <input
                required
                placeholder="π.χ. g.kalokairinos"
                value={regForm.username}
                onChange={(e) =>
                  setRegForm({ ...regForm, username: e.target.value.toLowerCase() })
                }
              />
              <span style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                πεζά λατινικά, αριθμοί, τελεία ή παύλα
              </span>
            </div>
            <div className="form-row">
              <label>Κωδικός</label>
              <input
                type="password"
                required
                placeholder="••••••••"
                value={regForm.password}
                onChange={(e) => setRegForm({ ...regForm, password: e.target.value })}
              />
              <span style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                τουλάχιστον 8 χαρακτήρες, με ένα γράμμα και έναν αριθμό
              </span>
            </div>
            <div className="form-row">
              <label>Ειδικότητα (προαιρετικό)</label>
              <input
                placeholder="π.χ. Ηλεκτρολόγος Συντήρησης"
                value={regForm.jobTitle}
                onChange={(e) => setRegForm({ ...regForm, jobTitle: e.target.value })}
              />
            </div>

            {error && (
              <p className="error-text" style={{ alignItems: "flex-start", lineHeight: 1.45 }}>
                <AlertCircle size={15} style={{ flexShrink: 0, marginTop: 2 }} />
                <span>{error}</span>
              </p>
            )}

            <button className="btn" type="submit" disabled={loading} style={{ width: "100%" }}>
              {loading ? (
                <>
                  <Loader2 size={16} style={{ animation: "spin 0.9s linear infinite" }} />
                  Δημιουργία...
                </>
              ) : (
                <>
                  <UserPlus size={16} /> Δημιουργία λογαριασμού
                </>
              )}
            </button>

            <button
              type="button"
              className="btn secondary"
              onClick={() => switchMode("login")}
              style={{ width: "100%", marginTop: "0.6rem" }}
            >
              <ArrowLeft size={16} /> Πίσω στη σύνδεση
            </button>

            <div className="login-hint">
              Ο λογαριασμός δημιουργείται ως <strong>Τεχνικός</strong> και χρειάζεται έγκριση από
              επόπτη πριν μπορέσεις να συνδεθείς.
            </div>
          </form>
        )}
      </div>

      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}
