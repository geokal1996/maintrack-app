import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { getMachines, createMachine, deleteMachine } from "../api/machinesApi";

const STATUSES = ["OPERATIONAL", "DOWN", "UNDER_MAINTENANCE"];

const emptyForm = { code: "", name: "", area: "", status: "OPERATIONAL" };

export default function MachinesPage() {
  const { isSupervisor } = useAuth();
  const [machines, setMachines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");

  function loadMachines() {
    setLoading(true);
    getMachines()
      .then(setMachines)
      .finally(() => setLoading(false));
  }

  useEffect(loadMachines, []);

  async function handleCreate(e) {
    e.preventDefault();
    setError("");
    try {
      await createMachine(form);
      setForm(emptyForm);
      setShowForm(false);
      loadMachines();
    } catch (err) {
      setError("Δεν ήταν δυνατή η δημιουργία - έλεγξε τα στοιχεία");
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("Σίγουρα θέλεις να διαγράψεις αυτή τη μηχανή;")) return;
    await deleteMachine(id);
    loadMachines();
  }

  return (
    <div>
      <div className="card">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <h2 style={{ margin: 0 }}>Μηχανές</h2>
          {isSupervisor && (
            <button className="btn" onClick={() => setShowForm((s) => !s)}>
              {showForm ? "Ακύρωση" : "+ Νέα μηχανή"}
            </button>
          )}
        </div>

        {showForm && (
          <form onSubmit={handleCreate} style={{ marginTop: "1rem" }}>
            <div className="form-row">
              <label>Κωδικός</label>
              <input required value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} />
            </div>
            <div className="form-row">
              <label>Όνομα</label>
              <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div className="form-row">
              <label>Περιοχή</label>
              <input value={form.area} onChange={(e) => setForm({ ...form, area: e.target.value })} />
            </div>
            <div className="form-row">
              <label>Κατάσταση</label>
              <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
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
                <th>Κωδικός</th>
                <th>Όνομα</th>
                <th>Περιοχή</th>
                <th>Κατάσταση</th>
                {isSupervisor && <th></th>}
              </tr>
            </thead>
            <tbody>
              {machines.map((m) => (
                <tr key={m.id}>
                  <td>{m.code}</td>
                  <td>{m.name}</td>
                  <td>{m.area}</td>
                  <td><span className={`badge status-${m.status}`}>{m.status}</span></td>
                  {isSupervisor && (
                    <td>
                      <button className="btn danger" onClick={() => handleDelete(m.id)}>Διαγραφή</button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
