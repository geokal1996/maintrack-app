import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  ArrowLeft,
  Cog,
  MapPin,
  AlertTriangle,
  ArrowRight,
  CheckCircle2,
  Clock,
} from "lucide-react";
import { getMachine, getMachineFaults } from "../api/machinesApi";
import { getReliability, getTrend } from "../api/statsApi";
import { useTheme } from "../context/ThemeContext";
import { SkeletonBlock, SkeletonCards } from "../components/Skeleton";
import EmptyState from "../components/EmptyState";
import ReliabilityCards from "../components/ReliabilityCards";
import TrendChart from "../components/TrendChart";

const STATUS_LABELS = {
  OPERATIONAL: "Σε λειτουργία",
  DOWN: "Εκτός λειτουργίας",
  UNDER_MAINTENANCE: "Σε συντήρηση",
};
const FAULT_STATUS_LABELS = {
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

// I selida tis MIHANIS. Se ena systima sintirisis i mihani einai i kentriki
// ontotita - edo mazevontai to istoriko tis, oi deiktes tis kai i tasi tis.
export default function MachineDetailPage() {
  const { id } = useParams();
  const { isDark } = useTheme();

  const [machine, setMachine] = useState(null);
  const [faults, setFaults] = useState([]);
  const [reliability, setReliability] = useState(null);
  const [trend, setTrend] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      getMachine(id),
      getMachineFaults(id),
      getReliability({ machineId: id }),
      getTrend({ machineId: id }),
    ])
      .then(([m, f, r, t]) => {
        setMachine(m);
        setFaults(f);
        setReliability(r);
        setTrend(t);
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div>
        <SkeletonBlock height={120} />
        <div style={{ height: "1.25rem" }} />
        <SkeletonCards count={3} />
      </div>
    );
  }

  if (!machine) {
    return <EmptyState icon={Cog} message="Δεν βρέθηκε η μηχανή." />;
  }

  const openFaults = faults.filter((f) => f.status === "OPEN" || f.status === "IN_PROGRESS");

  return (
    <div>
      <p style={{ marginTop: 0 }}>
        <Link to="/machines" style={{ display: "inline-flex", alignItems: "center", gap: 5 }}>
          <ArrowLeft size={15} /> Πίσω στις μηχανές
        </Link>
      </p>

      <div className="card">
        <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap", marginBottom: "0.6rem" }}>
          <span className={`badge dot status-${machine.status}`}>{STATUS_LABELS[machine.status]}</span>
          {machine.area && (
            <span className="chip">
              <MapPin size={12} /> {machine.area}
            </span>
          )}
        </div>

        <h1 style={{ fontSize: "1.45rem", margin: "0 0 0.25rem", letterSpacing: "-0.025em" }}>
          {machine.name}
        </h1>
        <div className="mono muted" style={{ fontSize: "0.95rem" }}>
          {machine.code}
        </div>

        <div className="divider" />

        <div style={{ display: "flex", gap: "1.5rem", flexWrap: "wrap" }}>
          <div>
            <div style={{ fontSize: "1.5rem", fontWeight: 700 }}>{faults.length}</div>
            <div className="muted" style={{ fontSize: "0.8rem" }}>
              συνολικές βλάβες
            </div>
          </div>
          <div>
            <div
              style={{
                fontSize: "1.5rem",
                fontWeight: 700,
                color: openFaults.length > 0 ? "var(--danger)" : "var(--success)",
              }}
            >
              {openFaults.length}
            </div>
            <div className="muted" style={{ fontSize: "0.8rem" }}>
              ανοιχτές τώρα
            </div>
          </div>
          <div>
            <div style={{ fontSize: "1.5rem", fontWeight: 700 }}>
              {reliability?.totalDowntimeMinutes ?? 0}′
            </div>
            <div className="muted" style={{ fontSize: "0.8rem" }}>
              συνολικός χρόνος διακοπής
            </div>
          </div>
        </div>
      </div>

      <h2 style={{ fontSize: "1.05rem", margin: "1.5rem 0 0.75rem" }}>Δείκτες αξιοπιστίας</h2>
      <ReliabilityCards data={reliability} />

      <div style={{ height: "1.25rem" }} />
      <TrendChart data={trend} isDark={isDark} title="Τάση αυτής της μηχανής" />

      <div className="card">
        <h2>
          <AlertTriangle size={17} /> Ιστορικό βλαβών
        </h2>

        {faults.length === 0 ? (
          <EmptyState
            icon={CheckCircle2}
            message="Αυτή η μηχανή δεν έχει καταγεγραμμένες βλάβες."
          />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Τίτλος</th>
                  <th>Σοβαρότητα</th>
                  <th>Κατάσταση</th>
                  <th>Ημερομηνία</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {faults.map((f) => (
                  <tr key={f.id}>
                    <td className={`sev-cell sev-${f.severity}`}>{f.title}</td>
                    <td>
                      <span className={`badge severity-${f.severity}`}>
                        {SEVERITY_LABELS[f.severity]}
                      </span>
                    </td>
                    <td>
                      <span className={`badge dot status-${f.status}`}>
                        {FAULT_STATUS_LABELS[f.status]}
                      </span>
                    </td>
                    <td className="muted">
                      <Clock size={12} style={{ verticalAlign: -2, marginRight: 4 }} />
                      {new Date(f.createdAt).toLocaleDateString("el-GR")}
                    </td>
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
        )}
      </div>
    </div>
  );
}
