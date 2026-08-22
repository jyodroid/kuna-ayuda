import { useEffect, useState } from "react";
import { admins, audit, auth, board, search, session, shelters, sos } from "./api";
import type { Admin, AuditEntry, BoardPost, ModeratorActivity, SearchReport, Shelter, Sos } from "./api";

type Tab = "audit" | "moderators" | "moderation" | "password";

export function App() {
  const [loggedIn, setLoggedIn] = useState(!!session.token());

  useEffect(() => {
    const onUnauth = () => setLoggedIn(false);
    window.addEventListener("kuna-unauthorized", onUnauth);
    return () => window.removeEventListener("kuna-unauthorized", onUnauth);
  }, []);

  if (!loggedIn) return <Login onLogin={() => setLoggedIn(true)} />;
  return <Shell onSignOut={() => { session.clear(); setLoggedIn(false); }} />;
}

// ---- login ------------------------------------------------------------------------------------

function Login({ onLogin }: { onLogin: () => void }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const r = await auth.login(email, password);
      session.set(r.token, r.role, email.trim().toLowerCase());
      onLogin();
    } catch (err: any) {
      setError(err?.message ?? "No se pudo iniciar sesión");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="min-h-full flex items-center justify-center p-4">
      <form onSubmit={submit} className="w-full max-w-sm bg-white rounded-xl shadow p-6 space-y-4">
        <div className="text-center">
          <h1 className="text-xl font-bold text-primary">Kuna · Consola</h1>
          <p className="text-sm text-neutral-600">Acceso de moderadores</p>
        </div>
        {error && <p className="text-sm text-danger bg-danger-muted rounded p-2">{error}</p>}
        <input className="input" type="email" placeholder="Correo" value={email} autoFocus
          onChange={(e) => setEmail(e.target.value)} />
        <PasswordInput value={password} onChange={setPassword} placeholder="Contraseña" />
        <button className="btn-primary w-full" disabled={busy}>{busy ? "Entrando…" : "Entrar"}</button>
      </form>
    </div>
  );
}

// ---- shell ------------------------------------------------------------------------------------

function Shell({ onSignOut }: { onSignOut: () => void }) {
  const isSuper = session.isSuperAdmin();
  const [tab, setTab] = useState<Tab>(isSuper ? "audit" : "password");

  const tabs: { id: Tab; label: string }[] = isSuper
    ? [
        { id: "audit", label: "Auditoría" },
        { id: "moderators", label: "Moderadores" },
        { id: "moderation", label: "Moderación" },
        { id: "password", label: "Mi contraseña" },
      ]
    : [{ id: "password", label: "Mi contraseña" }];

  return (
    <div className="min-h-full">
      <header className="bg-primary text-white">
        <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
          <span className="font-semibold">Kuna · Consola</span>
          <div className="flex items-center gap-3 text-sm">
            <span className="opacity-90">{session.email()} · {session.role()}</span>
            <button className="underline" onClick={onSignOut}>Salir</button>
          </div>
        </div>
      </header>
      <nav className="bg-white border-b">
        <div className="max-w-6xl mx-auto px-4 flex gap-1">
          {tabs.map((t) => (
            <button key={t.id} onClick={() => setTab(t.id)}
              className={`px-4 py-3 text-sm font-medium border-b-2 ${tab === t.id ? "border-primary text-primary" : "border-transparent text-neutral-600"}`}>
              {t.label}
            </button>
          ))}
        </div>
      </nav>
      <main className="max-w-6xl mx-auto p-4">
        {tab === "audit" && <AuditPanel />}
        {tab === "moderators" && <ModeratorsPanel />}
        {tab === "moderation" && <ModerationPanel />}
        {tab === "password" && <PasswordPanel />}
      </main>
    </div>
  );
}

// ---- shared bits ------------------------------------------------------------------------------

function useAsync<T>(fn: () => Promise<T>, deps: any[] = []) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const reload = () => {
    setLoading(true);
    fn().then((d) => { setData(d); setError(""); })
      .catch((e) => setError(e?.message ?? "Error"))
      .finally(() => setLoading(false));
  };
  // NOTE: the effect must return undefined (or a cleanup fn) — never the Promise from reload(), or
  // React tries to invoke it as a cleanup on unmount and crashes the panel (blank "blink" on tab switch).
  useEffect(() => { reload(); }, deps);
  return { data, error, loading, reload };
}

