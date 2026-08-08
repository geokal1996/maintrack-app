import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import toast from "react-hot-toast";
import { Cog, Plus, X, Trash2, Save, Search, Pencil } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { getMachines, createMachine, updateMachine, deleteMachine } from "../api/machinesApi";
import { SkeletonTable } from "../components/Skeleton";
import EmptyState from "../components/EmptyState";
import { confirmToast } from "../components/confirmToast";

const STATUSES = ["OPERATIONAL", "DOWN", "UNDER_MAINTENANCE"];
const STATUS_LABELS = {
  OPERATIONAL: "Σε λειτουργία",
  DOWN: "Εκτός λειτουργίας",
  UNDER_MAINTENANCE: "Σε συντήρηση",
};

const emptyForm = { code: "", name: "", area: "", status: "OPERATIONAL" };

export default function MachinesPage() {
  const { canManageMachines, canDeleteMachines } = useAuth();
  const [machines, setMachines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [search, setSearch] = useState("");

  // An einai null -> dimiourgia neas. An exei id -> epexergasia yparxousas.
  const [editingId, setEditingId] = useState(null);

  function loadMachines() {
    setLoading(true);
    getMachines()
      .then(setMachines)
      .finally(() => setLoading(false));
  }

  useEffect(loadMachines, []);

  function startCreating() {
    setEditingId(null);
    setForm(emptyForm);
    setShowForm(true);
  }

  function startEditing(machine) {
    setEditingId(machine.id);
    setForm({
      code: machine.code,
      name: machine.name,
      area: machine.area || "",
      status: machine.status,
    });
    setShowForm(true);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function cancelForm() {
    setShowForm(false);
    setEditingId(null);
    setForm(emptyForm);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    try {
      if (editingId) {
        await updateMachine(editingId, form);
        toast.success(`Η μηχανή ${form.code} ενημερώθηκε`);
      } else {
        await createMachine(form);
        toast.success(`Η μηχανή ${form.code} καταχωρήθηκε`);
      }
      cancelForm();
      loadMachines();
    } catch (err) {
      toast.error(
        err.response?.data?.message || "Δεν ήταν δυνατή η αποθήκευση — έλεγξε τα στοιχεία"
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(machine) {
    const ok = await confirmToast(
      `Να διαγραφεί οριστικά η μηχανή ${machine.code} — ${machine.name};`,
      { confirmLabel: "Διαγραφή" }
    );
    if (!ok) return;

    try {
      await deleteMachine(machine.id);
      toast.success(`Η μηχανή ${machine.code} διαγράφηκε`);
      loadMachines();
    } catch (err) {
      toast.error(
        err.response?.data?.message ||
          "Δεν ήταν δυνατή η διαγραφή — ίσως έχει καταχωρημένες βλάβες"
      );
    }
  }

  const filtered = machines.filter((m) => {
    const q = search.trim().toLowerCase();
    if (!q) return true;
    return [m.code, m.name, m.area].filter(Boolean).some((v) => v.toLowerCase().includes(q));
  });

  return (
    <div>
      <h1 className="page-title">
        <Cog size={22} />
        Μηχανές
        <span className="sub">{machines.length} καταχωρημένες</span>
      </h1>

      {canManageMachines && (
        <div className="card">
          <div className="card-header">
            <h2>{editingId ? "Επεξεργασία μηχανής" : "Διαχείριση"}</h2>
            <button className="btn" onClick={showForm ? cancelForm : startCreating}>
              {showForm ? <X size={16} /> : <Plus size={16} />}
              {showForm ? "Ακύρωση" : "Νέα μηχανή"}
            </button>
          </div>

          {showForm && (
            <>
              <div className="divider" />
              <form onSubmit={handleSubmit}>
                <div className="form-grid">
                  <div className="form-row">
                    <label>Κωδικός</label>
                    <input
                      required
                      placeholder="π.χ. CRL1"
                      value={form.code}
                      onChange={(e) => setForm({ ...form, code: e.target.value })}
                    />
                  </div>
                  <div className="form-row">
                    <label>Όνομα</label>
                    <input
                      required
                      placeholder="π.χ. Γραμμή Ψυχρής Έλασης 1"
                      value={form.name}
                      onChange={(e) => setForm({ ...form, name: e.target.value })}
                    />
                  </div>
                  <div className="form-row">
                    <label>Περιοχή</label>
                    <input
                      placeholder="π.χ. Έλαση"
                      value={form.area}
                      onChange={(e) => setForm({ ...form, area: e.target.value })}
                    />
                  </div>
                  <div className="form-row">
                    <label>Κατάσταση</label>
                    <select
                      value={form.status}
                      onChange={(e) => setForm({ ...form, status: e.target.value })}
                    >
                      {STATUSES.map((s) => (
                        <option key={s} value={s}>
                          {STATUS_LABELS[s]}
                        </option>
                      ))}
                    </select>
                    <span className="muted" style={{ fontSize: "0.76rem" }}>
                      υπολογίζεται αυτόματα από τις βλάβες — άλλαξέ την μόνο για διόρθωση
                    </span>
                  </div>
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
      )}

      <div className="card">
        <div className="filters-row">
          <div className="form-row" style={{ marginBottom: 0, flex: 1, maxWidth: 320 }}>
            <label>
              <Search size={13} style={{ verticalAlign: -2, marginRight: 4 }} />
              Αναζήτηση
            </label>
            <input
              placeholder="Κωδικός, όνομα ή περιοχή..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        {loading ? (
          <SkeletonTable rows={5} cols={4} />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={Cog}
            message={search ? "Καμία μηχανή δεν ταιριάζει με την αναζήτηση." : "Δεν υπάρχουν μηχανές."}
          />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Κωδικός</th>
                  <th>Όνομα</th>
                  <th>Περιοχή</th>
                  <th>Κατάσταση</th>
                  {canManageMachines && <th></th>}
                </tr>
              </thead>
              <tbody>
                {filtered.map((m) => (
                  <tr key={m.id}>
                    <td className="mono">
                      <Link to={`/machines/${m.id}`}>{m.code}</Link>
                    </td>
                    <td>{m.name}</td>
                    <td className="muted">{m.area || "—"}</td>
                    <td>
                      <span className={`badge dot status-${m.status}`}>{STATUS_LABELS[m.status]}</span>
                    </td>
                    {canManageMachines && (
                      <td style={{ textAlign: "right", whiteSpace: "nowrap" }}>
                        <button
                          className="btn ghost small"
                          onClick={() => startEditing(m)}
                          title="Επεξεργασία"
                        >
                          <Pencil size={15} />
                        </button>
                        {canDeleteMachines && (
                          <button
                            className="btn ghost small"
                            onClick={() => handleDelete(m)}
                            title="Διαγραφή"
                          >
                            <Trash2 size={15} />
                          </button>
                        )}
                      </td>
                    )}
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
