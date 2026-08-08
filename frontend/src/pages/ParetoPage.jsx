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
import { BarChart3, RefreshCw, TrendingUp, Info } from "lucide-react";
import { getParetoDashboard, getReliability, getTrend } from "../api/statsApi";
import { useTheme } from "../context/ThemeContext";
import { SkeletonBlock, SkeletonCards } from "../components/Skeleton";
import EmptyState from "../components/EmptyState";
import PeriodFilter, { presetToRange } from "../components/PeriodFilter";
import ReliabilityCards from "../components/ReliabilityCards";
import TrendChart from "../components/TrendChart";

// Ena "generiko" Pareto grafima: mples (i timi) + kokkini grammi (to
// athroistiko %). To idio component to xrisimopoioume kai gia ta 3 diagrammata.
function ParetoChart({ title, subtitle, data, barColor, barName, isDark }) {
  const gridColor = isDark ? "#1f2b45" : "#e2e8f0";
  const axisColor = isDark ? "#94a3b8" : "#64748b";

  // To 80/20: posa stoixeia kalyptoun to 80% tou synolou
  const vitalFew = data.findIndex((d) => d.cumulativePercent >= 80);
  const vitalCount = vitalFew === -1 ? data.length : vitalFew + 1;

  return (
    <div className="card">
      <div className="card-header" style={{ marginBottom: "0.4rem" }}>
        <h2>
          <TrendingUp size={17} /> {title}
        </h2>
        {data.length > 0 && (
          <span className="chip">
            <Info size={12} /> {vitalCount} από {data.length} καλύπτουν το 80%
          </span>
        )}
      </div>
      {subtitle && (
        <p className="muted" style={{ marginTop: 0, marginBottom: "0.9rem" }}>
          {subtitle}
        </p>
      )}

      {data.length === 0 ? (
        <EmptyState icon={BarChart3} message="Δεν υπάρχουν δεδομένα για αυτό το γράφημα στην επιλεγμένη περίοδο." />
      ) : (
        <ResponsiveContainer width="100%" height={330}>
          <ComposedChart data={data} margin={{ top: 10, right: 15, left: -10, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke={gridColor} vertical={false} />
            <XAxis
              dataKey="label"
              tick={{ fill: axisColor, fontSize: 12 }}
              axisLine={{ stroke: gridColor }}
              tickLine={false}
            />
            <YAxis
              yAxisId="left"
              allowDecimals={false}
              tick={{ fill: axisColor, fontSize: 12 }}
              axisLine={false}
              tickLine={false}
            />
            <YAxis
              yAxisId="right"
              orientation="right"
              domain={[0, 100]}
              unit="%"
              tick={{ fill: axisColor, fontSize: 12 }}
              axisLine={false}
              tickLine={false}
            />
            <Tooltip
              cursor={{ fill: isDark ? "rgba(255,255,255,0.04)" : "rgba(15,23,42,0.04)" }}
              contentStyle={{
                background: "var(--surface)",
                border: "1px solid var(--border)",
                borderRadius: 10,
                color: "var(--text)",
                fontSize: "0.85rem",
              }}
              formatter={(value, name) =>
                name === "Αθροιστικό %" ? [`${value}%`, name] : [value, name]
              }
            />
            <Legend wrapperStyle={{ fontSize: "0.82rem" }} iconType="circle" />
            <Bar yAxisId="left" dataKey="value" fill={barColor} name={barName} radius={[6, 6, 0, 0]} />
            <Line
              yAxisId="right"
              dataKey="cumulativePercent"
              stroke="#dc2626"
              strokeWidth={2.5}
              dot={{ r: 3, fill: "#dc2626" }}
              activeDot={{ r: 5 }}
              name="Αθροιστικό %"
            />
          </ComposedChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

export default function ParetoPage() {
  const { isDark } = useTheme();
  const [data, setData] = useState(null);
  const [reliability, setReliability] = useState(null);
  const [trend, setTrend] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [preset, setPreset] = useState("all");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [area, setArea] = useState("");

  function loadData() {
    setLoading(true);
    setError("");
    const filters = { from, to, area };

    Promise.all([getParetoDashboard(filters), getReliability(filters), getTrend(filters)])
      .then(([pareto, rel, tr]) => {
        setData(pareto);
        setReliability(rel);
        setTrend(tr);
      })
      .catch(() => setError("Δεν ήταν δυνατή η φόρτωση των στατιστικών"))
      .finally(() => setLoading(false));
  }

  useEffect(loadData, [from, to, area]);

  function handlePresetChange(next) {
    setPreset(next);
    if (next !== "custom") {
      const range = presetToRange(next);
      setFrom(range.from);
      setTo(range.to);
    }
  }

  return (
    <div>
      <div className="card-header" style={{ marginBottom: "1.25rem" }}>
        <h1 className="page-title" style={{ margin: 0 }}>
          <BarChart3 size={22} />
          Ανάλυση
          <span className="sub">Ζωντανά από τη βάση</span>
        </h1>
        <button className="btn secondary" onClick={loadData} disabled={loading}>
          <RefreshCw size={15} />
          Ανανέωση
        </button>
      </div>

      <PeriodFilter
        preset={preset}
        onPresetChange={handlePresetChange}
        from={from}
        to={to}
        onFromChange={setFrom}
        onToChange={setTo}
        area={area}
        onAreaChange={setArea}
        availableAreas={data?.availableAreas || []}
        totalFaults={data?.totalFaults}
      />

      {error && <p className="error-text">{error}</p>}

      {loading ? (
        <>
          <SkeletonCards count={3} />
          <div style={{ height: "1.25rem" }} />
          <SkeletonBlock height={300} />
          <div style={{ height: "1.25rem" }} />
          <SkeletonBlock height={330} />
        </>
      ) : (
        data && (
          <>
            <ReliabilityCards data={reliability} />

            <div style={{ height: "1.25rem" }} />
            <TrendChart data={trend} isDark={isDark} />

            <ParetoChart
              title="Χρόνος Διακοπής ανά Μηχανή"
              subtitle="Ποιες μηχανές ευθύνονται για τον περισσότερο χαμένο χρόνο παραγωγής (σε λεπτά)."
              data={data.downtimeByMachine}
              barColor="#4f46e5"
              barName="Λεπτά διακοπής"
              isDark={isDark}
            />
            <ParetoChart
              title="Αριθμός Βλαβών ανά Μηχανή"
              subtitle="Ποιες μηχανές παρουσιάζουν τις περισσότερες βλάβες, ανεξάρτητα από τη διάρκειά τους."
              data={data.faultsByMachine}
              barColor="#0891b2"
              barName="Αριθμός βλαβών"
              isDark={isDark}
            />
            <ParetoChart
              title="Βλάβες ανά Σοβαρότητα"
              subtitle="Κατανομή του συνόλου των βλαβών ανά επίπεδο σοβαρότητας."
              data={data.faultsBySeverity}
              barColor="#7c3aed"
              barName="Αριθμός βλαβών"
              isDark={isDark}
            />
          </>
        )
      )}
    </div>
  );
}
