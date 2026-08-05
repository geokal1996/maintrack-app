import { useRef, useState } from "react";
import toast from "react-hot-toast";
import {
  FileSpreadsheet,
  Upload,
  Download,
  CheckCircle2,
  SkipForward,
  XCircle,
  HelpCircle,
  ChevronDown,
  ChevronRight,
  Loader2,
  Sliders,
  X,
} from "lucide-react";
import {
  inspectExcel,
  importFaultsFromExcel,
  downloadImportTemplate,
} from "../api/importApi";
import ColumnMapper, { MAPPABLE_FIELDS } from "./ColumnMapper";

// Ti stiles perimenei i kathe gnosti morfi. Emfanizetai sto ptyssomeno panel odigion.
const TEMPLATE_COLUMNS = [
  { name: "Αρ. Γνωστοποίησης", required: false, note: "μοναδικός κωδικός — αποτρέπει διπλοεγγραφές σε δεύτερο ανέβασμα" },
  { name: "Κωδικός Μηχανής", required: true, note: "πρέπει να αντιστοιχεί σε υπάρχουσα μηχανή" },
  { name: "Τίτλος Βλάβης", required: true, note: "σύντομη περιγραφή του προβλήματος" },
  { name: "Περιγραφή", required: false, note: "αναλυτικό κείμενο" },
  { name: "Σοβαρότητα", required: false, note: "LOW, MEDIUM, HIGH ή CRITICAL (κενό → MEDIUM)" },
  { name: "Κατάσταση", required: false, note: "OPEN, IN_PROGRESS, RESOLVED ή CLOSED (κενό → OPEN)" },
  { name: "Τεχνικός (username)", required: false, note: "κενό → ο συνδεδεμένος χρήστης" },
  { name: "Ενέργεια Συντήρησης", required: false, note: "τι έγινε για την αποκατάσταση" },
  { name: "Χρόνος Διακοπής (λεπτά)", required: false, note: "αριθμός σε λεπτά" },
];

const SAP_COLUMNS = [
  { name: "Notification", required: true, note: "αριθμός γνωστοποίησης → μοναδικό αναγνωριστικό" },
  { name: "FLoc. affected", required: true, note: "λειτουργική περιοχή → κωδικός μηχανής (εναλλακτικά «Equipment»)" },
  { name: "Description", required: true, note: "1η εμφάνιση → τίτλος βλάβης" },
  { name: "Description", required: false, note: "2η εμφάνιση → όνομα μηχανής" },
  { name: "Priority", required: false, note: "1→Κρίσιμη, 2→Υψηλή, 3→Μεσαία, 4→Χαμηλή" },
  { name: "System status", required: false, note: "NOCO→Έκλεισε, NOPR→Σε εξέλιξη, OSNO→Ανοιχτή, DLFL→παράλειψη" },
  { name: "Malfunct. start + Start Malfn (T)", required: false, note: "έναρξη βλάβης (ημερομηνία + ώρα)" },
  { name: "Malfunct.end + Malfunction end", required: false, note: "λήξη βλάβης — από εδώ υπολογίζεται ο χρόνος διακοπής" },
  { name: "Completn date", required: false, note: "ημερομηνία επίλυσης" },
  { name: "Reported by / Created By", required: false, note: "τεχνικός" },
];

const FORMAT_LABELS = {
  MAINTRACK_TEMPLATE: "Υπόδειγμα Maintrack",
  SAP_IW29: "Export από SAP (IW29)",
  UNKNOWN: "Άγνωστη μορφή",
};

