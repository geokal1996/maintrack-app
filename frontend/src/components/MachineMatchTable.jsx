import { Link2, PlusCircle, AlertTriangle, CheckCircle2 } from "lucide-react";

// Pinakas epivevaiosis: "vrika auta ta onomata mihanon sto arxeio sou - na ta
// syndeso etsi;". TIPOTA den grafetai sti vasi mexri na patisei "Eisagogi".
//
// Giati yparxei: to arxeio tou proistamenou grafei "Πρέσα 1" san eleuthero keimeno.
// I efarmogi mporei na mantepsei oti einai i idi katagegrammeni "7100-EXT-PRS1",
// alla mia lathos mantepsia tha xreone tis vlaves se lathos mihani - kai to Pareto
// tha edeixne lathos "enoxo". Gi' auto o xristis vlepei kai apofasizei.
export default function MachineMatchTable({ matches, resolutions, machines, onChange }) {
  function setResolution(rawName, value) {
    onChange({ ...resolutions, [rawName]: value === "" ? null : Number(value) });
  }

  const linked = matches.filter((m) => resolutions[m.rawName] != null).length;
  const created = matches.length - linked;

  return (
    <div>
      <div className="divider" />

      <div className="card-header" style={{ marginBottom: "0.4rem" }}>
        <h3 style={{ fontSize: "0.98rem", margin: 0, display: "flex", alignItems: "center", gap: "0.45rem" }}>
          <Link2 size={16} /> Αντιστοίχιση μηχανών
        </h3>
        <div style={{ display: "flex", gap: "0.4rem", flexWrap: "wrap" }}>
          <span className="chip">
            <CheckCircle2 size={12} /> {linked} σύνδεση σε υπάρχουσα
          </span>
          <span className="chip">
            <PlusCircle size={12} /> {created} νέες
          </span>
        </div>
      </div>

      <p className="muted" style={{ marginTop: 0 }}>
        Βρήκα {matches.length} διαφορετικά ονόματα μηχανών στο αρχείο σου. Όπου βρήκα πιθανή
        αντιστοιχία με μηχανή που ήδη υπάρχει, την προτείνω — αλλά <strong>έλεγξέ τις πριν
        προχωρήσεις</strong>. Λάθος αντιστοίχιση σημαίνει ότι οι βλάβες θα χρεωθούν σε άλλη μηχανή.
      </p>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Στο αρχείο σου</th>
              <th style={{ width: 90 }}>Γραμμές</th>
              <th style={{ width: "45%" }}>Θα καταχωρηθεί ως</th>
              <th style={{ width: 120 }}>Σιγουριά</th>
            </tr>
          </thead>
          <tbody>
            {matches.map((m) => {
              const current = resolutions[m.rawName] ?? "";
              const isNew = current === "" || current == null;
              // Xamili sigouria = i protasi mas, alla o xristis prepei na ti koitaxei
              const lowConfidence = !isNew && m.machineId === current && m.confidence < 90;

              return (
                <tr key={m.rawName}>
                  <td style={{ fontWeight: 600 }}>{m.rawName}</td>
                  <td className="muted">{m.rowCount}</td>
                  <td>
                    <select
                      value={current}
                      onChange={(e) => setResolution(m.rawName, e.target.value)}
                      style={{
                        width: "100%",
                        padding: "0.4rem 0.5rem",
                        border: "1px solid var(--border-strong)",
                        borderRadius: "var(--radius-sm)",
                        background: "var(--surface-2)",
                        color: "var(--text)",
                        fontFamily: "inherit",
                        fontSize: "0.86rem",
                      }}
                    >
                      <option value="">— νέα μηχανή «{m.rawName}» —</option>
                      {machines.map((machine) => (
                        <option key={machine.id} value={machine.id}>
                          {machine.code} — {machine.name}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td>
                    {isNew ? (
                      <span className="badge" style={{ background: "var(--info-soft)", color: "var(--info)" }}>
                        <PlusCircle size={11} /> Νέα
                      </span>
                    ) : lowConfidence ? (
                      <span className="badge" style={{ background: "var(--warning-soft)", color: "var(--warning)" }}>
                        <AlertTriangle size={11} /> {m.confidence}%
                      </span>
                    ) : (
                      <span className="badge" style={{ background: "var(--success-soft)", color: "var(--success)" }}>
                        <CheckCircle2 size={11} /> {m.confidence ? `${m.confidence}%` : "χειροκίνητα"}
                      </span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
