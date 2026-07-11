import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getFaults, createFault } from "../api/faultsApi";
import { getMachines } from "../api/machinesApi";

const STATUSES = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];
const SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

const emptyForm = { machineId: "", title: "", description: "", severity: "MEDIUM" };

export default function FaultsPage() {
  const { user } = useAuth();
  const [faults, setFaults] = useState([]);
  const [machines, setMachines] = useState([]);
  const [statusFilter, setStatusFilter] = useState("");
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");

  function loadFaults() {
    setLoading(true);
    getFaults(statusFilter ? { status: statusFilter } : {})
      .then(setFaults)
      .finally(() => setLoading(false));
  }

  useEffect(loadFaults, [statusFilter]);

  useEffect(() => {
    getMachines().then(setMachines);
  }, []);

  async function handleCreate(e) {
    e.preventDefault();
    setError("");
    try {
      await createFault({
        machineId: Number(form.machineId),
        reportedByUserId: user.id,
        title: form.title,
        description: form.description,
        severity: form.severity,
      });
      setForm(emptyForm);
      setShowForm(false);
      loadFaults();
    } catch (err) {
      setError("Δεν ήταν δυνατή η καταχώρηση - έλεγξε τα στοιχεία");
    }
  }

  return (
    <div>
      <div className="card">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <h2 style={{ margin: 0 }}>Βλάβες</h2>
          <button className="btn" onClick={() => setShowForm((s) => !s)}>
            {showForm ? "Ακύρωση" : "+ Νέα βλάβη"}
          </button>
        </div>

        {showForm && (
          <form onSubmit={handleCreate} style={{ marginTop: "1rem" }}>
            <div className="form-row">
              <label>Μηχανή</label>
              <select required value={form.machineId} onChange={(e) => setForm({ ...form, machineId: e.target.value })}>
                <option value="">-- επίλεξε μηχανή --</option>
                {machines.map((m) => (
                  <option key={m.id} value={m.id}>{m.code} — {m.name}</option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <label>Τίτλος</label>
              <input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            </div>
            <div className="form-row">
              <label>Περιγραφή</label>
              <textarea rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="form-row">
              <label>Σοβαρότητα</label>
              <select value={form.severity} onChange={(e) => setForm({ ...form, severity: e.target.value })}>
                {SEVERITIES.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            {error && <p className="error-text">{error}</p>}
            <div className="form-actions">
              <button className="btn" type="submit">Καταχώρηση</button>
            </div>
          </form>
        )}
      </div>

      <div className="card">
        <div className="filters-row">
          <div className="form-row" style={{ marginBottom: 0 }}>
            <label>Φίλτρο κατάστασης</label>
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="">Όλες</option>
              {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        </div>

        {loading ? (
          <p className="muted">Φόρτωση...</p>
        ) : faults.length === 0 ? (
          <p className="muted">Δεν βρέθηκαν βλάβες.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Μηχανή</th>
                <th>Τίτλος</th>
                <th>Σοβαρότητα</th>
                <th>Κατάσταση</th>
                <th>Δημιουργήθηκε</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {faults.map((f) => (
                <tr key={f.id}>
                  <td>{f.machineCode}</td>
                  <td>{f.title}</td>
                  <td><span className={`badge severity-${f.severity}`}>{f.severity}</span></td>
                  <td><span className={`badge status-${f.status}`}>{f.status}</span></td>
                  <td>{new Date(f.createdAt).toLocaleString("el-GR")}</td>
                  <td><Link to={`/faults/${f.id}`}>Λεπτομέρειες →</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
