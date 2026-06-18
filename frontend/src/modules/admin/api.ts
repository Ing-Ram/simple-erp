import { api } from "../../lib/api";
import type { AuthUser } from "../../lib/auth";

/** A managed user account (mirrors backend UserResponse). */
export interface ManagedUser {
  id: number;
  username: string;
  displayName: string;
  role: AuthUser["role"];
}

export interface CreateUserRequest {
  username: string;
  displayName: string;
  role: AuthUser["role"];
  password: string;
}

/** Admin-only user management endpoints. */
export function fetchUsers(): Promise<ManagedUser[]> {
  return api.get<ManagedUser[]>("/api/v1/users");
}

export function createUser(body: CreateUserRequest): Promise<ManagedUser> {
  return api.post<ManagedUser>("/api/v1/users", body);
}

export function resetUserPassword(id: number, password: string): Promise<ManagedUser> {
  return api.post<ManagedUser>(`/api/v1/users/${id}/reset-password`, { password });
}

export function changeUserRole(id: number, role: AuthUser["role"]): Promise<ManagedUser> {
  return api.post<ManagedUser>(`/api/v1/users/${id}/role`, { role });
}
