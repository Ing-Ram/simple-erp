import { api } from "../../lib/api";
import type {
  Milestone,
  MilestoneRequest,
  Project,
  ProjectRequest,
  ProjectsDashboard,
  Task,
  TaskRequest,
  TaskStatus,
  TimeEntry,
  TimeEntryRequest,
} from "./types";

/** Typed endpoint functions for the Projects module. */
export function fetchProjectsDashboard(): Promise<ProjectsDashboard> {
  return api.get<ProjectsDashboard>("/api/v1/projects/dashboard");
}

// Projects ---------------------------------------------------------------------

export function fetchProjects(): Promise<Project[]> {
  return api.get<Project[]>("/api/v1/projects");
}

export function fetchProject(id: number): Promise<Project> {
  return api.get<Project>(`/api/v1/projects/${id}`);
}

export function createProject(body: ProjectRequest): Promise<Project> {
  return api.post<Project>("/api/v1/projects", body);
}

export function activateProject(id: number): Promise<Project> {
  return api.post<Project>(`/api/v1/projects/${id}/activate`, {});
}

export function holdProject(id: number): Promise<Project> {
  return api.post<Project>(`/api/v1/projects/${id}/hold`, {});
}

export function completeProject(id: number): Promise<Project> {
  return api.post<Project>(`/api/v1/projects/${id}/complete`, {});
}

export function cancelProject(id: number): Promise<Project> {
  return api.post<Project>(`/api/v1/projects/${id}/cancel`, {});
}

// Tasks ------------------------------------------------------------------------

export function fetchTasks(projectId: number): Promise<Task[]> {
  return api.get<Task[]>(`/api/v1/projects/tasks?projectId=${projectId}`);
}

export function createTask(body: TaskRequest): Promise<Task> {
  return api.post<Task>("/api/v1/projects/tasks", body);
}

export function changeTaskStatus(id: number, status: TaskStatus): Promise<Task> {
  return api.post<Task>(`/api/v1/projects/tasks/${id}/status`, { status });
}

export function assignTask(id: number, employeeId: number | null): Promise<Task> {
  const query = employeeId == null ? "" : `?employeeId=${employeeId}`;
  return api.post<Task>(`/api/v1/projects/tasks/${id}/assign${query}`, {});
}

// Milestones -------------------------------------------------------------------

export function fetchMilestones(projectId: number): Promise<Milestone[]> {
  return api.get<Milestone[]>(`/api/v1/projects/milestones?projectId=${projectId}`);
}

export function createMilestone(body: MilestoneRequest): Promise<Milestone> {
  return api.post<Milestone>("/api/v1/projects/milestones", body);
}

export function completeMilestone(id: number): Promise<Milestone> {
  return api.post<Milestone>(`/api/v1/projects/milestones/${id}/complete`, {});
}

export function waiveMilestone(id: number): Promise<Milestone> {
  return api.post<Milestone>(`/api/v1/projects/milestones/${id}/waive`, {});
}

// Time entries -----------------------------------------------------------------

export function fetchTimeEntries(taskId: number): Promise<TimeEntry[]> {
  return api.get<TimeEntry[]>(`/api/v1/projects/time-entries?taskId=${taskId}`);
}

export function logTime(body: TimeEntryRequest): Promise<TimeEntry> {
  return api.post<TimeEntry>("/api/v1/projects/time-entries", body);
}
