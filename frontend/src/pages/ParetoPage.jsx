import { useEffect, useState } from "react";
import {
  ComposedChart,
  Bar,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { getParetoDashboard } from "../api/statsApi";

// Ena "generiko" Pareto grafima: mples mples (i timi) + kokkini grammi (to
// athroistiko %). To idio component to xrisimopoioume kai gia ta 3 diagrammata,
// allazontas mono ta dedomena/xromata/titlo.
function ParetoChart({ title, data, barColor, barName }) {
  return (
    <div className="card">
      <h2>{title}</h2>
      {data.length === 0 ? (
        <p className="muted">Δεν υπάρχουν ακόμα δεδομένα για αυτό το γράφημα.</p>
      ) : (
        <ResponsiveContainer width="100%" height={320}>
          <ComposedChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="label" />
            <YAxis yAxisId="left" allowDecimals={false} />
            <YAxis yAxisId="right" orientation="right" domain={[0, 100]} unit="%" />
            <Tooltip />
            <Legend />
            <Bar yAxisId="left" dataKey="value" fill={barColor} name={barName} />
            <Line
              yAxisId="right"
              dataKey="cumulativePercent"
              stroke="#dc2626"
              strokeWidth={2}
              dot={{ r: 3 }}
              name="Αθροιστικό %"
            />
          </ComposedChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

export default function ParetoPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  function loadData() {
    setLoading(true);
    setError("");
    getParetoDashboard()
      .then(setData)
      .catch(() => setError("Δεν ήταν δυνατή η φόρτωση των στατιστικών"))
      .finally(() => setLoading(false));
  }

  useEffect(loadData, []);

  return (
    <div>
      <div className="card" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h2 style={{ margin: 0 }}>Pareto Ανάλυση</h2>
        <button className="btn secondary" onClick={loadData}>Ανανέωση</button>
      </div>

      {loading && <p className="muted">Φόρτωση...</p>}
      {error && <p className="error-text">{error}</p>}

      {data && (
        <>
          <ParetoChart
            title="Pareto: Χρόνος Διακοπής ανά Μηχανή (λεπτά)"
            data={data.downtimeByMachine}
            barColor="#2563eb"
            barName="Λεπτά διακοπής"
          />
          <ParetoChart
            title="Pareto: Αριθμός Βλαβών ανά Μηχανή"
            data={data.faultsByMachine}
            barColor="#0891b2"
            barName="Αριθμός βλαβών"
          />
          <ParetoChart
            title="Pareto: Βλάβες ανά Σοβαρότητα"
            data={data.faultsBySeverity}
            barColor="#7c3aed"
            barName="Αριθμός βλαβών"
          />
        </>
      )}
    </div>
  );
}
