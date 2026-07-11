import apiClient from "./client";

export function login(username, password) {
  return apiClient.post("/api/auth/login", { username, password }).then((res) => res.data);
}
