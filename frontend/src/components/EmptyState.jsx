import { Inbox } from "lucide-react";

// Emfanizetai otan mia lista einai adeia. Kalytero apo ena skéto minima -
// dinei sto UI aeras kai kathodigei ton xristi ti na kanei.
export default function EmptyState({ icon: Icon = Inbox, message, hint }) {
  return (
    <div className="empty-state">
      <div className="empty-icon">
        <Icon size={24} />
      </div>
      <p>{message}</p>
      {hint && <p style={{ marginTop: "0.35rem", fontSize: "0.82rem", opacity: 0.8 }}>{hint}</p>}
    </div>
  );
}
