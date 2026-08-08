import { CalendarRange, MapPin } from "lucide-react";

// Etoimes epiloges periodou. To "value" einai poses meres piso apo simera,
// i null gia "ola ta dedomena".
export const PRESETS = [
  { key: "30d", label: "Τελευταίος μήνας", days: 30 },
  { key: "90d", label: "Τελευταίο τρίμηνο", days: 90 },
  { key: "365d", label: "Τελευταίο έτος", days: 365 },
  { key: "all", label: "Όλα", days: null },
];

// Ypologizei tis imerominies apo mia etoimi epilogi
export function presetToRange(key) {
  const preset = PRESETS.find((p) => p.key === key);
  if (!preset || preset.days === null) {
    return { from: "", to: "" };
  }
  const to = new Date();
  const from = new Date();
  from.setDate(from.getDate() - preset.days);
  return { from: toIso(from), to: toIso(to) };
}

function toIso(date) {
  // "YYYY-MM-DD" - i morfi pou perimenei to backend
  return date.toISOString().slice(0, 10);
}

// Filtro periodou + perioxis, koino gia Pareto kai Dashboard.
export default function PeriodFilter({
  preset,
  onPresetChange,
  from,
  to,
  onFromChange,
  onToChange,
  area,
  onAreaChange,
  availableAreas = [],
  totalFaults,
}) {
  return (
    <div className="card" style={{ paddingTop: "1rem", paddingBottom: "1rem" }}>
      <div className="filters-row" style={{ marginBottom: 0, alignItems: "flex-end" }}>
        <div className="form-row" style={{ marginBottom: 0, minWidth: 190 }}>
          <label>
            <CalendarRange size={13} style={{ verticalAlign: -2, marginRight: 4 }} />
            Περίοδος
          </label>
          <select value={preset} onChange={(e) => onPresetChange(e.target.value)}>
            {PRESETS.map((p) => (
              <option key={p.key} value={p.key}>
                {p.label}
              </option>
            ))}
            <option value="custom">Προσαρμοσμένη...</option>
          </select>
        </div>

        {preset === "custom" && (
          <>
            <div className="form-row" style={{ marginBottom: 0 }}>
              <label>Από</label>
              <input type="date" value={from} onChange={(e) => onFromChange(e.target.value)} />
            </div>
            <div className="form-row" style={{ marginBottom: 0 }}>
              <label>Έως</label>
              <input type="date" value={to} onChange={(e) => onToChange(e.target.value)} />
            </div>
          </>
        )}

        {availableAreas.length > 0 && (
          <div className="form-row" style={{ marginBottom: 0, minWidth: 190 }}>
            <label>
              <MapPin size={13} style={{ verticalAlign: -2, marginRight: 4 }} />
              Περιοχή
            </label>
            <select value={area} onChange={(e) => onAreaChange(e.target.value)}>
              <option value="">Όλες οι περιοχές</option>
              {availableAreas.map((a) => (
                <option key={a} value={a}>
                  {a}
                </option>
              ))}
            </select>
          </div>
        )}

        {totalFaults != null && (
          <span className="chip" style={{ marginBottom: "0.55rem" }}>
            {totalFaults} {totalFaults === 1 ? "βλάβη" : "βλάβες"} στον υπολογισμό
          </span>
        )}
      </div>
    </div>
  );
}
