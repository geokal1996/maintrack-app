import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getFaults } from "../api/faultsApi";
import { getMachines } from "../api/machinesApi";

export default function DashboardPage() {
  const [faults, setFaults] = useState([]);
  const [machines, setMachines] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getFaults({ status: "OPEN" }), getMachines()])
      .then(([openFaults, allMachines]) => {
        setFaults(openFaults);
        setMachines(allMachines);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="muted">Φόρτωση...</p>;

  const downMachines = machines.filter((m) => m.status === "DOWN");

  return (
    <div>
      <div className="grid-3">
        <div className="card stat-box">
          <div className="value">{faults.length}</div>
          <div className="label">Ανοιχτές βλάβες</div>
        </div>
        <div className="card stat-box">
          <div className="value">{downMachines.length}</div>
          <div className="label">Μηχανές εκτός λειτουργίας</div>
        </div>
        <div className="card stat-box">
          <div className="value">{machines.length}</div>
          <div className="label">Σύνολο μηχανών</div>
        </div>
      </div>

      <div className="card">
        <h2>Ανοιχτές βλάβες</h2>
        {faults.length === 0 ? (
          <p className="muted">Δεν υπάρχουν ανοιχτές βλάβες αυτή τη στιγμή.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Μηχανή</th>
                <th>Τίτλος</th>
                <th>Σοβαρότητα</th>
                <th>Δημιουργήθηκε</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {faults.map((f) => (
                <tr key={f.id}>
                  <td>{f.machineCode}</td>
                  <td>{f.title}</td>
                  <td><span className={`badge severity-${f.severity}`}>{f.severity}</span></td>
                  <td>{new Date(f.createdAt).toLocaleString("el-GR")}</td>
                  <td><Link to={`/faults/${f.id}`}>Λεπτομέρειες →</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="card">
        <h2>Μηχανές εκτός λειτουργίας</h2>
        {downMachines.length === 0 ? (
          <p className="muted">Όλες οι μηχανές λειτουργούν κανονικά.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Κωδικός</th>
                <th>Όνομα</th>
                <th>Περιοχή</th>
              </tr>
            </thead>
            <tbody>
              {downMachines.map((m) => (
                <tr key={m.id}>
                  <td>{m.code}</td>
                  <td>{m.name}</td>
                  <td>{m.area}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
