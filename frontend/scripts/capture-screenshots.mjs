// One-off doc tool: drives the running dev app (Vite :5173 + API :8080) through the
// product story and saves screenshots into docs/screenshots for the README.
// Run with the app already up:  node scripts/capture-screenshots.mjs
import { chromium } from "playwright";
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";

const __dirname = dirname(fileURLToPath(import.meta.url));
const OUT = resolve(__dirname, "../../docs/screenshots");
const BASE = "http://localhost:5173";

// The story, in order. Each entry is a route + filename; some wait for a known cell.
const SHOTS = [
  { file: "01-login.png", path: "/login", waitFor: "text=Sign in to continue" },
  { file: "02-home.png", path: "/", waitFor: "text=Welcome back" },
  { file: "03-finance.png", path: "/finance", waitFor: "text=AR outstanding" },
  { file: "04-finance-invoices.png", path: "/finance/invoices" },
  { file: "05-hr.png", path: "/hr" },
  { file: "06-hr-roll-call.png", path: "/hr/roll-call" },
  { file: "07-sales.png", path: "/sales" },
  { file: "08-sales-reps.png", path: "/sales/reps" },
  { file: "09-projects.png", path: "/projects" },
  { file: "10-users.png", path: "/users" },
];

const wait = (ms) => new Promise((r) => setTimeout(r, ms));

const browser = await chromium.launch({ channel: "chrome" });
const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 }, deviceScaleFactor: 2 });
const page = await ctx.newPage();

// Sign in through the real login form so the JWT lands in localStorage.
async function signIn() {
  await page.goto(`${BASE}/login`, { waitUntil: "networkidle" });
  await page.locator('input:not([type="password"])').first().fill("admin");
  await page.fill('input[type="password"]', "admin123");
  await page.click('button:has-text("Sign in")');
  await page.waitForURL(`${BASE}/`, { timeout: 10_000 });
}
await signIn();

for (const shot of SHOTS) {
  // For the login shot, sign out of the session view by clearing the token first.
  if (shot.path === "/login") {
    await page.evaluate(() => localStorage.removeItem("simpleerp.token"));
  }
  await page.goto(`${BASE}${shot.path}`, { waitUntil: "networkidle" });
  if (shot.waitFor) {
    try { await page.waitForSelector(shot.waitFor, { timeout: 8_000 }); } catch { /* best effort */ }
  }
  await wait(700); // let polling queries settle / numbers paint
  await page.screenshot({ path: resolve(OUT, shot.file), fullPage: false });
  console.log("captured", shot.file);
  // Re-auth after the login shot so the rest of the story renders.
  if (shot.path === "/login") await signIn();
}

await browser.close();
console.log("done →", OUT);
