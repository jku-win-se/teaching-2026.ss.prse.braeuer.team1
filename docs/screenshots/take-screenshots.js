/* eslint-disable */
const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

const OUT_DIR = process.argv[2] || path.join(__dirname, 'out');
fs.mkdirSync(OUT_DIR, { recursive: true });

const BASE = 'http://localhost:3000';

const PAGES = [
  { slug: '01-login',         url: '/login',         waitFor: 'text=/E-?Mail|Login|Anmelden|Email/i' },
  { slug: '02-register',      url: '/register',      waitFor: 'text=/Registrier|Register/i' },
  { slug: '03-dashboard',     url: '/',              waitFor: 'main', requiresAuth: true },
  { slug: '04-rooms',         url: '/rooms',         waitFor: 'main', requiresAuth: true },
  { slug: '05-devices',       url: '/devices',       waitFor: 'main', requiresAuth: true },
  { slug: '06-scenes',        url: '/scenes',        waitFor: 'main', requiresAuth: true },
  { slug: '07-rules',         url: '/rules',         waitFor: 'main', requiresAuth: true },
  { slug: '08-schedules',     url: '/schedules',     waitFor: 'main', requiresAuth: true },
  { slug: '09-energy',        url: '/energy',        waitFor: 'main', requiresAuth: true },
  { slug: '10-activity',      url: '/activity',      waitFor: 'main', requiresAuth: true },
  { slug: '11-notifications', url: '/notifications', waitFor: 'main', requiresAuth: true },
  { slug: '12-vacation',      url: '/vacation',      waitFor: 'main', requiresAuth: true },
  { slug: '13-members',       url: '/members',       waitFor: 'main', requiresAuth: true },
  { slug: '14-simulation',    url: '/simulation',    waitFor: 'main', requiresAuth: true },
];

(async () => {
  const browser = await chromium.launch({ headless: true });
  const ctx = await browser.newContext({
    viewport: { width: 1440, height: 900 },
    deviceScaleFactor: 2,
  });
  const page = await ctx.newPage();

  // Login flow with seeded owner
  console.log('Login flow...');
  await page.goto(BASE + '/login', { waitUntil: 'networkidle' });
  // Capture login screen first (before filling)
  await page.screenshot({ path: path.join(OUT_DIR, '01-login.png'), fullPage: false });

  // Try to find email/password inputs flexibly
  const emailInput = page.locator('input[type="email"], input[name="email"], input[placeholder*="ail" i]').first();
  const passwordInput = page.locator('input[type="password"]').first();
  await emailInput.waitFor({ timeout: 8000 });
  await emailInput.fill('alice@example.com');
  await passwordInput.fill('password123');
  // Try to submit
  const submitBtn = page.locator('button[type="submit"], button:has-text("Login"), button:has-text("Anmelden"), button:has-text("Sign in")').first();
  await submitBtn.click();
  await page.waitForURL((url) => !url.toString().includes('/login'), { timeout: 10000 }).catch(() => {});
  await page.waitForLoadState('networkidle').catch(() => {});

  for (const p of PAGES.slice(1)) {
    if (p.url === '/register') {
      // Open register without auth
      const ctx2 = await browser.newContext({
        viewport: { width: 1440, height: 900 },
        deviceScaleFactor: 2,
      });
      const page2 = await ctx2.newPage();
      try {
        await page2.goto(BASE + p.url, { waitUntil: 'networkidle' });
        await page2.waitForTimeout(800);
        await page2.screenshot({ path: path.join(OUT_DIR, p.slug + '.png'), fullPage: false });
        console.log('  ✓', p.slug);
      } catch (e) {
        console.log('  ✗', p.slug, e.message);
      }
      await ctx2.close();
      continue;
    }
    try {
      await page.goto(BASE + p.url, { waitUntil: 'networkidle', timeout: 15000 });
      await page.waitForTimeout(1500);
      await page.screenshot({ path: path.join(OUT_DIR, p.slug + '.png'), fullPage: false });
      console.log('  ✓', p.slug);
    } catch (e) {
      console.log('  ✗', p.slug, e.message);
    }
  }

  await browser.close();
  console.log('Done. Screenshots in:', OUT_DIR);
})();
