import apiClient from "./client";

export function getFaults({ status, machineId } = {}) {
  const params = {};
  if (status) params.status = status;
  if (machineId) params.machineId = machineId;
  return apiClient.get("/api/faults", { params }).then((res) => res.data);
}

export function getFault(id) {
  return apiClient.get(`/api/faults/${id}`).then((res) => res.data);
}

export function createFault(fault) {
  return apiClient.post("/api/faults", fault).then((res) => res.data);
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
