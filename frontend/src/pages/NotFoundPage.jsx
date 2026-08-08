import { Link, useLocation } from "react-router-dom";
import { MapPinOff, ArrowLeft } from "lucide-react";

// Emfanizetai otan o xristis grapsei ena URL pou den yparxei (p.x. /vlaves anti gia
// /faults, i ena palio bookmark). Xoris auto, to React Router tha edeixne KENI selida -
// pou moiazei me "kollise i efarmogi" kai einai xeirotero apo ena katharo minima.
export default function NotFoundPage() {
  const location = useLocation();

  return (
    <div className="card" style={{ textAlign: "center", padding: "3rem 1.5rem" }}>
      <MapPinOff size={44} style={{ opacity: 0.35, marginBottom: "0.9rem" }} />
      <h1 style={{ fontSize: "1.6rem", margin: "0 0 0.4rem" }}>Η σελίδα δεν βρέθηκε</h1>
      <p className="muted" style={{ margin: "0 0 0.3rem" }}>
        Δεν υπάρχει σελίδα στη διεύθυνση <code>{location.pathname}</code>.
      </p>
      <p className="muted" style={{ marginTop: 0 }}>
        Ίσως ο σύνδεσμος είναι λάθος ή η σελίδα μετακινήθηκε.
      </p>
      <div className="form-actions" style={{ justifyContent: "center", marginTop: "1.2rem" }}>
        <Link to="/" className="btn">
          <ArrowLeft size={16} /> Επιστροφή στο Dashboard
        </Link>
      </div>
    </div>
  );
}
