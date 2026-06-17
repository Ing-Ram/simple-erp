import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { api } from "./api";
import { clearToken, getToken, setToken } from "./authToken";

/** The signed-in user, mirroring the backend's login/me payload. */
export interface AuthUser {
  username: string;
  displayName: string;
  role: "ADMIN" | "MEMBER";
}

interface LoginResponse extends AuthUser {
  token: string;
}

interface AuthState {
  user: AuthUser | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

/** Holds the session: restores it from a stored token on load, exposes login/logout. */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!getToken()) {
      setLoading(false);
      return;
    }
    api
      .get<AuthUser>("/api/v1/auth/me")
      .then(setUser)
      .catch(() => clearToken())
      .finally(() => setLoading(false));
  }, []);

  const login = async (username: string, password: string) => {
    const res = await api.post<LoginResponse>("/api/v1/auth/login", { username, password });
    setToken(res.token);
    setUser({ username: res.username, displayName: res.displayName, role: res.role });
  };

  const logout = () => {
    clearToken();
    setUser(null);
    window.location.assign("/login");
  };

  return <AuthContext.Provider value={{ user, loading, login, logout }}>{children}</AuthContext.Provider>;
}

/** Access the auth state; throws if used outside the provider. */
export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
}

/** Gates its children behind a valid session, redirecting to the login page otherwise. */
export function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) {
    return <div className="p-8 text-sm text-neutral-500">Loading…</div>;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}
