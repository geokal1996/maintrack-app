import apiClient from "./client";

// Selidopoiimeni anazitisi. Epistrefei { content, page, size, totalElements, totalPages, first, last }
export function searchFaults({ status, machineId, q, page = 0, size = 25 } = {}) {
  const params = { page, size };
  if (status) params.status = status;
  if (machineId) params.machineId = machineId;
  if (q && q.trim()) params.q = q.trim();
  return apiClient.get("/api/faults", { params }).then((res) => res.data);
}

// Xoris selidopoiisi - gia mikres listes (p.x. oi anoixtes vlaves sto Dashboard)
export function getFaults({ status, machineId } = {}) {
  const params = {};
  if (status) params.status = status;
  if (machineId) params.machineId = machineId;
  return apiClient.get("/api/faults/all", { params }).then((res) => res.data);
}

export function getFault(id) {
  return apiClient.get(`/api/faults/${id}`).then((res) => res.data);
}

export function createFault(fault) {
  return apiClient.post("/api/faults", fault).then((res) => res.data);
}

export function updateFault(id, fault) {
  return apiClient.put(`/api/faults/${id}`, fault).then((res) => res.data);
}

export function deleteFault(id) {
  return apiClient.delete(`/api/faults/${id}`);
}

export function updateFaultStatus(id, status) {
  return apiClient.patch(`/api/faults/${id}/status`, { status }).then((res) => res.data);
}

export function getFaultActions(faultId) {
  return apiClient.get(`/api/faults/${faultId}/actions`).then((res) => res.data);
}

export function addFaultAction(faultId, action) {
  return apiClient.post(`/api/faults/${faultId}/actions`, action).then((res) => res.data);
}

export function updateFaultAction(faultId, actionId, action) {
  return apiClient.put(`/api/faults/${faultId}/actions/${actionId}`, action).then((res) => res.data);
}

export function deleteFaultAction(faultId, actionId) {
  return apiClient.delete(`/api/faults/${faultId}/actions/${actionId}`);
}
