import apiClient from "./client";

// Selidopoiimeni anazitisi. Epistrefei { content, page, size, totalElements, totalPages, first, last }
export function searchFaults({ status, machineId, assignedToUserId, q, page = 0, size = 25 } = {}) {
  const params = { page, size };
  if (status) params.status = status;
  if (machineId) params.machineId = machineId;
  if (assignedToUserId) params.assignedToUserId = assignedToUserId;
  if (q && q.trim()) params.q = q.trim();
  return apiClient.get("/api/faults", { params }).then((res) => res.data);
}

// Katevazei ta apotelesmata os arxeio Excel, me ta IDIA filtra me ti lista.
// To "responseType: blob" einai apraitito - xoris auto o axios tha prospathouse
// na diavasei to binary arxeio san keimeno kai tha to katestrefe.
export async function exportFaults({ status, machineId, assignedToUserId, q } = {}) {
  const params = {};
  if (status) params.status = status;
  if (machineId) params.machineId = machineId;
  if (assignedToUserId) params.assignedToUserId = assignedToUserId;
  if (q && q.trim()) params.q = q.trim();

  const res = await apiClient.get("/api/faults/export", { params, responseType: "blob" });

  // To onoma tou arxeiou erxetai apo to Content-Disposition tou server
  const disposition = res.headers["content-disposition"] || "";
  const match = disposition.match(/filename="?([^"]+)"?/);
  const filename = match ? match[1] : "maintrack-vlaves.xlsx";

  const url = window.URL.createObjectURL(new Blob([res.data]));
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  // Eleftheronoume ti mnimi - alliws to arxeio menei fortomeno sto tab
  window.URL.revokeObjectURL(url);
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

// Anathesi vlavis se texniko. Stelnoume userId: null gia na afairethei i anathesi.
export function assignFault(id, userId) {
  return apiClient.patch(`/api/faults/${id}/assignee`, { userId }).then((res) => res.data);
}

export function getFaultHistory(id) {
  return apiClient.get(`/api/faults/${id}/history`).then((res) => res.data);
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
