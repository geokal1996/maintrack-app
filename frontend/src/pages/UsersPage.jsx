import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { Users, Plus, X, Save, UserCheck, UserX, Clock } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { getUsers, createUser, setUserActive, updateUserRole } from "../api/usersApi";
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
  const { user: currentUser, isManager } = useAuth();
  // O SUPERVISOR mporei na diaxeiristei MONO TECHNICIAN. O MANAGER kai TECHNICIAN kai SUPERVISOR
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

  function showApiError(err, fallback) {
    const data = err.response?.data;
    if (data && typeof data === "object" && !data.message) {
      toast.error(Object.values(data).join(" · "));
    } else {
      toast.error(data?.message || fallback);
    }
  }

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
      showApiError(err, "Δεν ήταν δυνατή η δημιουργία");
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
      showApiError(err, "Δεν ήταν δυνατή η αλλαγή");
    }
  }

  async function handleRoleChange(u, newRole) {
    if (newRole === u.role) return;

    const ok = await confirmToast(
      `Να αλλάξει ο ρόλος του ${u.username} από «${ROLE_LABELS[u.role]}» σε «${ROLE_LABELS[newRole]}»;`,
      { confirmLabel: "Αλλαγή ρόλου" }
    );
    if (!ok) {
      loadUsers(); // epanafora tou dropdown stin proigoumeni timi
      return;
    }

    try {
      await updateUserRole(u.id, newRole);
      toast.success(`Ο ${u.username} είναι πλέον ${ROLE_LABELS[newRole]}`);
      loadUsers();
    } catch (err) {
      showApiError(err, "Δεν ήταν δυνατή η αλλαγή ρόλου");
      loadUsers();
    }
  }

  // Poious rolous mporo na DOSO se ayton ton xristi
  function assignableRolesFor(u) {
    // Ston eauto mou den allazo rolo
    if (u.username === currentUser?.username) return null;
    // Ton MANAGER den ton peirazei kaneis apo tin efarmogi
    if (u.role === "MANAGER") return null;
    // O epoptis diaxeirizetai mono texnikous
    if (!isManager && u.role !== "TECHNICIAN") return null;
    return availableRoles;
  }

  const pendingApproval = users.filter((u) => !u.active).length;

  return (
    <div>
      <h1 className="page-title">
        <Users size={22} />
        Χρήστες
        <span className="sub">{users.length} λογαριασμοί</span>
      </h1>

      {pendingApproval > 0 && (
        <div
          className="card"
          style={{
            display: "flex",
            alignItems: "center",
            gap: "0.7rem",
            borderLeft: "4px solid var(--warning)",
          }}
        >
          <Clock size={20} style={{ color: "var(--warning)", flexShrink: 0 }} />
          <span>
            <strong>{pendingApproval}</strong>{" "}
            {pendingApproval === 1 ? "λογαριασμός περιμένει" : "λογαριασμοί περιμένουν"} έγκριση.
            Ενεργοποίησέ τους από τη λίστα παρακάτω για να μπορέσουν να συνδεθούν.
          </span>
        </div>
      )}

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
                    onChange={(e) => setForm({ ...form, username: e.target.value.toLowerCase() })}
                  />
                  <span className="muted" style={{ fontSize: "0.76rem" }}>
                    πεζά λατινικά, αριθμοί, τελεία ή παύλα
                  </span>
                </div>
                <div className="form-row">
                  <label>Κωδικός</label>
                  <input
                    type="password"
                    required
                    placeholder="••••••••"
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                  />
                  <span className="muted" style={{ fontSize: "0.76rem" }}>
                    τουλάχιστον 8 χαρακτήρες, με γράμμα και αριθμό
                  </span>
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
                  <th style={{ minWidth: 150 }}>Ρόλος</th>
                  <th>Τίτλος θέσης</th>
                  <th>Κατάσταση</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => {
                  const roles = assignableRolesFor(u);
                  return (
                    <tr key={u.id} style={!u.active ? { background: "var(--warning-soft)" } : undefined}>
                      <td>
                        <div style={{ fontWeight: 600 }}>{u.fullName}</div>
                        <div className="muted mono" style={{ fontSize: "0.78rem" }}>
                          {u.username}
                          {u.username === currentUser?.username && " (εσύ)"}
                        </div>
                      </td>
                      <td>
                        {roles ? (
                          <select
                            value={u.role}
                            onChange={(e) => handleRoleChange(u, e.target.value)}
                            style={{
                              padding: "0.3rem 0.45rem",
                              border: "1px solid var(--border-strong)",
                              borderRadius: "var(--radius-sm)",
                              background: "var(--surface-2)",
                              color: "var(--text)",
                              fontFamily: "inherit",
                              fontSize: "0.82rem",
                            }}
                          >
                            {[...new Set([u.role, ...roles])].map((r) => (
                              <option key={r} value={r}>
                                {ROLE_LABELS[r]}
                              </option>
                            ))}
                          </select>
                        ) : (
                          <span className={`badge role-${u.role}`}>{ROLE_LABELS[u.role]}</span>
                        )}
                      </td>
                      <td className="muted">{u.jobTitle || "—"}</td>
                      <td>
                        <span
                          className="badge dot"
                          style={{
                            background: u.active ? "var(--success-soft)" : "var(--warning-soft)",
                            color: u.active ? "var(--success)" : "var(--warning)",
                          }}
                        >
                          {u.active ? "Ενεργός" : "Αναμονή έγκρισης"}
                        </span>
                      </td>
                      <td style={{ textAlign: "right" }}>
                        {u.username !== currentUser?.username && (
                          <button
                            className="btn ghost small"
                            onClick={() => handleToggleActive(u)}
                            title={u.active ? "Απενεργοποίηση" : "Ενεργοποίηση"}
                          >
                            {u.active ? <UserX size={15} /> : <UserCheck size={15} />}
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
