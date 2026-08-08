import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react";

// Elenxoi selidopoiisis. Deixnei mexri 5 arithmous selidon gyro apo tin trexousa,
// oste na min gemisei i othoni otan yparxoun 40 selides.
export default function Pagination({ page, totalPages, totalElements, size, onPageChange, onSizeChange }) {
  if (totalElements === 0) {
    return null;
  }

  const from = page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);

  // Poious arithmous selidon deixnoume
  const windowSize = 5;
  let start = Math.max(0, page - Math.floor(windowSize / 2));
  let end = Math.min(totalPages, start + windowSize);
  start = Math.max(0, end - windowSize);
  const pages = [];
  for (let i = start; i < end; i++) {
    pages.push(i);
  }

  return (
    <div className="pagination">
      <span className="muted">
        {from}–{to} από {totalElements}
      </span>

      <div className="pagination-controls">
        <button
          className="btn ghost small"
          onClick={() => onPageChange(0)}
          disabled={page === 0}
          title="Πρώτη σελίδα"
        >
          <ChevronsLeft size={15} />
        </button>
        <button
          className="btn ghost small"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          title="Προηγούμενη"
        >
          <ChevronLeft size={15} />
        </button>

        {pages.map((p) => (
          <button
            key={p}
            className={p === page ? "btn small" : "btn ghost small"}
            onClick={() => onPageChange(p)}
          >
            {p + 1}
          </button>
        ))}

        <button
          className="btn ghost small"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
          title="Επόμενη"
        >
          <ChevronRight size={15} />
        </button>
        <button
          className="btn ghost small"
          onClick={() => onPageChange(totalPages - 1)}
          disabled={page >= totalPages - 1}
          title="Τελευταία σελίδα"
        >
          <ChevronsRight size={15} />
        </button>
      </div>

      <select
        className="page-size"
        value={size}
        onChange={(e) => onSizeChange(Number(e.target.value))}
        title="Εγγραφές ανά σελίδα"
      >
        {[10, 25, 50, 100].map((s) => (
          <option key={s} value={s}>
            {s} ανά σελίδα
          </option>
        ))}
      </select>
    </div>
  );
}
