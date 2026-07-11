import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getFault, updateFaultStatus, getFaultActions, addFaultAction } from "../api/faultsApi";

// Poies allages katastasis epitrepontai apo poy - taeriazei me ti logiki tou backend
const NEXT_STATUSES = {
  OPEN: ["IN_PROGRESS"],
  IN_PROGRESS: ["RESOLVED"],
  RESOLVED: ["CLOSED"],
  CLOSED: [],
};

const emptyActionForm = { description: "", downtimeMinutes: "" };

export default function FaultDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const [fault, setFault] = useState(null);
  const [actions, setActions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionForm, setActionForm] = useState(emptyActionForm);
  const [error, setError] = useState("");
  const [statusError, setStatusError] = useState("");

  function loadAll() {
    setLoading(true);
    Promise.all([getFault(id), getFaultActions(id)])
      .then(([f, a]) => {
        setFault(f);
        setActions(a);
      })
      .finally(() => setLoading(false));
  }

  useEffect(loadAll, [id]);

  async function handleStatusChange(newStatus) {
    setStatusError("");
    try {
      await updateFaultStatus(id, newStatus);
      loadAll();
    } catch (err) {
      setStatusError("Δεν ήταν δυνατή η αλλαγή κατάστασης");
    }
  }

  async function handleAddAction(e) {
    e.preventDefault();
    setError("");
    try {
      await addFaultAction(id, {
        technicianUserId: user.id,
        description: actionForm.description,
        downtimeMinutes: actionForm.downtimeMinutes ? Number(actionForm.downtimeMinutes) : null,
      });
      setActionForm(emptyActionForm);
      loadAll();
    } catch (err) {
      setError("Δεν ήταν δυνατή η καταχώρηση ενέργειας");
    }
  }

  if (loading) return <p className="muted">Φόρτωση...</p>;
  if (!fault) return <p className="error-text">Δεν βρέθηκε η βλάβη.</p>;

  const nextOptions = NEXT_STATUSES[fault.status] || [];

  return (
    <div>
      <p><Link to="/faults">← Πίσω στη λίστα βλαβών</Link></p>

      <div className="card">
        <h2>{fault.title}</h2>
        <p className="muted">
          Μηχανή: <strong>{fault.machineCode} — {fault.machineName}</strong> · Ανέφερε: {fault.reportedByUsername}
        </p>
        <p>{fault.description || <span className="muted">Χωρίς περιγραφή</span>}</p>
        <p>
          <span className={`badge severity-${fault.severity}`}>{fault.severity}</span>{" "}
          <span className={`badge status-${fault.status}`}>{fault.status}</span>
        </p>
        <p className="muted">
          Δημιουργήθηκε: {new Date(fault.createdAt).toLocaleString("el-GR")}
          {fault.resolvedAt && <> · Λύθηκε: {new Date(fault.resolvedAt).toLocaleString("el-GR")}</>}
        </p>

        {nextOptions.length > 0 && (
          <div className="form-actions">
            {nextOptions.map((s) => (
              <button key={s} className="btn secondary" onClick={() => handleStatusChange(s)}>
                Αλλαγή σε {s}
              </button>
            ))}
          </div>
        )}
        {statusError && <p className="error-text">{statusError}</p>}
      </div>

      <div className="card">
        <h2>Ενέργειες συντήρησης</h2>
        {actions.length === 0 ? (
          <p className="muted">Δεν έχουν καταχωρηθεί ενέργειες ακόμα.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Τεχνικός</th>
                <th>Περιγραφή</th>
                <th>Διάρκεια (λεπτά)</th>
                <th>Ημερομηνία</th>
              </tr>
            </thead>
            <tbody>
              {actions.map((a) => (
                <tr key={a.id}>
                  <td>{a.technicianUsername}</td>
                  <td>{a.description}</td>
                  <td>{a.downtimeMinutes ?? "-"}</td>
                  <td>{new Date(a.actionDate).toLocaleString("el-GR")}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {fault.status !== "CLOSED" && (
          <form onSubmit={handleAddAction} style={{ marginTop: "1rem" }}>
            <div className="form-row">
              <label>Περιγραφή ενέργειας</label>
              <textarea
                rows={2}
                required
                value={actionForm.description}
                onChange={(e) => setActionForm({ ...actionForm, description: e.target.value })}
              />
            </div>
            <div className="form-row">
              <label>Διάρκεια διακοπής (λεπτά, προαιρετικό)</label>
              <input
                type="number"
                min="0"
                value={actionForm.downtimeMinutes}
                onChange={(e) => setActionForm({ ...actionForm, downtimeMinutes: e.target.value })}
              />
            </div>
            {error && <p className="error-text">{error}</p>}
            <div className="form-actions">
              <button className="btn" type="submit">Καταχώρηση ενέργειας</button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
