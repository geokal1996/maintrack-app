import { createContext, useContext, useState } from "react";
import { login as loginApi } from "../api/authApi";

// To Context einai o tropos tou React na "moirazei" dedomena (edo: poios einai
// syndedemenos) se OLA ta components, xoris na ta perasoume xeirokinita to kathena.
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("maintrack_user");
    return stored ? JSON.parse(stored) : null;
  });

  async function login(username, password) {
    const data = await loginApi(username, password);
    // I apantisi tou backend einai: { userId, token, username, fullName, role }
    const loggedInUser = {
      id: data.userId,
      username: data.username,
      fullName: data.fullName,
      role: data.role,
    };
    localStorage.setItem("maintrack_token", data.token);
    localStorage.setItem("maintrack_user", JSON.stringify(loggedInUser));
    setUser(loggedInUser);
    return loggedInUser;
  }

  function logout() {
    localStorage.removeItem("maintrack_token");
    localStorage.removeItem("maintrack_user");
    setUser(null);
  }

  const value = {
    user,
    isAuthenticated: !!user,
    isSupervisor: user?.role === "SUPERVISOR",
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// Custom hook - etsi se kathe component pou to xreiazetai, grafoume apla
// "const { user, logout } = useAuth();" anti na kanoume import to Context kathe fora.
export function useAuth() {
  return useContext(AuthContext);
}
