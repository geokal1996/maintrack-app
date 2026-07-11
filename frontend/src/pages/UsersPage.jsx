import { useEffect, useState } from "react";
import { getUsers, createUser, setUserActive } from "../api/usersApi";

const ROLES = ["TECHNICIAN", "SUPERVISOR"];

const emptyForm = { username: "", password: "", fullName: "", role: "TECHNICIAN" };

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");

  function loadUsers() {
    setLoading(true);
    getUsers()
      .then(setUsers)
      .finally(() => setLoading(false));
  }

  useEffect(loadUsers, []);

  async function handleCreate(e) {
    e.preventDefault();
    setError("");
    try {
      await createUser(form);
      setForm(emptyForm);
      setShowForm(false);
      loadUsers();
    } catch (err) {
      setError("Δεν ήταν δυνατή η δημιουργία - έλεγξε τα στοιχεία (π.χ. αν το username υπάρχει ήδη)");
    }
  }

  async function handleToggleActive(u) {
    await setUserActive(u.id, !u.active);
    loadUsers();
  }

  return (
    <div>
      <div className="card">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <h2 style={{ margin: 0 }}>Χρήστες</h2>
          <button className="btn" onClick={() => setShowForm((s) => !s)}>
            {showForm ? "Ακύρωση" : "+ Νέος χρήστης"}
          </button>
        </div>

        {showForm && (
          <form onSubmit={handleCreate} style={{ marginTop: "1rem" }}>
            <div className="form-row">
              <label>Username</label>
              <input required value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
            </div>
            <div className="form-row">
              <label>Password</label>
              <input type="password" required minLength={4} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
            </div>
            <div className="form-row">
              <label>Ονοματεπώνυμο</label>
              <input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
            </div>
            <div className="form-row">
              <label>Ρόλος</label>
              <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
              </select>
            </div>
            {error && <p className="error-text">{error}</p>}
            <div className="form-actions">
              <button className="btn" type="submit">Αποθήκευση</button>
            </div>
          </form>
        )}
      </div>

      <div className="card">
        {loading ? (
          <p className="muted">Φόρτωση...</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Username</th>
                <th>Ονοματεπώνυμο</th>
                <th>Ρόλος</th>
                <th>Κατάσταση</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.username}</td>
                  <td>{u.fullName}</td>
                  <td>{u.role}</td>
                  <td>{u.active ? "Ενεργός" : "Ανενεργός"}</td>
                  <td>
                    <button className="btn secondary" onClick={() => handleToggleActive(u)}>
                      {u.active ? "Απενεργοποίηση" : "Ενεργοποίηση"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
