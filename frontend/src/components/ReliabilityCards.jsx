import { Timer, Wrench, Activity, HelpCircle } from "lucide-react";

// Formatarei ores se anagnosimi morfi: 36.5 -> "1 μέρα 12,5 ώρες"
function formatHours(hours) {
  if (hours == null) return "—";
  if (hours < 1) return `${Math.round(hours * 60)} λεπτά`;
  if (hours < 48) return `${hours.toFixed(1).replace(".", ",")} ώρες`;
  const days = Math.floor(hours / 24);
  const rest = hours - days * 24;
  return rest < 1 ? `${days} μέρες` : `${days} μέρες ${rest.toFixed(0)} ώρες`;
}

function MetricCard({ icon: Icon, label, value, hint, accent, accentSoft, good }) {
  return (
    <div className="card stat-card" style={{ "--accent": accent, "--accent-soft": accentSoft }}>
      <div className="stat-icon">
        <Icon size={22} />
      </div>
      <div style={{ minWidth: 0 }}>
        <div className="value" style={{ fontSize: "1.5rem" }}>
          {value}
        </div>
        <div className="label" style={{ display: "flex", alignItems: "center", gap: "0.3rem" }}>
          {label}
          {hint && (
            <span title={hint} style={{ display: "inline-flex", cursor: "help", opacity: 0.7 }}>
              <HelpCircle size={12} />
            </span>
          )}
        </div>
        {good && (
          <div style={{ fontSize: "0.72rem", color: "var(--text-muted)", marginTop: "0.15rem" }}>
            {good}
          </div>
        )}
      </div>
    </div>
  );
}

// Oi treis deiktes axiopistias. Einai to standard lexilogio tis viomixanikis
// sintirisis - opoios douleuei ston xoro tous anagnorizei amesa.
export default function ReliabilityCards({ data }) {
  if (!data) return null;

  return (
    <div className="grid-3">
      <MetricCard
        icon={Timer}
        label="MTBF"
        value={formatHours(data.mtbfHours)}
        hint="Mean Time Between Failures — ο μέσος χρόνος που μεσολαβεί ανάμεσα σε δύο βλάβες"
        good="μεγαλύτερο = καλύτερα"
        accent="#4f46e5"
        accentSoft="var(--primary-soft)"
      />
      <MetricCard
        icon={Wrench}
        label="MTTR"
        value={formatHours(data.mttrHours)}
        hint="Mean Time To Repair — ο μέσος χρόνος που χρειάζεται μια επισκευή"
        good="μικρότερο = καλύτερα"
        accent="#d97706"
        accentSoft="var(--warning-soft)"
      />
      <MetricCard
        icon={Activity}
        label="Διαθεσιμότητα"
        value={data.availabilityPercent != null ? `${data.availabilityPercent}%`.replace(".", ",") : "—"}
        hint="MTBF / (MTBF + MTTR) — το ποσοστό του χρόνου που ο εξοπλισμός ήταν διαθέσιμος"
        good={
          data.periodDays
            ? `σε ${data.periodDays} ${data.periodDays === 1 ? "μέρα" : "μέρες"}`
            : undefined
        }
        accent="#16a34a"
        accentSoft="var(--success-soft)"
      />
    </div>
  );
}
