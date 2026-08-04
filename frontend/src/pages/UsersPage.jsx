import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { Users, Plus, X, Save, UserCheck, UserX } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { getUsers, createUser, setUserActive } from "../api/usersApi";
import { SkeletonTable } from "../components/Skeleton";
import EmptyState from "../components/EmptyState";
import { confirmToast } from "../components/confirmToast";

const ROLE_LABELS = {
  MANAGER: "Διευθυντής",
  SUPERVISOR: "Επόπτης",
  TECHNICIAN: "Τεχνικός",
};

const emptyForm = { username: "", password: "", fullName: "", role: "TECHNICIAN", jobTitle: "" };

export default function UsersPage() {
  const { isManager } = useAuth();
  // O SUPERVISOR mporei na ftiaxnei MONO TECHNICIAN. O MANAGER mporei TECHNICIAN i SUPERVISOR
  // (oxi allon MANAGER - auto ginetai mono me to xeri stin vasi, gia asfaleia).
  const availableRoles = isManager ? ["TECHNICIAN", "SUPERVISOR"] : ["TECHNICIAN"];

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  function loadUsers() {
    setLoading(true);
    getUsers()
      .then(setUsers)
      .finally(() => setLoading(false));
  }

  useEffect(loadUsers, []);

  async function handleCreate(e) {
    e.preventDefault();
    setSaving(true);
    try {
      await createUser(form);
      toast.success(`Ο χρήστης ${form.username} δημιουργήθηκε`);
      setForm(emptyForm);
      setShowForm(false);
      loadUsers();
    } catch (err) {
      toast.error(
        err.response?.data?.message ||
          "Δεν ήταν δυνατή η δημιουργία — έλεγξε τα στοιχεία (π.χ. αν το username υπάρχει ήδη)"
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleToggleActive(u) {
    const action = u.active ? "απενεργοποιηθεί" : "ενεργοποιηθεί";
    const ok = await confirmToast(`Να ${action} ο χρήστης ${u.username};`, {
      confirmLabel: u.active ? "Απενεργοποίηση" : "Ενεργοποίηση",
    });
    if (!ok) return;

    try {
      await setUserActive(u.id, !u.active);
      toast.success(u.active ? "Ο χρήστης απενεργοποιήθηκε" : "Ο χρήστης ενεργοποιήθηκε");
      loadUsers();
    } catch (err) {
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η αλλαγή");
    }
  }

  return (
    <div>
      <h1 className="page-title">
        <Users size={22} />
        Χρήστες
        <span className="sub">{users.length} λογαριασμοί</span>
      </h1>

      <div className="card">
        <div className="card-header">
          <h2>Διαχείριση</h2>
          <button className="btn" onClick={() => setShowForm((s) => !s)}>
            {showForm ? <X size={16} /> : <Plus size={16} />}
            {showForm ? "Ακύρωση" : "Νέος χρήστης"}
          </button>
        </div>

        {showForm && (
          <>
            <div className="divider" />
            <form onSubmit={handleCreate}>
              <div className="form-grid">
                <div className="form-row">
                  <label>Username</label>
                  <input
                    required
                    placeholder="π.χ. g.papadopoulos"
                    value={form.username}
                    onChange={(e) => setForm({ ...form, username: e.target.value })}
                  />
                </div>
                <div className="form-row">
                  <label>Password</label>
                  <input
                    type="password"
                    required
                    minLength={4}
                    placeholder="••••••••"
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                  />
                </div>
                <div className="form-row">
                  <label>Ονοματεπώνυμο</label>
                  <input
                    required
                    placeholder="π.χ. Γιώργος Παπαδόπουλος"
                    value={form.fullName}
                    onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                  />
                </div>
                <div className="form-row">
                  <label>Ρόλος</label>
                  <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                    {availableRoles.map((r) => (
                      <option key={r} value={r}>
                        {ROLE_LABELS[r]}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="form-row">
                <label>Τίτλος θέσης (προαιρετικό)</label>
                <input
                  placeholder="π.χ. Ηλεκτρολόγος Συντήρησης"
                  value={form.jobTitle}
                  onChange={(e) => setForm({ ...form, jobTitle: e.target.value })}
                />
              </div>
              <div className="form-actions">
                <button className="btn" type="submit" disabled={saving}>
                  <Save size={16} />
                  {saving ? "Αποθήκευση..." : "Αποθήκευση"}
                </button>
              </div>
            </form>
          </>
        )}
      </div>

      <div className="card">
        {loading ? (
          <SkeletonTable rows={5} cols={5} />
        ) : users.length === 0 ? (
          <EmptyState icon={Users} message="Δεν υπάρχουν χρήστες." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Χρήστης</th>
                  <th>Ρόλος</th>
                  <th>Τίτλος θέσης</th>
                  <th>Κατάσταση</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{u.fullName}</div>
                      <div className="muted mono" style={{ fontSize: "0.78rem" }}>
                        {u.username}
                      </div>
                    </td>
                    <td>
                      <span className={`badge role-${u.role}`}>{ROLE_LABELS[u.role]}</span>
                    </td>
                    <td className="muted">{u.jobTitle || "—"}</td>
                    <td>
                      <span
                        className="badge dot"
                        style={{
                          background: u.active ? "var(--success-soft)" : "var(--surface-hover)",
                          color: u.active ? "var(--success)" : "var(--text-muted)",
                        }}
                      >
                        {u.active ? "Ενεργός" : "Ανενεργός"}
                      </span>
                    </td>
                    <td style={{ textAlign: "right" }}>
                      <button
                        className="btn ghost small"
                        onClick={() => handleToggleActive(u)}
                        title={u.active ? "Απενεργοποίηση" : "Ενεργοποίηση"}
                      >
                        {u.active ? <UserX size={15} /> : <UserCheck size={15} />}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
