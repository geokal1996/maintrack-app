import apiClient from "./client";

export function login(username, password) {
  return apiClient.post("/api/auth/login", { username, password }).then((res) => res.data);
}

// O logariasmos dimiourgeitai PANTA os Texnikos kai ANENERGOS - to apofasizei o server.
// O xristis den mporei na syndethei mexri na ton egkrinei epoptis.
export function register({ username, password, fullName, jobTitle }) {
  return apiClient
    .post("/api/auth/register", { username, password, fullName, jobTitle })
    .then((res) => res.data);
}
