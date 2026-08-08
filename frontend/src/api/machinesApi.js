import apiClient from "./client";

export function getMachines() {
  return apiClient.get("/api/machines").then((res) => res.data);
}

export function getMachine(id) {
  return apiClient.get(`/api/machines/${id}`).then((res) => res.data);
}

export function createMachine(machine) {
  return apiClient.post("/api/machines", machine).then((res) => res.data);
}

export function updateMachine(id, machine) {
  return apiClient.put(`/api/machines/${id}`, machine).then((res) => res.data);
}

export function getMachineFaults(machineId) {
  return apiClient
    .get("/api/faults/all", { params: { machineId } })
    .then((res) => res.data);
}

export function deleteMachine(id) {
  return apiClient.delete(`/api/machines/${id}`);
}
