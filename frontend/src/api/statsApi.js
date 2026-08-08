import apiClient from "./client";

// Ta filtra einai proairetika. Ta "from"/"to" einai imerominies se morfi YYYY-MM-DD.
function statsParams({ from, to, area, machineId } = {}) {
  const params = {};
  if (from) params.from = from;
  if (to) params.to = to;
  if (area) params.area = area;
  if (machineId) params.machineId = machineId;
  return params;
}

export function getParetoDashboard(filters) {
  return apiClient
    .get("/api/stats/pareto", { params: statsParams(filters) })
    .then((res) => res.data);
}

// MTBF / MTTR / diathesimotita
export function getReliability(filters) {
  return apiClient
    .get("/api/stats/reliability", { params: statsParams(filters) })
    .then((res) => res.data);
}

// Vlaves kai xronos diakopis ana mina
export function getTrend(filters) {
  return apiClient
    .get("/api/stats/trend", { params: statsParams(filters) })
    .then((res) => res.data);
}
