import toast from "react-hot-toast";
import { AlertTriangle } from "lucide-react";

// Antikathista to askhimo window.confirm() tou browser me ena omorfo,
// diko mas panel epivevaiosis. Epistrefei Promise<boolean> - opote sto
// component grafoume apla: if (!(await confirmToast("..."))) return;
export function confirmToast(message, { confirmLabel = "Ναι, συνέχεια", cancelLabel = "Ακύρωση" } = {}) {
  return new Promise((resolve) => {
    const id = toast.custom(
      (t) => (
        <div
          style={{
            background: "var(--surface)",
            color: "var(--text)",
            border: "1px solid var(--border)",
            borderRadius: "var(--radius)",
            boxShadow: "var(--shadow-lg)",
            padding: "1rem 1.1rem",
            maxWidth: 380,
            opacity: t.visible ? 1 : 0,
            transform: t.visible ? "translateY(0)" : "translateY(-8px)",
            transition: "all 0.18s ease",
          }}
        >
          <div style={{ display: "flex", gap: "0.7rem", alignItems: "flex-start" }}>
            <div
              style={{
                display: "grid",
                placeItems: "center",
                width: 34,
                height: 34,
                borderRadius: 10,
                background: "var(--danger-soft)",
                color: "var(--danger)",
                flexShrink: 0,
              }}
            >
              <AlertTriangle size={18} />
            </div>
            <div style={{ fontSize: "0.9rem", lineHeight: 1.45, paddingTop: 4 }}>{message}</div>
          </div>
          <div style={{ display: "flex", gap: "0.5rem", justifyContent: "flex-end", marginTop: "0.9rem" }}>
            <button
              className="btn secondary small"
              onClick={() => {
                toast.dismiss(id);
                resolve(false);
              }}
            >
              {cancelLabel}
            </button>
            <button
              className="btn danger small"
              onClick={() => {
                toast.dismiss(id);
                resolve(true);
              }}
            >
              {confirmLabel}
            </button>
          </div>
        </div>
      ),
      { duration: Infinity }
    );
  });
}