function ColumnTable({ title, subtitle, columns }) {
  return (
    <div style={{ marginTop: "1rem" }}>
      <h3 style={{ fontSize: "0.92rem", margin: "0 0 0.15rem" }}>{title}</h3>
      <p className="muted" style={{ margin: "0 0 0.6rem", fontSize: "0.82rem" }}>{subtitle}</p>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th style={{ width: "30%" }}>Στήλη</th>
              <th style={{ width: 110 }}>Υποχρεωτική</th>
              <th>Σημείωση</th>
            </tr>
          </thead>
          <tbody>
            {columns.map((c, i) => (
              <tr key={`${c.name}-${i}`}>
                <td className="mono">{c.name}</td>
                <td>
                  {c.required ? (
                    <span className="badge" style={{ background: "var(--danger-soft)", color: "var(--danger)" }}>Ναι</span>
                  ) : (
                    <span className="badge" style={{ background: "var(--surface-hover)", color: "var(--text-muted)" }}>Όχι</span>
                  )}
                </td>
                <td className="muted">{c.note}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// Panel gia mazikí eisagogí vlavón apo arxeio Excel.
// Emfanizetai mono se SUPERVISOR/MANAGER (to elegxei i selida pou to kalei).
export default function ExcelImportPanel({ onImported }) {
  const [file, setFile] = useState(null);
  const [inspecting, setInspecting] = useState(false);
  const [preview, setPreview] = useState(null);
  const [mapping, setMapping] = useState(null);
  const [showMapper, setShowMapper] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [showHelp, setShowHelp] = useState(false);
  const [errorText, setErrorText] = useState("");
  const fileInputRef = useRef(null);

  function reset() {
    setFile(null);
    setPreview(null);
    setMapping(null);
    setShowMapper(false);
    setErrorText("");
    if (fileInputRef.current) fileInputRef.current.value = "";
  }

  // Molis dialexei arxeio, to steloume gia "anagnorisi" - xoris na apothikefsoume tipota.
  async function handleFileChosen(chosen) {
    setFile(chosen);
    setPreview(null);
    setMapping(null);
    setShowMapper(false);
    setResult(null);
    setErrorText("");
    if (!chosen) return;

    setInspecting(true);
    try {
      const data = await inspectExcel(chosen);
      setPreview(data);
      setMapping(data.suggestedMapping || {});
      // An den anagnorisame ti morfi, anoigoume amesos tin antistoixisi -
      // einai o monos tropos na proxorisei o xristis.
      if (data.detectedFormat === "UNKNOWN") {
        setShowMapper(true);
      }
    } catch (err) {
      setErrorText(err.response?.data?.message || "Δεν ήταν δυνατή η ανάγνωση του αρχείου");
    } finally {
      setInspecting(false);
    }
  }

  async function handleUpload() {
    if (!file) return;
    setUploading(true);
    setResult(null);
    setErrorText("");
    try {
      // Stelnoume tin antistoixisi MONO an o xristis anoixe to panel -
      // alliws afinoume to backend na anagnorisei moni tou ti morfi.
      const data = await importFaultsFromExcel(file, showMapper ? mapping : null);
      setResult(data);
      reset();

      if (data.imported > 0) {
        toast.success(`Εισήχθησαν ${data.imported} βλάβες`);
      } else if (data.failed > 0) {
        toast.error("Καμία γραμμή δεν εισήχθη — δες τα σφάλματα παρακάτω");
      } else {
        toast("Όλες οι γραμμές υπήρχαν ήδη", { icon: "ℹ️" });
      }

      if (onImported) onImported();
    } catch (err) {
      const message = err.response?.data?.message || "Δεν ήταν δυνατή η εισαγωγή του αρχείου";
      toast.error("Το αρχείο δεν έγινε δεκτό");
      setErrorText(message);
      setShowHelp(true);
    } finally {
      setUploading(false);
    }
  }

  async function handleDownloadTemplate() {
    try {
      await downloadImportTemplate();
      toast.success("Το υπόδειγμα κατέβηκε");
    } catch {
      toast.error("Δεν ήταν δυνατή η λήψη του υποδείγματος");
    }
  }

  const missingRequired =
    showMapper && mapping
      ? MAPPABLE_FIELDS.filter((f) => f.required && mapping[f.key] == null)
      : [];
  const canImport = file && !uploading && !inspecting && missingRequired.length === 0;

  const stats = result
    ? [
        { icon: CheckCircle2, value: result.imported, label: "Εισήχθησαν", color: "var(--success)", soft: "var(--success-soft)" },
        { icon: SkipForward, value: result.skipped, label: "Υπήρχαν ήδη", color: "var(--warning)", soft: "var(--warning-soft)" },
        { icon: XCircle, value: result.failed, label: "Με σφάλμα", color: "var(--danger)", soft: "var(--danger-soft)" },
      ]
    : [];

  return (
    <div className="card">
      <h2>
        <FileSpreadsheet size={17} /> Εισαγωγή από Excel
      </h2>
      <p className="muted" style={{ marginTop: 0 }}>
        Ανέβασε <strong>οποιοδήποτε</strong> αρχείο .xlsx. Αν είναι υπόδειγμα Maintrack ή export
        από SAP (IW29), αναγνωρίζεται αυτόματα. Αλλιώς θα σε ρωτήσω τι είναι κάθε στήλη.
      </p>

      <div className="filters-row" style={{ marginBottom: 0 }}>
        <input
          ref={fileInputRef}
          type="file"
          accept=".xlsx"
          onChange={(e) => handleFileChosen(e.target.files[0] || null)}
        />
        <button className="btn" onClick={handleUpload} disabled={!canImport}>
          {uploading ? <Loader2 size={15} /> : <Upload size={15} />}
          {uploading ? "Γίνεται εισαγωγή..." : "Εισαγωγή"}
        </button>
        <button className="btn secondary" onClick={handleDownloadTemplate}>
          <Download size={15} />
          Κατέβασε υπόδειγμα
        </button>
        <button className="btn ghost" onClick={() => setShowHelp((s) => !s)}>
          {showHelp ? <ChevronDown size={15} /> : <ChevronRight size={15} />}
          <HelpCircle size={15} />
          Ποιες στήλες χρειάζονται;
        </button>
      </div>

      {inspecting && (
        <p className="muted" style={{ marginTop: "0.9rem" }}>
          Διαβάζω τις στήλες του αρχείου...
        </p>
      )}

      {errorText && (
        <p className="error-text" style={{ alignItems: "flex-start", lineHeight: 1.5 }}>
          <XCircle size={16} style={{ flexShrink: 0, marginTop: 2 }} />
          <span>{errorText}</span>
        </p>
      )}

      {/* Anagnorisi morfis + diakoptis gia xeirokiniti antistoixisi */}
      {preview && (
        <div className="filters-row" style={{ marginTop: "1rem", marginBottom: 0, alignItems: "center" }}>
          <span
            className="badge dot"
            style={
              preview.detectedFormat === "UNKNOWN"
                ? { background: "var(--warning-soft)", color: "var(--warning)" }
                : { background: "var(--success-soft)", color: "var(--success)" }
            }
          >
            {FORMAT_LABELS[preview.detectedFormat]}
          </span>
          <span className="muted">
            {preview.headers.length} στήλες · {preview.totalRows} γραμμές
          </span>
          {preview.detectedFormat !== "UNKNOWN" && (
            <button className="btn ghost small" onClick={() => setShowMapper((s) => !s)}>
              {showMapper ? <X size={14} /> : <Sliders size={14} />}
              {showMapper ? "Ακύρωση χειροκίνητης αντιστοίχισης" : "Άλλαξε την αντιστοίχιση"}
            </button>
          )}
        </div>
      )}

      {showMapper && preview && mapping && (
        <ColumnMapper
          preview={preview}
          mapping={mapping}
          onChange={setMapping}
          autoDetected={preview.detectedFormat !== "UNKNOWN"}
        />
      )}

      {showHelp && (
        <>
          <div className="divider" />
          <p className="muted" style={{ marginTop: 0 }}>
            Η εφαρμογή αναγνωρίζει τη μορφή από τα <strong>ονόματα των επικεφαλίδων</strong> στην
            πρώτη γραμμή. Αν το αρχείο σου δεν ταιριάζει σε καμία από τις παρακάτω μορφές, δεν
            πειράζει — θα σου ζητήσει να αντιστοιχίσεις μόνος σου τις στήλες.
          </p>

          <ColumnTable
            title="Μορφή 1 — Υπόδειγμα Maintrack"
            subtitle="Για χειροκίνητη καταχώρηση. Κατέβασε το έτοιμο αρχείο και συμπλήρωσέ το."
            columns={TEMPLATE_COLUMNS}
          />

          <ColumnTable
            title="Μορφή 2 — Export από SAP (IW29)"
            subtitle="Λίστα γνωστοποιήσεων συντήρησης, όπως βγαίνει από το SAP χωρίς επεξεργασία. Οι μηχανές που δεν υπάρχουν δημιουργούνται αυτόματα."
            columns={SAP_COLUMNS}
          />
        </>
      )}

      {result && (
        <>
          <div className="divider" />
          <div className="grid-3">
            {stats.map(({ icon: Icon, value, label, color, soft }) => (
              <div
                key={label}
                className="stat-card"
                style={{
                  "--accent": color,
                  "--accent-soft": soft,
                  border: "1px solid var(--border)",
                  borderRadius: "var(--radius)",
                  background: "var(--surface-2)",
                }}
              >
                <div className="stat-icon">
                  <Icon size={20} />
                </div>
                <div>
                  <div className="value" style={{ fontSize: "1.55rem" }}>{value}</div>
                  <div className="label">{label}</div>
                </div>
              </div>
            ))}
          </div>

          {result.errors.length > 0 && (
            <div className="table-wrap" style={{ marginTop: "1.1rem" }}>
              <table>
                <thead>
                  <tr>
                    <th style={{ width: 90 }}>Γραμμή</th>
                    <th>Πρόβλημα</th>
                  </tr>
                </thead>
                <tbody>
                  {result.errors.map((e, i) => (
                    <tr key={i}>
                      <td className="mono">#{e.row}</td>
                      <td style={{ color: "var(--danger)" }}>{e.message}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}
