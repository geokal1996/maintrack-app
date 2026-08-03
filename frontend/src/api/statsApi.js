import apiClient from "./client";

export function getParetoDashboard() {
  return apiClient.get("/api/stats/pareto").then((res) => res.data);
}
