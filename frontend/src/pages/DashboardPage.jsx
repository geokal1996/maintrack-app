import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
  Legend,
} from "recharts";
import {
  AlertTriangle,
  CircleSlash,
  Cog,
  CheckCircle2,
  ArrowRight,
  LayoutDashboard,
} from "lucide-react";
import { getFaults } from "../api/faultsApi";
import { getMachines } from "../api/machinesApi";
import { SkeletonCards, SkeletonTable } from "../components/Skeleton";
import EmptyState from "../components/EmptyState";

const STATUS_META = {
  OPERATIONAL: { label: "Σε λειτουργία", color: "#16a34a" },
  UNDER_MAINTENANCE: { label: "Σε συντήρηση", color: "#d97706" },
  DOWN: { label: "Εκτός λειτουργίας", color: "#dc2626" },
};

function StatCard({ icon: Icon, value, label, accent, accentSoft }) {
  return (
    <div className="card stat-card" style={{ "--accent": accent, "--accent-soft": accentSoft }}>
      <div className="stat-icon">
        <Icon size={22} />
      </div>
      <div>
        <div className="value">{value}</div>
        <div className="label">{label}</div>
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const [faults, setFaults] = useState([]);
  const [machines, setMachines] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getFaults({ status: "OPEN" }), getMachines()])
      .then(([openFaults, allMachines]) => {
        setFaults(openFaults);
        setMachines(allMachines);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div>
        <h1 className="page-title">
          <LayoutDashboard size={22} /> Dashboard
        </h1>
        <SkeletonCards count={3} />
        <div className="card" style={{ marginTop: "1.25rem" }}>
          <SkeletonTable rows={4} cols={5} />
        </div>
      </div>
    );
  }

  const downMachines = machines.filter((m) => m.status === "DOWN");
  const allHealthy = downMachines.length === 0;

  // Katanomi mihanon ana katastasi, gia to donut chart
  const distribution = Object.keys(STATUS_META)
    .map((key) => ({
      name: STATUS_META[key].label,
      value: machines.filter((m) => m.status === key).length,
      color: STATUS_META[key].color,
    }))
    .filter((d) => d.value > 0);

  return (
    <div>
      <h1 className="page-title">
        <LayoutDashboard size={22} />
        Dashboard
        <span className="sub">Επισκόπηση κατάστασης</span>
      </h1>

      <div className="grid-3">
        <StatCard
          icon={AlertTriangle}
          value={faults.length}
          label="Ανοιχτές βλάβες"
          accent={faults.length > 0 ? "#d97706" : "#16a34a"}
          accentSoft={faults.length > 0 ? "var(--warning-soft)" : "var(--success-soft)"}
        />
        <StatCard
          icon={allHealthy ? CheckCircle2 : CircleSlash}
          value={downMachines.length}
          label="Μηχανές εκτός λειτουργίας"
          accent={allHealthy ? "#16a34a" : "#dc2626"}
          accentSoft={allHealthy ? "var(--success-soft)" : "var(--danger-soft)"}
        />
        <StatCard
          icon={Cog}
          value={machines.length}
          label="Σύνολο μηχανών"
          accent="#4f46e5"
          accentSoft="var(--primary-soft)"
        />
      </div>

      <div className="grid-2" style={{ marginTop: "1.25rem" }}>
        <div className="card" style={{ marginBottom: 0 }}>
          <h2>
            <Cog size={17} /> Κατανομή μηχανών
          </h2>
          {distribution.length === 0 ? (
            <EmptyState icon={Cog} message="Δεν υπάρχουν καταχωρημένες μηχανές." />
          ) : (
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie
                  data={distribution}
                  dataKey="value"
                  nameKey="name"
                  innerRadius={58}
                  outerRadius={90}
                  paddingAngle={3}
                  stroke="none"
                >
                  {distribution.map((d) => (
                    <Cell key={d.name} fill={d.color} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    background: "var(--surface)",
                    border: "1px solid var(--border)",
                    borderRadius: 10,
                    color: "var(--text)",
                  }}
                />
                <Legend iconType="circle" wrapperStyle={{ fontSize: "0.82rem" }} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="card" style={{ marginBottom: 0 }}>
          <h2>
            <CircleSlash size={17} /> Μηχανές εκτός λειτουργίας
          </h2>
          {allHealthy ? (
            <EmptyState
              icon={CheckCircle2}
              message="Όλες οι μηχανές λειτουργούν κανονικά."
              hint="Καμία ενεργή διακοπή αυτή τη στιγμή."
            />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Κωδικός</th>
                    <th>Όνομα</th>
                    <th>Περιοχή</th>
                  </tr>
                </thead>
                <tbody>
                  {downMachines.map((m) => (
                    <tr key={m.id}>
                      <td className="mono">{m.code}</td>
                      <td>{m.name}</td>
                      <td className="muted">{m.area}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      <div className="card" style={{ marginTop: "1.25rem" }}>
        <div className="card-header">
          <h2>
            <AlertTriangle size={17} /> Ανοιχτές βλάβες
          </h2>
          <Link to="/faults" style={{ fontSize: "0.85rem", display: "inline-flex", alignItems: "center", gap: 4 }}>
            Όλες οι βλάβες <ArrowRight size={14} />
          </Link>
        </div>

        {faults.length === 0 ? (
          <EmptyState
            icon={CheckCircle2}
            message="Δεν υπάρχουν ανοιχτές βλάβες αυτή τη στιγμή."
            hint="Ωραία νέα — όλα υπό έλεγχο."
          />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Μηχανή</th>
                  <th>Τίτλος</th>
                  <th>Σοβαρότητα</th>
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
                      <span className={`badge severity-${f.severity}`}>{f.severity}</span>
                    </td>
                    <td className="muted">{new Date(f.createdAt).toLocaleString("el-GR")}</td>
                    <td>
                      <Link to={`/faults/${f.id}`} style={{ display: "inline-flex", alignItems: "center", gap: 4 }}>
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
