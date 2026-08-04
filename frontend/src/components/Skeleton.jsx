// Mikra "fantasmata" pou emfanizontai oso fortonoun ta dedomena.
// Deixnoun poly kalytera apo ena skéto "Fortosi..." giati o xristis vlepei
// apo tora ti sxima tha exei to periexomeno.

export function SkeletonTable({ rows = 5, cols = 5 }) {
  return (
    <div aria-hidden="true">
      <div style={{ display: "flex", gap: "1rem", marginBottom: "1rem" }}>
        {Array.from({ length: cols }).map((_, i) => (
          <div key={i} className="skeleton" style={{ height: 10, flex: 1 }} />
        ))}
      </div>
      {Array.from({ length: rows }).map((_, r) => (
        <div key={r} style={{ display: "flex", gap: "1rem", marginBottom: "0.9rem" }}>
          {Array.from({ length: cols }).map((_, c) => (
            <div
              key={c}
              className="skeleton"
              style={{ height: 14, flex: c === 1 ? 2 : 1 }}
            />
          ))}
        </div>
      ))}
    </div>
  );
}

export function SkeletonCards({ count = 3 }) {
  return (
    <div className="grid-3" aria-hidden="true">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="card" style={{ height: 92 }}>
          <div className="skeleton" style={{ height: 12, width: "55%", marginBottom: 14 }} />
          <div className="skeleton" style={{ height: 26, width: "35%" }} />
        </div>
      ))}
    </div>
  );
}

export function SkeletonBlock({ height = 200 }) {
  return <div className="skeleton" style={{ height }} aria-hidden="true" />;
}
