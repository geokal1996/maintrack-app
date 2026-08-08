import { AlertCircle, Wand2 } from "lucide-react";

// Ta pedia tis efarmogis mas, me ti seira pou theloume na ta dei o xristis.
// To "required" simainei oti xoris auto den mporei na ginei i eisagogi.
export const MAPPABLE_FIELDS = [
  { key: "machineCode", label: "Κωδικός μηχανής", required: true, hint: "π.χ. CRL1 ή 6520-HRL-TIPP" },
  { key: "title", label: "Τίτλος βλάβης", required: true, hint: "σύντομη περιγραφή του προβλήματος" },
  { key: "date", label: "Ημερομηνία βλάβης", required: false, hint: "χωρίς αυτή, όλες οι εγγραφές παίρνουν τη σημερινή ημερομηνία" },
  { key: "externalRef", label: "Μοναδικός κωδικός", required: false, hint: "αποτρέπει διπλοεγγραφές σε δεύτερο ανέβασμα" },
  { key: "machineName", label: "Όνομα μηχανής", required: false, hint: "χρησιμοποιείται αν η μηχανή δημιουργηθεί αυτόματα" },
  { key: "description", label: "Αναλυτική περιγραφή", required: false, hint: "" },
  { key: "severity", label: "Σοβαρότητα", required: false, hint: "LOW/MEDIUM/HIGH/CRITICAL ή 1/2/3/4" },
  { key: "status", label: "Κατάσταση", required: false, hint: "OPEN/IN_PROGRESS/RESOLVED/CLOSED" },
  { key: "technician", label: "Τεχνικός", required: false, hint: "username — αν δεν βρεθεί, μπαίνεις εσύ" },
  { key: "action", label: "Ενέργεια συντήρησης", required: false, hint: "" },
  { key: "downtime", label: "Χρόνος διακοπής", required: false, hint: "αριθμός — δήλωσε παρακάτω αν είναι λεπτά ή ώρες" },
];

// Othoni antistoixisis: aristera ta DIKA MAS pedia, dexia dropdown me TIS DIKES TOU stiles.
export default function ColumnMapper({ preview, mapping, onChange, autoDetected }) {
  const { headers, sampleRows } = preview;

  function setField(key, value) {
    onChange({ ...mapping, [key]: value === "" ? null : Number(value) });
  }

  // Poies stiles exoun idi xrisimopoiithei - gia na to deixnoume ston xristi
  const usedColumns = new Set(
    MAPPABLE_FIELDS.map((f) => mapping[f.key]).filter((v) => v !== null && v !== undefined)
  );

  const missingRequired = MAPPABLE_FIELDS.filter((f) => f.required && mapping[f.key] == null);

  return (
    <div>
      <div className="divider" />

      <div className="card-header" style={{ marginBottom: "0.5rem" }}>
        <h3 style={{ fontSize: "0.98rem", margin: 0, display: "flex", alignItems: "center", gap: "0.45rem" }}>
          <Wand2 size={16} /> Αντιστοίχιση στηλών
        </h3>
        <span className="chip">
          {headers.length} στήλες · {preview.totalRows} γραμμές
        </span>
      </div>

      <p className="muted" style={{ marginTop: 0 }}>
        {autoDetected
          ? "Η μορφή αναγνωρίστηκε αυτόματα, αλλά μπορείς να αλλάξεις την αντιστοίχιση αν κάτι δεν είναι σωστό."
          : "Δεν αναγνώρισα αυτή τη μορφή, οπότε μάντεψα τι είναι κάθε στήλη από το όνομά της. Έλεγξε τις επιλογές και διόρθωσε ό,τι χρειάζεται."}
      </p>

      {/* Deigma apo to arxeio, gia na thymatai o xristis ti periexei kathe stili */}
      {sampleRows.length > 0 && (
        <div className="table-wrap" style={{ marginBottom: "1.1rem" }}>
          <table style={{ fontSize: "0.8rem" }}>
            <thead>
              <tr>
                {headers.map((h, i) => (
                  <th key={i} style={{ color: usedColumns.has(i) ? "var(--primary)" : undefined }}>
                    {h || `(στήλη ${i + 1})`}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {sampleRows.map((row, r) => (
                <tr key={r}>
                  {row.map((cell, c) => (
                    <td key={c} style={{ whiteSpace: "nowrap", maxWidth: 190, overflow: "hidden", textOverflow: "ellipsis" }}>
                      {cell}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="form-grid">
        {MAPPABLE_FIELDS.map((field) => (
          <div className="form-row" key={field.key}>
            <label>
              {field.label}
              {field.required && <span style={{ color: "var(--danger)" }}> *</span>}
            </label>
            <select
              value={mapping[field.key] ?? ""}
              onChange={(e) => setField(field.key, e.target.value)}
              style={
                field.required && mapping[field.key] == null
                  ? { borderColor: "var(--danger)" }
                  : undefined
              }
            >
              <option value="">— δεν υπάρχει —</option>
              {headers.map((h, i) => (
                <option key={i} value={i}>
                  {h || `(στήλη ${i + 1})`}
                </option>
              ))}
            </select>
            {field.hint && (
              <span className="muted" style={{ fontSize: "0.76rem" }}>
                {field.hint}
              </span>
            )}
          </div>
        ))}
      </div>

      <div className="filters-row" style={{ marginTop: "0.5rem", marginBottom: 0 }}>
        <div className="form-row" style={{ marginBottom: 0, minWidth: 210 }}>
          <label>Ο χρόνος διακοπής είναι σε</label>
          <select
            value={mapping.downtimeUnit || "MINUTES"}
            onChange={(e) => onChange({ ...mapping, downtimeUnit: e.target.value })}
            disabled={mapping.downtime == null}
          >
            <option value="MINUTES">Λεπτά</option>
            <option value="HOURS">Ώρες</option>
          </select>
        </div>

        <label
          style={{
            display: "flex",
            alignItems: "center",
            gap: "0.5rem",
            fontSize: "0.88rem",
            paddingBottom: "0.55rem",
          }}
        >
          <input
            type="checkbox"
            checked={mapping.createMissingMachines !== false}
            onChange={(e) => onChange({ ...mapping, createMissingMachines: e.target.checked })}
          />
          Δημιουργία μηχανών που δεν υπάρχουν
        </label>
      </div>

      {missingRequired.length > 0 && (
        <p className="error-text">
          <AlertCircle size={15} />
          Πρέπει να ορίσεις: {missingRequired.map((f) => f.label).join(", ")}
        </p>
      )}
    </div>
  );
}
