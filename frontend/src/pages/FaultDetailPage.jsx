import { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
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
  Pencil,
  Trash2,
  X,
  UserCheck,
  History,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import {
  getFault,
  updateFault,
  deleteFault,
  updateFaultStatus,
  assignFault,
  getFaultHistory,
  getFaultActions,
  addFaultAction,
  updateFaultAction,
  deleteFaultAction,
} from "../api/faultsApi";
import { getMachines } from "../api/machinesApi";
import { getUsers } from "../api/usersApi";
import { SkeletonBlock } from "../components/Skeleton";
import EmptyState from "../components/EmptyState";
import { confirmToast } from "../components/confirmToast";

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
const SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

const emptyActionForm = { description: "", downtimeMinutes: "" };

export default function FaultDetailPage() {
  const { id } = useParams();
  const { user, isManager, canManageUsers } = useAuth();
  const navigate = useNavigate();

  const [fault, setFault] = useState(null);
  const [actions, setActions] = useState([]);
  const [history, setHistory] = useState([]);
  const [machines, setMachines] = useState([]);
  const [technicians, setTechnicians] = useState([]);
  const [assigning, setAssigning] = useState(false);
  const [loading, setLoading] = useState(true);

  const [actionForm, setActionForm] = useState(emptyActionForm);
  const [saving, setSaving] = useState(false);

  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState(null);

  // Poia energeia epexergazomaste tin ora auti (id i null)
  const [editingActionId, setEditingActionId] = useState(null);
  const [actionEditForm, setActionEditForm] = useState(emptyActionForm);

  function loadAll() {
    setLoading(true);
    Promise.all([getFault(id), getFaultActions(id), getFaultHistory(id)])
      .then(([f, a, h]) => {
        setFault(f);
        setActions(a);
        setHistory(h);
      })
      .finally(() => setLoading(false));
  }

  useEffect(loadAll, [id]);

  useEffect(() => {
    getMachines().then(setMachines);
  }, []);

  // Ti lista xriston ti fernoume MONO an o syndedemenos exei dikaioma na ti dei.
  // O texnikos den exei prosvasi sto /api/users - gi' auton deixnoume mono
  // to koumpi "Ανάθεση σε εμένα" parakato.
  useEffect(() => {
    if (!canManageUsers) return;
    getUsers()
      .then((list) => setTechnicians(list.filter((u) => u.active)))
      .catch(() => setTechnicians([]));
  }, [canManageUsers]);

  function startEditing() {
    setEditForm({
      machineId: String(fault.machineId),
      title: fault.title,
      description: fault.description || "",
      severity: fault.severity,
    });
    setEditing(true);
  }

  async function handleSaveEdit(e) {
    e.preventDefault();
    setSaving(true);
    try {
      await updateFault(id, {
        machineId: Number(editForm.machineId),
        title: editForm.title,
        description: editForm.description,
        severity: editForm.severity,
      });
      toast.success("Η βλάβη ενημερώθηκε");
      setEditing(false);
      loadAll();
    } catch (err) {
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η ενημέρωση");
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteFault() {
    const ok = await confirmToast(
      `Να διαγραφεί οριστικά η βλάβη «${fault.title}»; Θα χαθούν και οι ${actions.length} ενέργειες συντήρησής της.`,
      { confirmLabel: "Διαγραφή" }
    );
    if (!ok) return;

    try {
      await deleteFault(id);
      toast.success("Η βλάβη διαγράφηκε");
      navigate("/faults");
    } catch (err) {
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η διαγραφή");
    }
  }

  async function handleStatusChange(newStatus) {
    try {
      await updateFaultStatus(id, newStatus);
      toast.success(`Η βλάβη πέρασε σε «${STATUS_LABELS[newStatus]}»`);
      loadAll();
    } catch (err) {
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η αλλαγή κατάστασης");
    }
  }

  async function handleAssign(userId) {
    setAssigning(true);
    try {
      await assignFault(id, userId);
      toast.success(userId ? "Η βλάβη ανατέθηκε" : "Η ανάθεση αφαιρέθηκε");
      loadAll();
    } catch (err) {
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η ανάθεση");
    } finally {
      setAssigning(false);
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

  function startEditingAction(action) {
    setEditingActionId(action.id);
    setActionEditForm({
      description: action.description || "",
      downtimeMinutes: action.downtimeMinutes ?? "",
    });
  }

  async function handleSaveAction(actionId) {
    try {
      await updateFaultAction(id, actionId, {
        technicianUserId: user.id,
        description: actionEditForm.description,
        downtimeMinutes: actionEditForm.downtimeMinutes
          ? Number(actionEditForm.downtimeMinutes)
          : null,
      });
      toast.success("Η ενέργεια ενημερώθηκε");
      setEditingActionId(null);
      loadAll();
    } catch (err) {
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η ενημέρωση");
    }
  }

  async function handleDeleteAction(action) {
    const ok = await confirmToast("Να διαγραφεί αυτή η ενέργεια συντήρησης;", {
      confirmLabel: "Διαγραφή",
    });
    if (!ok) return;

    try {
      await deleteFaultAction(id, action.id);
      toast.success("Η ενέργεια διαγράφηκε");
      loadAll();
    } catch (err) {
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η διαγραφή");
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
        {editing ? (
          <form onSubmit={handleSaveEdit}>
            <div className="card-header" style={{ marginBottom: "0.8rem" }}>
              <h2 style={{ margin: 0 }}>
                <Pencil size={17} /> Επεξεργασία βλάβης
              </h2>
              <button type="button" className="btn ghost small" onClick={() => setEditing(false)}>
                <X size={15} /> Ακύρωση
              </button>
            </div>

            <div className="form-grid">
              <div className="form-row">
                <label>Μηχανή</label>
                <select
                  required
                  value={editForm.machineId}
                  onChange={(e) => setEditForm({ ...editForm, machineId: e.target.value })}
                >
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
                  value={editForm.severity}
                  onChange={(e) => setEditForm({ ...editForm, severity: e.target.value })}
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
                value={editForm.title}
                onChange={(e) => setEditForm({ ...editForm, title: e.target.value })}
              />
            </div>
            <div className="form-row">
              <label>Περιγραφή</label>
              <textarea
                rows={3}
                value={editForm.description}
                onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
              />
            </div>
            <div className="form-actions">
              <button className="btn" type="submit" disabled={saving}>
                <Save size={16} />
                {saving ? "Αποθήκευση..." : "Αποθήκευση"}
              </button>
            </div>
          </form>
        ) : (
          <>
            <div className="card-header" style={{ marginBottom: "0.7rem" }}>
              <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap" }}>
                <span className={`badge severity-${fault.severity}`}>
                  {SEVERITY_LABELS[fault.severity]}
                </span>
                <span className={`badge dot status-${fault.status}`}>
                  {STATUS_LABELS[fault.status]}
                </span>
                {fault.externalRef && (
                  <span className="chip">
                    <FileSpreadsheet size={12} /> Εξωτ. κωδικός {fault.externalRef}
                  </span>
                )}
              </div>

              <div style={{ display: "flex", gap: "0.4rem" }}>
                <button className="btn ghost small" onClick={startEditing} title="Επεξεργασία">
                  <Pencil size={15} /> Επεξεργασία
                </button>
                {isManager && (
                  <button className="btn ghost small" onClick={handleDeleteFault} title="Διαγραφή">
                    <Trash2 size={15} />
                  </button>
                )}
              </div>
            </div>

            <h2 style={{ fontSize: "1.3rem", marginBottom: "0.6rem" }}>{fault.title}</h2>

            <div style={{ display: "flex", gap: "1.1rem", flexWrap: "wrap", marginBottom: "0.9rem" }}>
              <Link
                to={`/machines/${fault.machineId}`}
                className="chip"
                style={{ textDecoration: "none" }}
              >
                <Cog size={12} /> {fault.machineCode} — {fault.machineName}
              </Link>
              <span className="chip" title="Ποιος ανέφερε τη βλάβη">
                <UserIcon size={12} /> {fault.reportedByUsername}
              </span>
              {fault.assignedToUsername && (
                <span className="chip" title="Ποιος είναι υπεύθυνος για την αποκατάσταση">
                  <UserCheck size={12} /> Ανατέθηκε σε{" "}
                  {fault.assignedToFullName || fault.assignedToUsername}
                </span>
              )}
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

            {fault.status !== "CLOSED" && (
              <>
                <div className="divider" />
                {canManageUsers ? (
                  // Proistamenos / Diefthintis: anathesi se opoiondipote energo xristi
                  <div className="form-row" style={{ maxWidth: 340, marginBottom: 0 }}>
                    <label>
                      <UserCheck size={14} /> Υπεύθυνος αποκατάστασης
                    </label>
                    <select
                      disabled={assigning}
                      value={fault.assignedToUserId ?? ""}
                      onChange={(e) =>
                        handleAssign(e.target.value ? Number(e.target.value) : null)
                      }
                    >
                      <option value="">— Χωρίς ανάθεση —</option>
                      {technicians.map((t) => (
                        <option key={t.id} value={t.id}>
                          {t.fullName} ({t.username})
                        </option>
                      ))}
                    </select>
                  </div>
                ) : (
                  // Texnikos: mporei mono na "parei" i na "afisei" ti vlavi o idios
                  <div className="form-actions">
                    {fault.assignedToUserId === user.id ? (
                      <button
                        className="btn secondary"
                        disabled={assigning}
                        onClick={() => handleAssign(null)}
                      >
                        <X size={15} /> Αφαίρεση της ανάθεσής μου
                      </button>
                    ) : (
                      <button
                        className="btn secondary"
                        disabled={assigning || !!fault.assignedToUserId}
                        title={
                          fault.assignedToUserId
                            ? "Η βλάβη έχει ήδη ανατεθεί σε άλλον τεχνικό"
                            : undefined
                        }
                        onClick={() => handleAssign(user.id)}
                      >
                        <UserCheck size={15} /> Ανάθεση σε εμένα
                      </button>
                    )}
                  </div>
                )}
              </>
            )}
          </>
        )}
      </div>

      <div className="card">
        <h2>
          <History size={17} /> Ιστορικό καταστάσεων
        </h2>
        {history.length === 0 ? (
          <EmptyState icon={History} message="Δεν υπάρχουν καταγεγραμμένες αλλαγές." />
        ) : (
          <ul className="timeline">
            {history.map((h) => (
              <li key={h.id}>
                <div className="tl-head">
                  <strong>
                    {h.fromStatus ? (
                      <>
                        {STATUS_LABELS[h.fromStatus]} → {STATUS_LABELS[h.toStatus]}
                      </>
                    ) : (
                      "Καταχώρηση βλάβης"
                    )}
                  </strong>
                  <span>{new Date(h.changedAt).toLocaleString("el-GR")}</span>
                  <span className="chip">
                    <UserIcon size={11} /> {h.changedByFullName || h.changedByUsername || "—"}
                  </span>
                </div>
              </li>
            ))}
          </ul>
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
                {editingActionId === a.id ? (
                  <div>
                    <div className="form-row">
                      <label>Περιγραφή</label>
                      <textarea
                        rows={2}
                        value={actionEditForm.description}
                        onChange={(e) =>
                          setActionEditForm({ ...actionEditForm, description: e.target.value })
                        }
                      />
                    </div>
                    <div className="form-row" style={{ maxWidth: 220 }}>
                      <label>Διάρκεια διακοπής (λεπτά)</label>
                      <input
                        type="number"
                        min="0"
                        value={actionEditForm.downtimeMinutes}
                        onChange={(e) =>
                          setActionEditForm({ ...actionEditForm, downtimeMinutes: e.target.value })
                        }
                      />
                    </div>
                    <div className="form-actions">
                      <button className="btn small" onClick={() => handleSaveAction(a.id)}>
                        <Save size={14} /> Αποθήκευση
                      </button>
                      <button
                        className="btn secondary small"
                        onClick={() => setEditingActionId(null)}
                      >
                        Ακύρωση
                      </button>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="tl-head">
                      <strong>{a.technicianUsername}</strong>
                      <span>{new Date(a.actionDate).toLocaleString("el-GR")}</span>
                      {a.downtimeMinutes != null && (
                        <span className="chip">
                          <Clock size={11} /> {a.downtimeMinutes}′ διακοπή
                        </span>
                      )}
                      <span style={{ marginLeft: "auto", display: "flex", gap: "0.2rem" }}>
                        <button
                          className="btn ghost small"
                          onClick={() => startEditingAction(a)}
                          title="Επεξεργασία"
                        >
                          <Pencil size={13} />
                        </button>
                        <button
                          className="btn ghost small"
                          onClick={() => handleDeleteAction(a)}
                          title="Διαγραφή"
                        >
                          <Trash2 size={13} />
                        </button>
                      </span>
                    </div>
                    <div className="tl-body">{a.description}</div>
                  </>
                )}
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
