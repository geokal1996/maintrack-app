import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import toast from "react-hot-toast";
import {
  AlertTriangle,
  Plus,
  X,
  Save,
  Filter,
  ArrowRight,
  CheckCircle2,
  Search,
  UserCheck,
  Download,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { searchFaults, createFault, exportFaults } from "../api/faultsApi";
import { getMachines } from "../api/machinesApi";
import ExcelImportPanel from "../components/ExcelImportPanel";
import Pagination from "../components/Pagination";
import { SkeletonTable } from "../components/Skeleton";
import EmptyState from "../components/EmptyState";

const STATUSES = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];
const SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

const STATUS_LABELS = {
  OPEN: "Ανοιχτή",
  IN_PROGRESS: "Σε εξέλιξη",
  RESOLVED: "Επιλύθηκε",
  CLOSED: "Έκλεισε",
};
const SEVERITY_LABELS = {
  LOW: "Χαμηλή",
  MEDIUM: "Μεσαία",
  HIGH: "Υψηλή",
  CRITICAL: "Κρίσιμη",
};

const emptyForm = { machineId: "", title: "", description: "", severity: "MEDIUM" };

export default function FaultsPage() {
  const { user, canManageUsers } = useAuth();
  const [pageData, setPageData] = useState(null);
  const [machines, setMachines] = useState([]);
  const [statusFilter, setStatusFilter] = useState("");
  // "Μόνο οι δικές μου": deixnei mono osa exoun anatethei ston syndedemeno xristi
  const [onlyMine, setOnlyMine] = useState(false);
  const [search, setSearch] = useState("");
  const [appliedSearch, setAppliedSearch] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(25);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [exporting, setExporting] = useState(false);

  function loadFaults() {
    setLoading(true);
    searchFaults({
      status: statusFilter || undefined,
      assignedToUserId: onlyMine ? user.id : undefined,
      q: appliedSearch,
      page,
      size,
    })
      .then(setPageData)
      .catch(() => toast.error("Δεν ήταν δυνατή η φόρτωση των βλαβών"))
      .finally(() => setLoading(false));
  }

  useEffect(loadFaults, [statusFilter, onlyMine, appliedSearch, page, size]);

  useEffect(() => {
    getMachines().then(setMachines);
  }, []);

  // I anazitisi den trexei se kathe pliktrologisi - perimenei 400ms afou stamatisei
  // na grafei o xristis. Alliws tha stelname ena request ana grama.
  useEffect(() => {
    const timer = setTimeout(() => {
      setAppliedSearch(search.trim());
      setPage(0);
    }, 400);
    return () => clearTimeout(timer);
  }, [search]);

  async function handleCreate(e) {
    e.preventDefault();
    setSaving(true);
    try {
      await createFault({
        machineId: Number(form.machineId),
        reportedByUserId: user.id,
        title: form.title,
        description: form.description,
        severity: form.severity,
      });
      toast.success("Η βλάβη καταχωρήθηκε");
      setForm(emptyForm);
      setShowForm(false);
      setPage(0);
      loadFaults();
    } catch (err) {
      toast.error(
        err.response?.data?.message || "Δεν ήταν δυνατή η καταχώρηση — έλεγξε τα στοιχεία"
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleExport() {
    setExporting(true);
    try {
      await exportFaults({
        status: statusFilter || undefined,
        assignedToUserId: onlyMine ? user.id : undefined,
        q: appliedSearch,
      });
      toast.success("Το αρχείο κατέβηκε");
    } catch {
      toast.error("Δεν ήταν δυνατή η εξαγωγή");
    } finally {
      setExporting(false);
    }
  }

  const faults = pageData?.content || [];
  const hasFilters = appliedSearch || statusFilter || onlyMine;

  return (
    <div>
      <h1 className="page-title">
        <AlertTriangle size={22} />
        Βλάβες
        <span className="sub">{pageData ? `${pageData.totalElements} εγγραφές` : ""}</span>
      </h1>

      <div className="card">
        <div className="card-header">
          <h2>Καταχώρηση</h2>
          <button className="btn" onClick={() => setShowForm((s) => !s)}>
            {showForm ? <X size={16} /> : <Plus size={16} />}
            {showForm ? "Ακύρωση" : "Νέα βλάβη"}
          </button>
        </div>

        {showForm && (
          <>
            <div className="divider" />
            <form onSubmit={handleCreate}>
              <div className="form-grid">
                <div className="form-row">
                  <label>Μηχανή</label>
                  <select
                    required
                    value={form.machineId}
                    onChange={(e) => setForm({ ...form, machineId: e.target.value })}
                  >
                    <option value="">— επίλεξε μηχανή —</option>
                    {machines.map((m) => (
                      <option key={m.id} value={m.id}>
                        {m.code} — {m.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-row">
                  <label>Σοβαρότητα</label>
                  <select
                    value={form.severity}
                    onChange={(e) => setForm({ ...form, severity: e.target.value })}
                  >
                    {SEVERITIES.map((s) => (
                      <option key={s} value={s}>
                        {SEVERITY_LABELS[s]}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="form-row">
                <label>Τίτλος</label>
                <input
                  required
                  placeholder="π.χ. Διαρροή λαδιού από υδραυλική μονάδα"
                  value={form.title}
                  onChange={(e) => setForm({ ...form, title: e.target.value })}
                />
              </div>
              <div className="form-row">
                <label>Περιγραφή</label>
                <textarea
                  rows={3}
                  placeholder="Σύντομη περιγραφή του προβλήματος..."
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>
              <div className="form-actions">
                <button className="btn" type="submit" disabled={saving}>
                  <Save size={16} />
                  {saving ? "Καταχώρηση..." : "Καταχώρηση"}
                </button>
              </div>
            </form>
          </>
        )}
      </div>

      {/* I mazikí eisagogí einai dikaioma SUPERVISOR/MANAGER */}
      {canManageUsers && <ExcelImportPanel onImported={loadFaults} />}

      <div className="card">
        <div className="filters-row">
          <div className="form-row" style={{ marginBottom: 0, minWidth: 180 }}>
            <label>
              <Filter size={13} style={{ verticalAlign: -2, marginRight: 4 }} />
              Κατάσταση
            </label>
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
            >
              <option value="">Όλες</option>
              {STATUSES.map((s) => (
                <option key={s} value={s}>
                  {STATUS_LABELS[s]}
                </option>
              ))}
            </select>
          </div>
          <div className="form-row" style={{ marginBottom: 0, flex: 1, maxWidth: 320 }}>
            <label>
              <Search size={13} style={{ verticalAlign: -2, marginRight: 4 }} />
              Αναζήτηση
            </label>
            <input
              placeholder="Τίτλος ή μηχανή..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <button
            type="button"
            className={onlyMine ? "btn small" : "btn secondary small"}
            style={{ alignSelf: "flex-end" }}
            onClick={() => {
              setOnlyMine((v) => !v);
              setPage(0);
            }}
            title="Δείχνει μόνο τις βλάβες που έχουν ανατεθεί σε εμένα"
          >
            <UserCheck size={15} /> Ανατεθειμένες σε εμένα
          </button>
          <button
            type="button"
            className="btn secondary small"
            style={{ alignSelf: "flex-end" }}
            onClick={handleExport}
            disabled={exporting || faults.length === 0}
            title="Κατεβάζει σε Excel ό,τι δείχνουν τα φίλτρα αυτή τη στιγμή"
          >
            <Download size={15} /> {exporting ? "Εξαγωγή..." : "Εξαγωγή σε Excel"}
          </button>
        </div>

        {loading ? (
          <SkeletonTable rows={8} cols={6} />
        ) : faults.length === 0 ? (
          <EmptyState
            icon={CheckCircle2}
            message={hasFilters ? "Καμία βλάβη δεν ταιριάζει με τα φίλτρα." : "Δεν βρέθηκαν βλάβες."}
          />
        ) : (
          <>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Μηχανή</th>
                    <th>Τίτλος</th>
                    <th>Σοβαρότητα</th>
                    <th>Κατάσταση</th>
                    <th>Ανατέθηκε σε</th>
                    <th>Δημιουργήθηκε</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {faults.map((f) => (
                    <tr key={f.id}>
                      <td className={`sev-cell sev-${f.severity} mono`}>{f.machineCode}</td>
                      <td>{f.title}</td>
                      <td>
                        <span className={`badge severity-${f.severity}`}>
                          {SEVERITY_LABELS[f.severity]}
                        </span>
                      </td>
                      <td>
                        <span className={`badge dot status-${f.status}`}>
                          {STATUS_LABELS[f.status]}
                        </span>
                      </td>
                      <td>
                        {f.assignedToUsername ? (
                          f.assignedToFullName || f.assignedToUsername
                        ) : (
                          <span className="muted">—</span>
                        )}
                      </td>
                      <td className="muted">{new Date(f.createdAt).toLocaleString("el-GR")}</td>
                      <td>
                        <Link
                          to={`/faults/${f.id}`}
                          style={{ display: "inline-flex", alignItems: "center", gap: 4 }}
                        >
                          Λεπτομέρειες <ArrowRight size={14} />
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <Pagination
              page={pageData.page}
              totalPages={pageData.totalPages}
              totalElements={pageData.totalElements}
              size={pageData.size}
              onPageChange={setPage}
              onSizeChange={(s) => {
                setSize(s);
                setPage(0);
              }}
            />
          </>
        )}
      </div>
    </div>
  );
}
