import { useRef, useState } from "react";
import toast from "react-hot-toast";
import {
  FileSpreadsheet,
  Upload,
  Download,
  CheckCircle2,
  SkipForward,
  XCircle,
} from "lucide-react";
import { importFaultsFromExcel, downloadImportTemplate } from "../api/importApi";

// Panel gia mazikí eisagogí vlavón apo arxeio Excel.
// Emfanizetai mono se SUPERVISOR/MANAGER (to elegxei i selida pou to kalei).
export default function ExcelImportPanel({ onImported }) {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const fileInputRef = useRef(null);

  async function handleUpload() {
    if (!file) return;
    setUploading(true);
    setResult(null);
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
      toast.error(err.response?.data?.message || "Δεν ήταν δυνατή η εισαγωγή του αρχείου");
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
        Ανέβασε αρχείο .xlsx με τις στήλες του υποδείγματος. Οι σωστές γραμμές εισάγονται·
        όσες έχουν πρόβλημα αναφέρονται παρακάτω χωρίς να μπλοκάρουν τις υπόλοιπες.
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
      </div>

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