function Msg({ error, loading }: { error: string; loading: boolean }) {
  if (loading) return <p className="text-neutral-500 text-sm">Cargando…</p>;
  if (error) return <p className="text-danger text-sm">{error}</p>;
  return null;
}

/** Password field with a show/hide eye — grey when hidden, red (open eye) when the password is visible. */
function PasswordInput({ value, onChange, placeholder, autoFocus }: {
  value: string; onChange: (v: string) => void; placeholder: string; autoFocus?: boolean;
}) {
  const [show, setShow] = useState(false);
  return (
    <div className="relative">
      <input
        className="input w-full pr-10"
        type={show ? "text" : "password"}
        placeholder={placeholder}
        value={value}
        autoFocus={autoFocus}
        onChange={(e) => onChange(e.target.value)}
        required
      />
      <button
        type="button"
        onClick={() => setShow((s) => !s)}
        aria-label={show ? "Ocultar contraseña" : "Mostrar contraseña"}
        title={show ? "Ocultar" : "Mostrar"}
        className={`absolute right-2 top-1/2 -translate-y-1/2 ${show ? "text-danger" : "text-neutral-400"} hover:opacity-80`}
      >
        {show ? (
          // open eye (visible → red)
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
        ) : (
          // eye with a slash (hidden → grey)
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M9.9 4.24A9.1 9.1 0 0 1 12 4c6.5 0 10 7 10 7a13.2 13.2 0 0 1-1.67 2.68" />
            <path d="M6.1 6.1A13.3 13.3 0 0 0 2 11s3.5 7 10 7a9 9 0 0 0 5-1.4" />
            <path d="m1 1 22 22" />
          </svg>
        )}
      </button>
    </div>
  );
}

// ---- audit ------------------------------------------------------------------------------------

const ACTIONS = [
  "", "SHELTER_CREATE", "SHELTER_UPDATE", "SHELTER_DELETE", "BOARD_APPROVE", "BOARD_REJECT",
  "SOS_HANDLE", "SOS_REOPEN", "SOS_DELETE", "SEARCH_DELETE", "ADMIN_CREATE", "ADMIN_DELETE",
  "ADMIN_DISABLE", "ADMIN_ENABLE", "PASSWORD_CHANGE", "PASSWORD_RESET", "LOGIN_SUCCESS", "LOGIN_FAILURE", "REVERT",
];

