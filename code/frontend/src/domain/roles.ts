export function normalizeRole(role: string | null | undefined): string {
  return String(role ?? "").trim().toUpperCase();
}

export function canModerateRole(role: string | null | undefined): boolean {
  const normalized = normalizeRole(role);
  return normalized === "MODERATOR" || normalized === "ADMIN";
}

export function isAdminRole(role: string | null | undefined): boolean {
  return normalizeRole(role) === "ADMIN";
}
