// Real packaged-server check. Build React into backend static resources and package the jar first.
import assert from 'node:assert/strict';
import {spawn} from 'node:child_process';
import {mkdtemp, rm, access} from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import net from 'node:net';
import {setTimeout as delay} from 'node:timers/promises';

const root = fileURLToPath(new URL('../', import.meta.url));
const jar = path.join(root, 'backend/target/preparation-desk.jar');
await access(jar);
const directory = await mkdtemp(path.join(os.tmpdir(), 'desk-http-'));
const socket = net.createServer();
await new Promise(resolve => socket.listen(0, '127.0.0.1', resolve));
const port = socket.address().port;
await new Promise(resolve => socket.close(resolve));
const base = `http://127.0.0.1:${port}`;
let process;
let logs = '';
async function start() {
  logs = '';
  process = spawn('java', ['-Xmx256m', '-XX:+UseSerialGC', '-jar', jar,
    `--server.port=${port}`, '--spring.profiles.active=local',
    `--spring.datasource.url=jdbc:h2:file:${path.join(directory, 'database')};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE`,
    '--app.signup-code=', '--app.require-invite=false'], {cwd: root, stdio: ['ignore', 'pipe', 'pipe']});
  process.stdout.on('data', data => {logs = (logs + data).slice(-20000);});
  process.stderr.on('data', data => {logs = (logs + data).slice(-20000);});
  for (let i = 0; i < 120; i++) {
    if (process.exitCode !== null) throw new Error('Server exited during startup:\n' + logs);
    try {if ((await fetch(base + '/api/health')).ok) return;} catch {}
    await delay(250);
  }
  throw new Error('Server did not become healthy:\n' + logs);
}
async function stop() {
  if (!process || process.exitCode !== null || process.signalCode !== null) return;
  const exited = new Promise(resolve => process.once('exit', resolve));
  process.kill('SIGTERM');
  const timeout = setTimeout(() => process.kill('SIGKILL'), 10000);
  try {await exited;} finally {clearTimeout(timeout);}
}
function client() {
  const cookies = new Map();
  return async function request(method, route, body, useToken = true) {
    const headers = {};
    if (!['GET', 'HEAD'].includes(method) && useToken) {
      const token = await request('GET', '/api/auth/csrf');
      headers[token.data.headerName] = token.data.token;
    }
    if (cookies.size) headers.Cookie = [...cookies].map(([key, value]) => `${key}=${value}`).join('; ');
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    const response = await fetch(base + route, {method, headers, body: body === undefined ? undefined : JSON.stringify(body)});
    for (const cookie of response.headers.getSetCookie()) {
      const pair = cookie.split(';')[0]; const at = pair.indexOf('=');
      const key = pair.slice(0, at), value = pair.slice(at + 1);
      if (value) cookies.set(key, value); else cookies.delete(key);
    }
    const text = await response.text();
    return {response, data: response.headers.get('content-type')?.includes('application/json') && text ? JSON.parse(text) : text};
  };
}
const credentials = {email: 'http-check@example.test', password: 'unique-http-test-password'};
try {
  await start();
  const anonymous = client();
  assert.equal((await anonymous('GET', '/api/planner')).response.status, 401);
  const home = await anonymous('GET', '/');
  assert.equal(home.response.status, 200, 'Package the frontend into the jar before this test.');
  assert.match(home.data, /id="root"/);
  const script = home.data.match(/src="([^\"]+\.js)"/)[1];
  assert.equal((await anonymous('GET', script)).response.status, 200);
  assert.match(home.response.headers.get('content-security-policy'), /frame-ancestors 'none'/);
  assert.equal((await anonymous('POST', '/api/auth/register', {...credentials, fullName: 'HTTP Learner'}, false)).response.status, 403);
  const a = client();
  assert.equal((await a('POST', '/api/auth/register', {...credentials, fullName: 'HTTP Learner'})).response.status, 201);
  const signedIn = await a('POST', '/api/auth/login', credentials);
  assert.equal(signedIn.response.status, 200);
  const cookie = signedIn.response.headers.getSetCookie().find(v => v.startsWith('DESKSESSION='));
  assert.match(cookie, /HttpOnly/i); assert.match(cookie, /SameSite=Lax/i);
  assert.equal((await a('PUT', '/api/profile', {fullName: 'परीक्षार्थी', city: 'Delhi', bio: 'Daily study', targetYear: null})).response.status, 200);
  assert.equal((await a('PATCH', '/api/planner', {field: 'progress', patch: {'polity-1': 'complete'}})).response.status, 200);
  assert.equal((await a('PATCH', '/api/planner', {field: 'settings', patch: {practiceDrafts: {ethics: 'ईमानदारी और सार्वजनिक सेवा'}}})).response.status, 200);
  const b = client();
  assert.equal((await b('POST', '/api/auth/register', {email: 'other-http@example.test', password: credentials.password, fullName: 'Other Learner'})).response.status, 201);
  assert.equal((await b('POST', '/api/auth/login', {email: 'other-http@example.test', password: credentials.password})).response.status, 200);
  assert.deepEqual((await b('GET', '/api/planner')).data.progress, {});
  assert.equal((await a('POST', '/api/auth/logout')).response.status, 204);
  assert.equal((await a('GET', '/api/planner')).response.status, 401);
  await stop(); await start();
  const restored = client();
  assert.equal((await restored('POST', '/api/auth/login', credentials)).response.status, 200);
  assert.equal((await restored('GET', '/api/auth/me')).data.fullName, 'परीक्षार्थी');
  const saved = (await restored('GET', '/api/planner')).data;
  assert.equal(saved.progress['polity-1'], 'complete');
  assert.equal(saved.settings.practiceDrafts.ethics, 'ईमानदारी और सार्वजनिक सेवा');
  console.log('PASS: packaged React assets, real HTTP register/login/profile/logout, cookies, CSRF, two-user isolation and saved data after server restart.');
} finally {await stop(); await rm(directory, {recursive: true, force: true});}