function AuditPanel() {
  const [actor, setActor] = useState("");
  const [action, setAction] = useState("");
  const [reverted, setReverted] = useState("");
  const [detail, setDetail] = useState<AuditEntry | null>(null);
  const { data, error, loading, reload } = useAsync<AuditEntry[]>(
    () => audit.list({ ...(actor && { actor }), ...(action && { action }), ...(reverted && { reverted }) }),
    [actor, action, reverted],
  );

  const revert = async (e: AuditEntry) => {
    if (!confirm(`¿Revertir la acción ${e.action} #${e.id}?`)) return;
    try { await audit.revert(e.id); reload(); }
    catch (err: any) { alert(err?.message ?? "No se pudo revertir"); }
  };

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap gap-2 items-end">
        <input className="input w-56" placeholder="Filtrar por correo" value={actor} onChange={(e) => setActor(e.target.value)} />
        <select className="input w-56" value={action} onChange={(e) => setAction(e.target.value)}>
          {ACTIONS.map((a) => <option key={a} value={a}>{a || "Todas las acciones"}</option>)}
        </select>
        <select className="input w-40" value={reverted} onChange={(e) => setReverted(e.target.value)}>
          <option value="">Todas</option>
          <option value="false">Sin revertir</option>
          <option value="true">Revertidas</option>
        </select>
      </div>
      <Msg error={error} loading={loading} />
      <div className="overflow-x-auto bg-white rounded-lg shadow">
        <table className="min-w-full text-sm">
          <thead className="bg-neutral-100 text-left text-neutral-600">
            <tr>
              <th className="p-2">Fecha</th><th className="p-2">Moderador</th><th className="p-2">Acción</th>
              <th className="p-2">Entidad</th><th className="p-2">Estado</th><th className="p-2"></th>
            </tr>
          </thead>
          <tbody>
            {data?.map((e) => (
              <tr key={e.id} className="border-t">
                <td className="p-2 whitespace-nowrap">{e.createdAt.replace("T", " ").slice(0, 19)}</td>
                <td className="p-2">{e.actorEmail}</td>
                <td className="p-2 font-mono text-xs">{e.action}</td>
                <td className="p-2">{e.entityType}{e.entityId ? ` #${e.entityId}` : ""}</td>
                <td className="p-2">{e.revertedAt ? <span className="text-neutral-400">revertida</span> : "—"}</td>
                <td className="p-2 whitespace-nowrap text-right">
                  {(e.beforeJson || e.afterJson) && (
                    <button className="text-primary underline mr-3" onClick={() => setDetail(e)}>Ver</button>
                  )}
                  {!e.revertedAt && (
                    <button className="text-danger underline" onClick={() => revert(e)}>Revertir</button>
                  )}
                </td>
              </tr>
            ))}
            {data && data.length === 0 && <tr><td className="p-3 text-neutral-500" colSpan={6}>Sin registros.</td></tr>}
          </tbody>
        </table>
      </div>
      {detail && <DiffModal entry={detail} onClose={() => setDetail(null)} />}
    </div>
  );
}

function DiffModal({ entry, onClose }: { entry: AuditEntry; onClose: () => void }) {
  const fmt = (s: string | null) => {
    if (!s) return "—";
    try { return JSON.stringify(JSON.parse(s), null, 2); } catch { return s; }
  };
  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4" onClick={onClose}>
      <div className="bg-white rounded-lg shadow max-w-3xl w-full max-h-[80vh] overflow-auto p-4" onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between mb-2">
          <h3 className="font-semibold">{entry.action} · {entry.entityType} #{entry.entityId}</h3>
          <button onClick={onClose} className="text-neutral-500">✕</button>
        </div>
        <div className="grid md:grid-cols-2 gap-3">
          <div><p className="text-xs font-semibold text-neutral-500 mb-1">ANTES</p>
            <pre className="text-xs bg-neutral-50 p-2 rounded overflow-auto">{fmt(entry.beforeJson)}</pre></div>
          <div><p className="text-xs font-semibold text-neutral-500 mb-1">DESPUÉS</p>
            <pre className="text-xs bg-neutral-50 p-2 rounded overflow-auto">{fmt(entry.afterJson)}</pre></div>
        </div>
      </div>
    </div>
  );
}

// ---- moderators -------------------------------------------------------------------------------

