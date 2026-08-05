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
} from "lucide-react";
import { importFaultsFromExcel, downloadImportTemplate } from "../api/importApi";

// Ti stiles perimenei i kathe morfi. Emfanizetai sto ptyssomeno panel odigion,
// oste o xristis na kserei ek ton proteron pos prepei na einai to arxeio tou.
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
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [showHelp, setShowHelp] = useState(false);
  const [formatError, setFormatError] = useState("");
  const fileInputRef = useRef(null);

  async function handleUpload() {
    if (!file) return;
    setUploading(true);
    setResult(null);
    setFormatError("");
    try {
      const data = await importFaultsFromExcel(file);
      setResult(data);
      setFile(null);
      if (fileInputRef.current) fileInputRef.current.value = "";

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
      // To minima gia ti morfi einai makry kai xrisimo -> to deixnoume mesa sti
      // selida (kai anoigoume kai tis odigies), oxi mono se ena toast pou fevgei.
      setFormatError(message);
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

  const stats = result
    ? [
        {
          icon: CheckCircle2,
          value: result.imported,
          label: "Εισήχθησαν",
          color: "var(--success)",
          soft: "var(--success-soft)",
        },
        {
          icon: SkipForward,
          value: result.skipped,
          label: "Υπήρχαν ήδη",
          color: "var(--warning)",
          soft: "var(--warning-soft)",
        },
        {
          icon: XCircle,
          value: result.failed,
          label: "Με σφάλμα",
          color: "var(--danger)",
          soft: "var(--danger-soft)",
        },
      ]
    : [];

  return (
    <div className="card">
      <h2>
        <FileSpreadsheet size={17} /> Εισαγωγή από Excel
      </h2>
      <p className="muted" style={{ marginTop: 0 }}>
        Ανέβασε αρχείο <strong>.xlsx</strong> — είτε με τις στήλες του υποδείγματος, είτε
        απευθείας export γνωστοποιήσεων από <strong>SAP (IW29)</strong>. Η μορφή αναγνωρίζεται
        αυτόματα. Οι σωστές γραμμές εισάγονται· όσες έχουν πρόβλημα αναφέρονται παρακάτω
        χωρίς να μπλοκάρουν τις υπόλοιπες.
      </p>

      <div className="filters-row" style={{ marginBottom: 0 }}>
        <input
          ref={fileInputRef}
          type="file"
          accept=".xlsx"
          onChange={(e) => setFile(e.target.files[0] || null)}
        />
        <button className="btn" onClick={handleUpload} disabled={!file || uploading}>
          <Upload size={15} />
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

      {formatError && (
        <p className="error-text" style={{ alignItems: "flex-start", lineHeight: 1.5 }}>
          <XCircle size={16} style={{ flexShrink: 0, marginTop: 2 }} />
          <span>{formatError}</span>
        </p>
      )}

      {showHelp && (
        <>
          <div className="divider" />
          <p className="muted" style={{ marginTop: 0 }}>
            Η εφαρμογή αναγνωρίζει τη μορφή του αρχείου από τα <strong>ονόματα των επικεφαλίδων</strong>
            {" "}στην πρώτη γραμμή — όχι από τη σειρά τους. Αρκεί να υπάρχουν οι υποχρεωτικές στήλες·
            οι υπόλοιπες είναι προαιρετικές και μπορούν να λείπουν εντελώς.
          </p>

          <ColumnTable
            title="Μορφή 1 — Υπόδειγμα Maintrack"
            subtitle="Για χειροκίνητη καταχώρηση. Κατέβασε το έτοιμο αρχείο από το κουμπί «Κατέβασε υπόδειγμα» και συμπλήρωσέ το."
            columns={TEMPLATE_COLUMNS}
          />

          <ColumnTable
            title="Μορφή 2 — Export από SAP (IW29)"
            subtitle="Λίστα γνωστοποιήσεων συντήρησης. Ανέβασε το αρχείο όπως βγαίνει από το SAP, χωρίς επεξεργασία. Οι μηχανές που δεν υπάρχουν δημιουργούνται αυτόματα."
            columns={SAP_COLUMNS}
          />

          <p className="muted" style={{ marginBottom: 0 }}>
            <strong>Αν το αρχείο σου έχει άλλα ονόματα στηλών:</strong> μετονόμασε τις επικεφαλίδες
            ώστε να ταιριάζουν με μία από τις δύο μορφές, ή αντίγραψε τα δεδομένα σου στο υπόδειγμα.
          </p>
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
                  <div className="value" style={{ fontSize: "1.55rem" }}>
                    {value}
                  </div>
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
