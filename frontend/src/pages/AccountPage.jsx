import { useState } from "react";
import toast from "react-hot-toast";
import { UserCircle, KeyRound, Save, ShieldCheck } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { changeOwnPassword } from "../api/usersApi";

const ROLE_LABELS = {
  MANAGER: "Διευθυντής",
  SUPERVISOR: "Επόπτης",
  TECHNICIAN: "Τεχνικός",
};

const emptyForm = { currentPassword: "", newPassword: "", confirmPassword: "" };

export default function AccountPage() {
  const { user } = useAuth();
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();

    // Elenxoume tin epivevaiosi EDO kai oxi sto backend: einai kathara thema
    // pliktrologisis tou xristi, den exei noima na taxidepsei mexri ton server.
    if (form.newPassword !== form.confirmPassword) {
      toast.error("Οι δύο νέοι κωδικοί δεν ταιριάζουν");
      return;
    }

    setSaving(true);
    try {
      await changeOwnPassword(form.currentPassword, form.newPassword);
      toast.success("Ο κωδικός άλλαξε");
      setForm(emptyForm);
    } catch (err) {
      const data = err.response?.data;
      if (data && typeof data === "object" && !data.message) {
        toast.error(Object.values(data).join(" · "));
      } else {
        toast.error(data?.message || "Δεν ήταν δυνατή η αλλαγή κωδικού");
      }
    } finally {
      setSaving(false);
    }
  }

  const initials = (user?.fullName || user?.username || "?")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0])
    .join("")
    .toUpperCase();

  return (
    <div>
      <h1 className="page-title">
        <UserCircle size={22} />
        Ο λογαριασμός μου
      </h1>

      <div className="card">
        <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
          <span
            style={{
              display: "grid",
              placeItems: "center",
              width: 56,
              height: 56,
              borderRadius: "50%",
              background: "linear-gradient(135deg, #6366f1, #0ea5e9)",
              color: "#fff",
              fontWeight: 700,
              fontSize: "1.1rem",
            }}
          >
            {initials}
          </span>
          <div>
            <div style={{ fontSize: "1.1rem", fontWeight: 650 }}>{user?.fullName}</div>
            <div className="muted mono">{user?.username}</div>
            <span className={`badge role-${user?.role}`} style={{ marginTop: "0.4rem" }}>
              <ShieldCheck size={11} /> {ROLE_LABELS[user?.role] || user?.role}
            </span>
          </div>
        </div>
      </div>

      <div className="card">
        <h2>
          <KeyRound size={17} /> Αλλαγή κωδικού
        </h2>
        <p className="muted" style={{ marginTop: 0 }}>
          Ζητάμε και τον τρέχοντα κωδικό για επιβεβαίωση — έτσι κανείς δεν μπορεί να αλλάξει
          τον κωδικό σου αν βρει την οθόνη σου ανοιχτή.
        </p>

        <form onSubmit={handleSubmit} style={{ maxWidth: 420 }}>
          <div className="form-row">
            <label>Τρέχων κωδικός</label>
            <input
              type="password"
              required
              autoComplete="current-password"
              value={form.currentPassword}
              onChange={(e) => setForm({ ...form, currentPassword: e.target.value })}
            />
          </div>
          <div className="form-row">
            <label>Νέος κωδικός</label>
            <input
              type="password"
              required
              autoComplete="new-password"
              value={form.newPassword}
              onChange={(e) => setForm({ ...form, newPassword: e.target.value })}
            />
            <span className="muted" style={{ fontSize: "0.76rem" }}>
              τουλάχιστον 8 χαρακτήρες, με ένα γράμμα και έναν αριθμό
            </span>
          </div>
          <div className="form-row">
            <label>Επανάληψη νέου κωδικού</label>
            <input
              type="password"
              required
              autoComplete="new-password"
              value={form.confirmPassword}
              onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
            />
          </div>
          <div className="form-actions">
            <button className="btn" type="submit" disabled={saving}>
              <Save size={16} />
              {saving ? "Αποθήκευση..." : "Αλλαγή κωδικού"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