function ModeratorsPanel() {
  const { data, error, loading, reload } = useAsync<ModeratorActivity[]>(() => audit.moderators(), []);
  const adminList = useAsync<Admin[]>(() => admins.list(), []);
  const [newEmail, setNewEmail] = useState("");
  const [newPass, setNewPass] = useState("");

  const idByEmail = (email: string) => adminList.data?.find((a) => a.email === email)?.id;

  const act = async (fn: () => Promise<unknown>, confirmMsg?: string) => {
    if (confirmMsg && !confirm(confirmMsg)) return;
    try { await fn(); reload(); adminList.reload(); }
    catch (err: any) { alert(err?.message ?? "Error"); }
  };

  const createAdmin = async (e: React.FormEvent) => {
    e.preventDefault();
    await act(() => admins.create(newEmail, newPass));
    setNewEmail(""); setNewPass("");
  };

  return (
    <div className="space-y-4">
      <Msg error={error || adminList.error} loading={loading} />
      <div className="overflow-x-auto bg-white rounded-lg shadow">
        <table className="min-w-full text-sm">
          <thead className="bg-neutral-100 text-left text-neutral-600">
            <tr><th className="p-2">Correo</th><th className="p-2">Rol</th><th className="p-2">Estado</th>
              <th className="p-2">Actividad</th><th className="p-2">Última</th><th className="p-2"></th></tr>
          </thead>
          <tbody>
            {data?.map((m) => {
              const id = idByEmail(m.email);
              const total = Object.values(m.counts).reduce((a, b) => a + b, 0);
              return (
                <tr key={m.email} className="border-t align-top">
                  <td className="p-2">{m.email}</td>
                  <td className="p-2">{m.role}</td>
                  <td className="p-2">{m.enabled ? "activo" : <span className="text-danger">deshabilitado</span>}</td>
                  <td className="p-2 text-xs">{total} acciones
                    <div className="text-neutral-500">{Object.entries(m.counts).map(([k, v]) => `${k}:${v}`).join("  ")}</div>
                  </td>
                  <td className="p-2 whitespace-nowrap text-xs">{m.lastActiveAt?.replace("T", " ").slice(0, 16) ?? "—"}</td>
                  <td className="p-2 text-right whitespace-nowrap space-x-2">
                    <button className="text-danger underline"
                      onClick={() => act(() => audit.revertAll(m.email).then((r) => alert(`Revertidas ${r.reverted}, omitidas ${r.skipped.length}`)),
                        `¿Revertir TODAS las acciones de ${m.email}?`)}>Revertir todo</button>
                    {id != null && m.role !== "SUPERADMIN" && (m.enabled
                      ? <button className="text-neutral-700 underline" onClick={() => act(() => admins.disable(id), `¿Deshabilitar a ${m.email}?`)}>Deshabilitar</button>
                      : <button className="text-primary underline" onClick={() => act(() => admins.enable(id))}>Habilitar</button>)}
                    {id != null && (
                      <button className="text-primary underline" onClick={() => {
                        const p = prompt(`Nueva contraseña para ${m.email} (mín. 12):`);
                        if (p) act(() => admins.resetPassword(id, p));
                      }}>Restablecer clave</button>
                    )}
                    {id != null && m.role !== "SUPERADMIN" && (
                      <button className="text-danger underline" onClick={() => act(() => admins.remove(id), `¿Eliminar a ${m.email}?`)}>Eliminar</button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      <form onSubmit={createAdmin} className="bg-white rounded-lg shadow p-4 flex flex-wrap gap-2 items-end">
        <div><label className="label">Nuevo moderador</label>
          <input className="input w-56" type="email" placeholder="correo" value={newEmail} onChange={(e) => setNewEmail(e.target.value)} required /></div>
        <input className="input w-48" type="password" placeholder="contraseña (mín. 12)" value={newPass} onChange={(e) => setNewPass(e.target.value)} required />
        <button className="btn-primary">Crear</button>
      </form>
    </div>
  );
}

// ---- moderation -------------------------------------------------------------------------------

function ModerationPanel() {
  const [sub, setSub] = useState<"board" | "search" | "shelters" | "sos">("board");
  const label = (s: string) =>
    s === "board" ? "Red de ayuda" : s === "search" ? "Búsqueda" : s === "shelters" ? "Puntos de ayuda" : "SOS";
  return (
    <div className="space-y-3">
      <div className="flex gap-2">
        {(["board", "search", "shelters", "sos"] as const).map((s) => (
          <button key={s} onClick={() => setSub(s)}
            className={`px-3 py-1.5 rounded text-sm ${sub === s ? "bg-primary text-white" : "bg-white border"}`}>
            {label(s)}
          </button>
        ))}
      </div>
      {sub === "board" && <BoardMod />}
      {sub === "search" && <SearchMod />}
      {sub === "shelters" && <ShelterMod />}
      {sub === "sos" && <SosMod />}
    </div>
  );
}

function riskFlagLabel(code: string): string {
  switch (code) {
    case "ASKS_FOR_MONEY": return "Pide dinero";
    case "UNVERIFIED_CLAIM": return "Sin verificar";
    case "NO_SOURCE": return "Sin fuente";
    default: return code;
  }
}

function BoardMod() {
  const [view, setView] = useState<"pending" | "active">("pending");
  const { data, error, loading, reload } = useAsync<BoardPost[]>(
    () => (view === "pending" ? board.pending() : board.active()), [view]);
  const act = async (fn: () => Promise<unknown>) => { try { await fn(); reload(); } catch (e: any) { alert(e?.message); } };
  return (
    <div className="space-y-2">
      <div className="flex gap-2">
        {(["pending", "active"] as const).map((v) => (
          <button key={v} onClick={() => setView(v)}
            className={`px-3 py-1 rounded text-sm ${view === v ? "bg-primary text-white" : "bg-white border"}`}>
            {v === "pending" ? "Pendientes" : "Publicados"}
          </button>
        ))}
      </div>
      <Msg error={error} loading={loading} />
      {data?.length === 0 && (
        <p className="text-neutral-500 text-sm">{view === "pending" ? "Nada pendiente." : "Sin publicaciones activas."}</p>
      )}
      {data?.map((p) => (
        <div key={p.id} className="bg-white rounded-lg shadow p-3">
          <div className="text-sm font-medium">{p.kind} · {p.resourceType} · {p.region}</div>
          <div className="text-sm text-neutral-700">{p.description}</div>
          {p.collectionPoints && p.collectionPoints.length > 0 && (
            <div className="mt-1 text-sm">
              <div className="font-medium text-primary">Puntos de recepción</div>
              <ul className="list-disc pl-5 text-neutral-700">
                {p.collectionPoints.map((c, i) => (
                  <li key={i}>{[c.name, c.address, c.hours].filter(Boolean).join(" · ")}</li>
                ))}
              </ul>
            </div>
          )}
          {p.rawText && <div className="text-xs text-neutral-500 mt-1">Original: {p.rawText}</div>}
          {p.factCheck && <div className="text-xs text-danger mt-1">Fact-check: {p.factCheck}</div>}
          {p.riskFlags && p.riskFlags.length > 0 && (
            <div className="text-xs text-danger mt-1">Señales: {p.riskFlags.map(riskFlagLabel).join(" · ")}</div>
          )}
          <div className="mt-2 space-x-3">
            {view === "pending" && (
              <button className="btn-primary" onClick={() => act(() => board.approve(p.id))}>Aprobar</button>
            )}
            <button className="text-danger underline"
              onClick={() => confirm("¿Eliminar esta publicación?") && act(() => board.reject(p.id))}>
              {view === "pending" ? "Rechazar" : "Eliminar"}
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}

function SearchMod() {
  const [country, setCountry] = useState("CO");
  const { data, error, loading, reload } = useAsync<SearchReport[]>(() => search.list(country), [country]);
  const act = async (fn: () => Promise<unknown>) => { try { await fn(); reload(); } catch (e: any) { alert(e?.message); } };
  return (
    <div className="space-y-2">
      <select className="input w-32" value={country} onChange={(e) => setCountry(e.target.value)}>
        {["CO", "ID", "ES", "IT", "PE"].map((c) => <option key={c}>{c}</option>)}
      </select>
      <Msg error={error} loading={loading} />
      {data?.length === 0 && <p className="text-neutral-500 text-sm">Sin reportes.</p>}
      {data?.map((r) => (
        <div key={r.id} className="bg-white rounded-lg shadow p-3">
          <div className="text-sm font-medium">{r.subject} · {r.state} · {r.title}</div>
          <div className="text-sm text-neutral-700">{r.lastSeen}</div>
          {r.description && <div className="text-sm text-neutral-700">{r.description}</div>}
          <div className="mt-2">
            <button className="text-danger underline"
              onClick={() => confirm(`¿Eliminar "${r.title}"?`) && act(() => search.remove(r.id))}>Eliminar</button>
          </div>
        </div>
      ))}
    </div>
  );
}

function ShelterMod() {
  const [country, setCountry] = useState("CO");
  const { data, error, loading, reload } = useAsync<Shelter[]>(() => shelters.list(country), [country]);
  const act = async (fn: () => Promise<unknown>) => { try { await fn(); reload(); } catch (e: any) { alert(e?.message); } };
  return (
    <div className="space-y-2">
      <select className="input w-32" value={country} onChange={(e) => setCountry(e.target.value)}>
        {["CO", "ID", "ES", "IT", "PE"].map((c) => <option key={c}>{c}</option>)}
      </select>
      <Msg error={error} loading={loading} />
      <div className="overflow-x-auto bg-white rounded-lg shadow">
        <table className="min-w-full text-sm">
          <thead className="bg-neutral-100 text-left text-neutral-600"><tr>
            <th className="p-2">Nombre</th><th className="p-2">Tipo</th><th className="p-2">Dirección</th><th className="p-2"></th></tr></thead>
          <tbody>
            {data?.map((s) => (
              <tr key={s.id} className="border-t">
                <td className="p-2">{s.name}</td><td className="p-2">{s.type}</td><td className="p-2">{s.address}</td>
                <td className="p-2 text-right"><button className="text-danger underline"
                  onClick={() => confirm(`¿Eliminar "${s.name}"?`) && act(() => shelters.remove(s.id))}>Eliminar</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function SosMod() {
  const [archived, setArchived] = useState("false");
  const { data, error, loading, reload } = useAsync<Sos[]>(() => sos.list(archived), [archived]);
  const act = async (fn: () => Promise<unknown>) => { try { await fn(); reload(); } catch (e: any) { alert(e?.message); } };
  return (
    <div className="space-y-2">
      <select className="input w-40" value={archived} onChange={(e) => setArchived(e.target.value)}>
        <option value="false">Activos</option><option value="true">Archivados</option><option value="all">Todos</option>
      </select>
      <Msg error={error} loading={loading} />
      {data?.map((r) => (
        <div key={r.id} className={`rounded-lg shadow p-3 ${r.status === "SOS" ? "bg-danger-muted" : "bg-white"}`}>
          <div className="text-sm font-medium">{r.status} · {r.region ?? "sin región"} · {r.createdAt.replace("T", " ").slice(0, 16)}</div>
          {r.message && <div className="text-sm">{r.message}</div>}
          {r.latitude != null && <a className="text-primary underline text-xs" target="_blank"
            href={`https://www.openstreetmap.org/?mlat=${r.latitude}&mlon=${r.longitude}#map=15/${r.latitude}/${r.longitude}`}>Ver en el mapa</a>}
          <div className="mt-2 space-x-3">
            {!r.handledAt
              ? <button className="btn-primary" onClick={() => act(() => sos.handle(r.id))}>Atendido</button>
              : <><span className="text-xs text-neutral-500">Atendido por {r.handledBy}</span>
                  <button className="text-primary underline" onClick={() => act(() => sos.reopen(r.id))}>Restaurar</button></>}
            <button className="text-danger underline" onClick={() => confirm("¿Eliminar permanentemente?") && act(() => sos.remove(r.id))}>Eliminar</button>
          </div>
        </div>
      ))}
    </div>
  );
}

// ---- password ---------------------------------------------------------------------------------

function PasswordPanel() {
  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [confirmPw, setConfirmPw] = useState("");
  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsg(""); setErr("");
    if (next !== confirmPw) { setErr("Las contraseñas nuevas no coinciden"); return; }
    if (next.length < 12) { setErr("Mínimo 12 caracteres"); return; }
    try {
      await auth.changePassword(current, next);
      setMsg("Contraseña actualizada.");
      setCurrent(""); setNext(""); setConfirmPw("");
    } catch (e: any) { setErr(e?.message ?? "No se pudo cambiar"); }
  };

  return (
    <form onSubmit={submit} className="max-w-sm bg-white rounded-lg shadow p-6 space-y-3">
      <h2 className="font-semibold">Cambiar mi contraseña</h2>
      {msg && <p className="text-sm text-primary bg-primary-muted rounded p-2">{msg}</p>}
      {err && <p className="text-sm text-danger bg-danger-muted rounded p-2">{err}</p>}
      <PasswordInput value={current} onChange={setCurrent} placeholder="Contraseña actual" />
      <PasswordInput value={next} onChange={setNext} placeholder="Nueva contraseña (mín. 12)" />
      <PasswordInput value={confirmPw} onChange={setConfirmPw} placeholder="Repetir nueva contraseña" />
      <button className="btn-primary w-full">Actualizar</button>
    </form>
  );
}
