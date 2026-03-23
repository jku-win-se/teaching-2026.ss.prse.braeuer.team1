import { createContext, useContext, useState, useEffect, type ReactNode } from "react";
import type { User } from "@/types/types";

interface AuthContextType {
  user: User | null;
  login: (user: User) => void;
  logout: () => void;
  isOwner: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });

  useEffect(() => {
    if (user) {
      localStorage.setItem("user", JSON.stringify(user));
    } else {
      localStorage.removeItem("user");
    }
  }, [user]);

  const login = (u: User) => setUser(u);
  const logout = () => setUser(null);
  const isOwner = user?.role === "OWNER";

  return (
    <AuthContext.Provider value={{ user, login, logout, isOwner }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
