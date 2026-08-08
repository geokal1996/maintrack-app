import apiClient from "./client";

export function getUsers() {
  return apiClient.get("/api/users").then((res) => res.data);
}

export function createUser(user) {
  return apiClient.post("/api/users", user).then((res) => res.data);
}

export function setUserActive(id, active) {
  return apiClient.patch(`/api/users/${id}/active`, null, { params: { active } });
}

export function updateUserRole(id, role) {
  return apiClient.patch(`/api/users/${id}/role`, { role }).then((res) => res.data);
}

// Allagi tou DIKOU mou kodikou - diathesimo se kathe syndedemeno xristi
export function changeOwnPassword(currentPassword, newPassword) {
  return apiClient.post("/api/users/me/password", { currentPassword, newPassword });
}
