import axios from "axios";

// Auto einai to "kentriko" antikeimeno pou xrisimopoioume gia OLA ta requests
// pros to backend. Ola ta alla api/*.js arxeia to xrisimopoioun.
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
});

// "Interceptor" = kodikas pou "petiketai" mprosta apo KATHE request.
// Edo pairnoume to token apo to localStorage (an yparxei) kai to
// prosthetoume automata sto header - etsi den to grafoume xeirokinita pantou.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("maintrack_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Endpoints opou ena 401 einai FYSIOLOGIKI apantisi kai OXI "elikse i syndesi sou".
// P.x. sto login, to 401 simainei apla "lathos kodikos" - den exei noima na
// "apossyndesoume" kapoion pou den einai kan syndedemenos.
const AUTH_ENDPOINTS = ["/api/auth/login", "/api/auth/register"];

// An to backend apantisei 401 (to token elikse i den isxyei), stelnoume
// automata ton xristi piso sto login.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url || "";
    const isAuthEndpoint = AUTH_ENDPOINTS.some((endpoint) => url.includes(endpoint));

    if (status === 401 && !isAuthEndpoint) {
      localStorage.removeItem("maintrack_token");
      localStorage.removeItem("maintrack_user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default apiClient;
