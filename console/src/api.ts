// Shared API client for the Kuna console. Same-origin with the server (served at /console), so it
// calls /api/** directly. Attaches the moderator's Bearer token and the app-only key on every request;
// on a 401 it clears the session (auto sign-out), matching the app's behavior.

// Deterrent app-only key (also baked into the app binary). Not a secret — see server config/AppGate.kt.
const APP_KEY = "b7e2d40915a86c3f0e1d7942bc63f58a2049e1cd76b8340af5921e6d0c8b47f3";

const TOKEN_KEY = "kuna.console.token";
const ROLE_KEY = "kuna.console.role";
const EMAIL_KEY = "kuna.console.email";

export const session = {
  token: () => sessionStorage.getItem(TOKEN_KEY),
  role: () => sessionStorage.getItem(ROLE_KEY),
  email: () => sessionStorage.getItem(EMAIL_KEY),
  isSuperAdmin: () => sessionStorage.getItem(ROLE_KEY) === "SUPERADMIN",
  set: (token: string, role: string, email: string) => {
    sessionStorage.setItem(TOKEN_KEY, token);
    sessionStorage.setItem(ROLE_KEY, role);
    sessionStorage.setItem(EMAIL_KEY, email);
  },
  clear: () => {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(ROLE_KEY);
    sessionStorage.removeItem(EMAIL_KEY);
  },
};

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

type Options = { method?: string; body?: unknown; auth?: boolean };

export async function api<T>(path: string, opts: Options = {}): Promise<T> {
  const headers: Record<string, string> = { "X-App-Key": APP_KEY };
  if (opts.body !== undefined) headers["Content-Type"] = "application/json";
  if (opts.auth !== false) {
    const t = session.token();
    if (t) headers["Authorization"] = `Bearer ${t}`;
  }
  const res = await fetch(path, {
    method: opts.method ?? "GET",
    headers,
    body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined,
  });
  if (res.status === 401) {
    session.clear();
    window.dispatchEvent(new Event("kuna-unauthorized"));
    throw new ApiError(401, "Sesión expirada");
  }
  if (!res.ok) {
    let message = `Error ${res.status}`;
    try {
      const j = await res.json();
      message = j?.message ?? j?.error ?? message;
    } catch {
      /* ignore */
    }
    throw new ApiError(res.status, message);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

// ---- types ------------------------------------------------------------------------------------

export type AuditEntry = {
  id: number;
  actorEmail: string;
  actorRole: string;
  action: string;
  entityType: string;
  entityId: string | null;
  beforeJson: string | null;
  afterJson: string | null;
  ip: string | null;
  createdAt: string;
  revertedAt: string | null;
  revertedBy: string | null;
};

export type ModeratorActivity = {
  email: string;
  role: string;
  enabled: boolean;
  counts: Record<string, number>;
  lastActiveAt: string | null;
};

export type Admin = { id: number; email: string; role: string; enabled: boolean };

export type CollectionPoint = { name: string; address: string; hours: string };

export type BoardPost = {
  id: number;
  kind: string;
  resourceType: string;
  region: string;
  description: string;
  contactPhone: string | null;
  contactName: string | null;
  status: string;
  rawText: string | null;
  factCheck: string | null;
  createdAt: string;
  collectionPoints?: CollectionPoint[];
  riskFlags?: string[];
};

export type Shelter = {
  id: number;
  name: string;
  type: string;
  address: string;
  latitude: number;
  longitude: number;
  accepts: string;
  hours: string | null;
  contactPhone: string | null;
  verified: boolean;
  lastVerified: string | null;
};

export type Sos = {
  id: number;
  status: string;
  latitude: number | null;
  longitude: number | null;
  region: string | null;
  message: string | null;
  contactPhone: string | null;
  createdAt: string;
  handledAt: string | null;
  handledBy: string | null;
};

export type RevertAllResult = { reverted: number; skipped: { entryId: number; reason: string }[] };

// ---- endpoints --------------------------------------------------------------------------------

export const auth = {
  login: (email: string, password: string) =>
    api<{ token: string; role: string }>("/api/auth/login", { method: "POST", auth: false, body: { email, password } }),
  changePassword: (currentPassword: string, newPassword: string) =>
    api<void>("/api/auth/password", { method: "POST", body: { currentPassword, newPassword } }),
  // Self-service account deletion (any non-owner moderator). Current password confirms intent.
  deleteAccount: (currentPassword: string) =>
    api<void>("/api/auth/account/delete", { method: "POST", body: { currentPassword } }),
};

export const audit = {
  list: (params: Record<string, string>) => {
    const qs = new URLSearchParams(params).toString();
    return api<AuditEntry[]>(`/api/audit${qs ? `?${qs}` : ""}`);
  },
  moderators: () => api<ModeratorActivity[]>("/api/audit/moderators"),
  revert: (id: number) => api<void>(`/api/audit/${id}/revert`, { method: "POST" }),
  revertAll: (moderator: string) =>
    api<RevertAllResult>(`/api/audit/revert-all?moderator=${encodeURIComponent(moderator)}`, { method: "POST" }),
};

export const admins = {
  list: () => api<Admin[]>("/api/admins"),
  create: (email: string, password: string) => api<Admin>("/api/admins", { method: "POST", body: { email, password } }),
  remove: (id: number) => api<void>(`/api/admins/${id}`, { method: "DELETE" }),
  disable: (id: number) => api<void>(`/api/admins/${id}/disable`, { method: "POST" }),
  enable: (id: number) => api<void>(`/api/admins/${id}/enable`, { method: "POST" }),
  resetPassword: (id: number, newPassword: string) =>
    api<void>(`/api/admins/${id}/password`, { method: "POST", body: { newPassword } }),
};

export const board = {
  pending: () => api<BoardPost[]>("/api/board/pending"),
  active: () => api<BoardPost[]>("/api/board/active"),
  approve: (id: number) => api<void>(`/api/board/${id}/approve`, { method: "POST" }),
  reject: (id: number) => api<void>(`/api/board/${id}`, { method: "DELETE" }),
};

export type SearchReport = {
  id: number;
  subject: string;
  state: string;
  title: string;
  lastSeen: string;
  description: string;
  contactPhone: string | null;
  contactName: string | null;
  photoId: number | null;
  country: string;
};

export const search = {
  // Public read is ACTIVE-only, scoped by country; delete is admin-gated.
  list: (country: string) => api<SearchReport[]>(`/api/search?country=${country}`),
  remove: (id: number) => api<void>(`/api/search/${id}`, { method: "DELETE" }),
};

export const shelters = {
  list: (country: string) => api<Shelter[]>(`/api/shelters?country=${country}`),
  remove: (id: number) => api<void>(`/api/shelters/${id}`, { method: "DELETE" }),
};

export const sos = {
  list: (archived: string) => api<Sos[]>(`/api/sos?archived=${archived}`),
  handle: (id: number) => api<void>(`/api/sos/${id}/handle`, { method: "POST" }),
  reopen: (id: number) => api<void>(`/api/sos/${id}/reopen`, { method: "POST" }),
  remove: (id: number) => api<void>(`/api/sos/${id}`, { method: "DELETE" }),
};
