/* eslint-disable */
// Generates screenshots of every page in the running SmartHomie frontend
// and writes them to the directory passed as first CLI argument.
//
// Usage: node take-screenshots.js <output-dir>
// Requires: docker compose up, seed user alice@example.com / password123

const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

const OUT_DIR = process.argv[2] || path.join(__dirname, 'out');
fs.mkdirSync(OUT_DIR, { recursive: true });

const BASE = 'http://localhost:3000';

const AUTHED_PAGES = [
  { slug: '03-dashboard',     url: '/' },
  { slug: '04-rooms',         url: '/rooms' },
  { slug: '05-devices',       url: '/devices' },
  { slug: '06-scenes',        url: '/scenes' },
  { slug: '07-rules',         url: '/rules' },
  { slug: '08-schedules',     url: '/schedules' },
  { slug: '09-energy',        url: '/energy' },
  { slug: '10-activity',      url: '/activity' },
  { slug: '11-notifications', url: '/notifications' },
  { slug: '12-vacation',      url: '/vacation' },
  { slug: '13-members',       url: '/members' },
  { slug: '14-simulation',    url: '/simulation' },
];

async function shoot(page, file) {
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForTimeout(800);
  // fullPage:true → image height equals actual rendered content,
  // no empty whitespace below.
  await page.screenshot({ path: file, fullPage: true, type: 'png' });
}

(async () => {
  const browser = await chromium.launch({ headless: true });
  // deviceScaleFactor:1 → standard web resolution (1280×800),
  // readable PNGs that render correctly in every Markdown viewer including GitHub.
  const ctx = await browser.newContext({
    viewport: { width: 1280, height: 800 },
    deviceScaleFactor: 1,
  });
  const page = await ctx.newPage();

  console.log('01-login');
  await page.goto(BASE + '/login', { waitUntil: 'networkidle' });
  await shoot(page, path.join(OUT_DIR, '01-login.png'));

  console.log('02-register');
  {
    const ctx2 = await browser.newContext({
      viewport: { width: 1280, height: 800 },
      deviceScaleFactor: 1,
    });
    const p2 = await ctx2.newPage();
    await p2.goto(BASE + '/register', { waitUntil: 'networkidle' });
    await shoot(p2, path.join(OUT_DIR, '02-register.png'));
    await ctx2.close();
  }

  // Login as seed owner
  await page.locator('input[type="email"], input[name="email"]').first().fill('alice@example.com');
  await page.locator('input[type="password"]').first().fill('password123');
  await page.locator('button[type="submit"], button:has-text("Anmelden")').first().click();
  await page.waitForURL((url) => !url.toString().includes('/login'), { timeout: 10000 }).catch(() => {});
  await page.waitForLoadState('networkidle').catch(() => {});

  for (const p of AUTHED_PAGES) {
    console.log(p.slug);
    try {
      await page.goto(BASE + p.url, { waitUntil: 'networkidle', timeout: 15000 });
      await shoot(page, path.join(OUT_DIR, p.slug + '.png'));
    } catch (e) {
      console.log('  fail', p.slug, e.message);
    }
  }

  await browser.close();
  console.log('Done. Screenshots in:', OUT_DIR);
})();
