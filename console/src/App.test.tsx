import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { afterEach, beforeEach, expect, test, vi } from "vitest";
import { App } from "./App";

// Mock the API responses so we can drive the UI without a backend.
function mockFetch() {
  return vi.fn(async (url: string) => {
    const path = url.toString();
    const json = (data: unknown) =>
      new Response(JSON.stringify(data), { status: 200, headers: { "Content-Type": "application/json" } });
    if (path.includes("/api/audit/moderators")) return json([{ email: "mod@kuna.org", role: "ADMIN", enabled: true, counts: { SHELTER_CREATE: 2 }, lastActiveAt: "2026-08-20T10:00:00" }]);
    if (path.includes("/api/admins")) return json([{ id: 1, email: "mod@kuna.org", role: "ADMIN", enabled: true }]);
    if (path.includes("/api/audit")) return json([]);
    return json([]);
  });
}

beforeEach(() => {
  sessionStorage.setItem("kuna.console.token", "fake-jwt");
  sessionStorage.setItem("kuna.console.role", "SUPERADMIN");
  sessionStorage.setItem("kuna.console.email", "owner@kuna.org");
  vi.stubGlobal("fetch", mockFetch());
});

afterEach(() => {
  sessionStorage.clear();
  vi.unstubAllGlobals();
});

// The bug: switching from the Audit tab (which mounts a useAsync effect) to Moderators crashed the
// panel (blank "blink"). This asserts the switch renders the Moderators content without crashing.
test("switching to the Moderadores tab renders it without blanking", async () => {
  render(<App />);

  // Default super-admin tab is Auditoría.
  expect(await screen.findByText("Auditoría")).toBeTruthy();

  // Click the Moderadores tab.
  fireEvent.click(screen.getByRole("button", { name: "Moderadores" }));

  // The moderators panel must render (its "Nuevo moderador" form + the mocked row), not a blank page.
  await waitFor(() => {
    expect(screen.getByText("Nuevo moderador")).toBeTruthy();
    expect(screen.getByText("mod@kuna.org")).toBeTruthy();
  });
});
