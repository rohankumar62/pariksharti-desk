type Csrf = {headerName: string; token: string};
let token: Promise<Csrf> | undefined;
export class ApiError extends Error {
  constructor(public status: number, public code: string, message: string) { super(message); }
}
export function resetCsrf() { token = undefined; }
async function csrf(): Promise<Csrf> {
  token ??= fetch('/api/auth/csrf', {credentials: 'same-origin', cache: 'no-store'})
    .then(async r => { if (!r.ok) throw new Error('Unable to connect to the server.'); return r.json(); })
    .catch(e => { token = undefined; throw e; });
  return token;
}
export async function api<T>(url: string, method = 'GET', body?: unknown): Promise<T> {
  const headers: Record<string, string> = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const c = await csrf(); headers[c.headerName] = c.token;
  }
  let r: Response;
  try { r = await fetch(url, {method, headers, credentials: 'same-origin', cache: 'no-store', body: body === undefined ? undefined : JSON.stringify(body)}); }
  catch { throw new Error('Unable to connect. Check that the backend is running.'); }
  const data = r.status === 204 ? undefined : await r.json();
  if (!r.ok) {
    if (r.status === 403) resetCsrf();
    if (r.status === 401 && data?.code === 'AUTH_REQUIRED') window.dispatchEvent(new Event('desk-session-expired'));
    throw new ApiError(r.status, data?.code ?? 'REQUEST_FAILED', data?.error ?? 'Request failed. Please retry.');
  }
  return data as T;
}
