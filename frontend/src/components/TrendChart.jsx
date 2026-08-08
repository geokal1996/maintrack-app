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
import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import EmptyState from "./EmptyState";

// Ypologizei an i katastasi veltionetai: sygrinei to proto miso tis periodou me to deftero.
function computeTrend(data) {
  if (data.length < 4) return null;
  const mid = Math.floor(data.length / 2);
  const firstHalf = data.slice(0, mid);
  const secondHalf = data.slice(mid);

  const avg = (arr) => arr.reduce((s, d) => s + d.faultCount, 0) / arr.length;
  const before = avg(firstHalf);
  const after = avg(secondHalf);
  if (before === 0) return null;

  const change = Math.round(((after - before) / before) * 100);
  return { change, improving: change < 0 };
}

export default function TrendChart({ data, isDark, title = "Τάση στον χρόνο" }) {
  const gridColor = isDark ? "#1f2b45" : "#e2e8f0";
  const axisColor = isDark ? "#94a3b8" : "#64748b";
  const trend = computeTrend(data || []);

  return (
    <div className="card">
      <div className="card-header" style={{ marginBottom: "0.4rem" }}>
        <h2>
          <TrendingUp size={17} /> {title}
        </h2>
        {trend && (
          <span
            className="badge"
            style={
              trend.improving
                ? { background: "var(--success-soft)", color: "var(--success)" }
                : trend.change === 0
                ? { background: "var(--surface-hover)", color: "var(--text-muted)" }
                : { background: "var(--danger-soft)", color: "var(--danger)" }
            }
          >
            {trend.improving ? (
              <TrendingDown size={12} />
            ) : trend.change === 0 ? (
              <Minus size={12} />
            ) : (
              <TrendingUp size={12} />
            )}
            {trend.change > 0 ? "+" : ""}
            {trend.change}% βλάβες
          </span>
        )}
      </div>
      <p className="muted" style={{ marginTop: 0, marginBottom: "0.9rem" }}>
        Αριθμός βλαβών και χρόνος διακοπής ανά μήνα. Δείχνει αν η κατάσταση βελτιώνεται —
        κάτι που τα Pareto, ως στιγμιότυπο, δεν μπορούν να δείξουν.
      </p>

      {!data || data.length === 0 ? (
        <EmptyState icon={TrendingUp} message="Δεν υπάρχουν αρκετά δεδομένα για γράφημα τάσης." />
      ) : (
        <ResponsiveContainer width="100%" height={300}>
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
            />
            <Legend wrapperStyle={{ fontSize: "0.82rem" }} iconType="circle" />
            <Bar
              yAxisId="left"
              dataKey="faultCount"
              fill="#0891b2"
              name="Αριθμός βλαβών"
              radius={[6, 6, 0, 0]}
            />
            <Line
              yAxisId="right"
              dataKey="downtimeMinutes"
              stroke="#d97706"
              strokeWidth={2.5}
              dot={{ r: 3, fill: "#d97706" }}
              name="Λεπτά διακοπής"
            />
          </ComposedChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}
