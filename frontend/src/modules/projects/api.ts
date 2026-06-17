import { api } from "../../lib/api";
import type { ProjectsDashboard } from "./types";

/** Typed endpoint functions for the Projects module. */
export function fetchProjectsDashboard(): Promise<ProjectsDashboard> {
  return api.get<ProjectsDashboard>("/api/v1/projects/dashboard");
}
