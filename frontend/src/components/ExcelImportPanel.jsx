import { useRef, useState } from "react";
import { importFaultsFromExcel, downloadImportTemplate } from "../api/importApi";

// Panel gia mazikí eisagogí vlavón apo arxeio Excel.
// Emfanizetai mono se SUPERVISOR/MANAGER (to elegxei i selida pou to kalei).
export default function ExcelImportPanel({ onImported }) {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const fileInputRef = useRef(null);

  async function handleUpload() {
    if (!file) return;
    setUploading(true);
    setError("");
    setResult(null);
    try {
      const data = await importFaultsFromExcel(file);
      setResult(data);
      setFile(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
      // Enimerose ti lista vlavon tis selidas
      if (onImported) onImported();
    } catch (err) {
      setError(err.response?.data?.message || "Δεν ήταν δυνατή η εισαγωγή του αρχείου");
    } finally {
      setUploading(false);
    }
  }

  return (
    <div className="card">
      <h2>Εισαγωγή από Excel</h2>
      <p className="muted">
        Ανέβασε αρχείο .xlsx με τις στήλες του υποδείγματος. Οι σωστές γραμμές
        εισάγονται· όσες έχουν πρόβλημα αναφέρονται παρακάτω χωρίς να μπλοκάρουν τις υπόλοιπες.
      </p>

      <div className="filters-row">
        <input
          ref={fileInputRef}
          type="file"
          accept=".xlsx"
          onChange={(e) => setFile(e.target.files[0] || null)}
        />
        <button className="btn" onClick={handleUpload} disabled={!file || uploading}>
          {uploading ? "Γίνεται εισαγωγή..." : "Εισαγωγή"}
        </button>
        <button className="btn secondary" onClick={downloadImportTemplate}>
          Κατέβασε υπόδειγμα
        </button>
      </div>

      {error && <p className="error-text">{error}</p>}

      {result && (
        <div style={{ marginTop: "1rem" }}>
          <div className="grid-3">
            <div className="stat-box">
              <div className="value" style={{ color: "#166534" }}>{result.imported}</div>
              <div className="label">Εισήχθησαν</div>
            </div>
            <div className="stat-box">
              <div className="value" style={{ color: "#854d0e" }}>{result.skipped}</div>
              <div className="label">Υπήρχαν ήδη</div>
            </div>
            <div className="stat-box">
              <div className="value" style={{ color: "#991b1b" }}>{result.failed}</div>
              <div className="label">Με σφάλμα</div>
            </div>
          </div>

          {result.errors.length > 0 && (
            <table style={{ marginTop: "1rem" }}>
              <thead>
                <tr>
                  <th>Γραμμή</th>
                  <th>Πρόβλημα</th>
                </tr>
              </thead>
              <tbody>
                {result.errors.map((e, i) => (
                  <tr key={i}>
                    <td>{e.row}</td>
                    <td>{e.message}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}
