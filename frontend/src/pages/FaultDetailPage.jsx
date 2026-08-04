import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import toast from "react-hot-toast";
import {
  ArrowLeft,
  Cog,
  User as UserIcon,
  Clock,
  Wrench,
  Save,
  ChevronRight,
  CalendarCheck,
  FileSpreadsheet,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { getFault, updateFaultStatus, getFaultActions, addFaultAction } from "../api/faultsApi";
import { SkeletonBlock } from "../components/Skeleton";
import EmptyState from "../components/EmptyState";

// Poies allages katastasis epitrepontai apo poy - taeriazei me ti logiki tou backend
const NEXT_STATUSES = {
  OPEN: ["IN_PROGRESS"],
  IN_PROGRESS: ["RESOLVED"],
  RESOLVED: ["CLOSED"],
  CLOSED: [],
};

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

const emptyActionForm = { description: "", downtimeMinutes: "" };

export default function FaultDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const [fault, setFault] = useState(null);
  const [actions, setActions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionForm, setActionForm] = useState(emptyActionForm);
  const [saving, setSaving] = useState(false);

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
    try {
      await updateFaultStatus(id, newStatus);
      toast.success(`Η βλάβη πέρασε σε «${STATUS_LABELS[newStatus]}»`);
      loadAll();
    } catch (err) {
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η αλλαγή κατάστασης");
    }
  }

  async function handleAddAction(e) {
    e.preventDefault();
    setSaving(true);
    try {
      await addFaultAction(id, {
        technicianUserId: user.id,
        description: actionForm.description,
        downtimeMinutes: actionForm.downtimeMinutes ? Number(actionForm.downtimeMinutes) : null,
      });
      toast.success("Η ενέργεια καταχωρήθηκε");
      setActionForm(emptyActionForm);
      loadAll();
    } catch (err) {
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η καταχώρηση ενέργειας");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div>
        <SkeletonBlock height={180} />
        <div style={{ height: "1.25rem" }} />
        <SkeletonBlock height={240} />
      </div>
    );
  }

  if (!fault) {
    return <EmptyState message="Δεν βρέθηκε η βλάβη." />;
  }

  const nextOptions = NEXT_STATUSES[fault.status] || [];
  const totalDowntime = actions.reduce((sum, a) => sum + (a.downtimeMinutes || 0), 0);

  return (
    <div>
      <p style={{ marginTop: 0 }}>
        <Link to="/faults" style={{ display: "inline-flex", alignItems: "center", gap: 5 }}>
          <ArrowLeft size={15} /> Πίσω στη λίστα βλαβών
        </Link>
      </p>

      <div className="card">
        <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap", marginBottom: "0.7rem" }}>
          <span className={`badge severity-${fault.severity}`}>{SEVERITY_LABELS[fault.severity]}</span>
          <span className={`badge dot status-${fault.status}`}>{STATUS_LABELS[fault.status]}</span>
          {fault.externalRef && (
            <span className="chip">
              <FileSpreadsheet size={12} /> SAP #{fault.externalRef}
            </span>
          )}
        </div>

        <h2 style={{ fontSize: "1.3rem", marginBottom: "0.6rem" }}>{fault.title}</h2>

        <div style={{ display: "flex", gap: "1.1rem", flexWrap: "wrap", marginBottom: "0.9rem" }}>
          <span className="chip">
            <Cog size={12} /> {fault.machineCode} — {fault.machineName}
          </span>
          <span className="chip">
            <UserIcon size={12} /> {fault.reportedByUsername}
          </span>
          <span className="chip">
            <Clock size={12} /> {new Date(fault.createdAt).toLocaleString("el-GR")}
          </span>
          {fault.resolvedAt && (
            <span className="chip">
              <CalendarCheck size={12} /> Λύθηκε {new Date(fault.resolvedAt).toLocaleString("el-GR")}
            </span>
          )}
          {totalDowntime > 0 && (
            <span className="chip">
              <Wrench size={12} /> Σύνολο διακοπής {totalDowntime}′
            </span>
          )}
        </div>

        <p style={{ lineHeight: 1.6, margin: 0 }}>
          {fault.description || <span className="muted">Χωρίς περιγραφή</span>}
        </p>

        {nextOptions.length > 0 && (
          <>
            <div className="divider" />
            <div className="form-actions">
              {nextOptions.map((s) => (
                <button key={s} className="btn secondary" onClick={() => handleStatusChange(s)}>
                  Αλλαγή σε «{STATUS_LABELS[s]}» <ChevronRight size={15} />
                </button>
              ))}
            </div>
          </>
        )}
      </div>

      <div className="card">
        <h2>
          <Wrench size={17} /> Ενέργειες συντήρησης
        </h2>

        {actions.length === 0 ? (
          <EmptyState
            icon={Wrench}
            message="Δεν έχουν καταχωρηθεί ενέργειες ακόμα."
            hint="Πρόσθεσε την πρώτη ενέργεια από τη φόρμα παρακάτω."
          />
        ) : (
          <ul className="timeline">
            {actions.map((a) => (
              <li key={a.id}>
                <div className="tl-head">
                  <strong>{a.technicianUsername}</strong>
                  <span>{new Date(a.actionDate).toLocaleString("el-GR")}</span>
                  {a.downtimeMinutes != null && (
                    <span className="chip">
                      <Clock size={11} /> {a.downtimeMinutes}′ διακοπή
                    </span>
                  )}
                </div>
                <div className="tl-body">{a.description}</div>
              </li>
            ))}
          </ul>
        )}

        {fault.status !== "CLOSED" && (
          <>
            <div className="divider" />
            <form onSubmit={handleAddAction}>
              <div className="form-row">
                <label>Περιγραφή ενέργειας</label>
                <textarea
                  rows={2}
                  required
                  placeholder="π.χ. Αντικατάσταση τσιμούχας και συμπλήρωση λαδιού"
                  value={actionForm.description}
                  onChange={(e) => setActionForm({ ...actionForm, description: e.target.value })}
                />
              </div>
              <div className="form-row" style={{ maxWidth: 260 }}>
                <label>Διάρκεια διακοπής σε λεπτά (προαιρετικό)</label>
                <input
                  type="number"
                  min="0"
                  placeholder="π.χ. 45"
                  value={actionForm.downtimeMinutes}
                  onChange={(e) => setActionForm({ ...actionForm, downtimeMinutes: e.target.value })}
                />
              </div>
              <div className="form-actions">
                <button className="btn" type="submit" disabled={saving}>
                  <Save size={16} />
                  {saving ? "Καταχώρηση..." : "Καταχώρηση ενέργειας"}
                </button>
              </div>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
