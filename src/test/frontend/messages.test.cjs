// Run with Node.js and Playwright installed: node --test src/test/frontend/messages.test.cjs
const { test } = require("node:test");
const assert = require("node:assert/strict");
const path = require("node:path");
const { chromium } = require("playwright");

test("CityFix exposes status updates and errors through separate live regions", async t => {
    const browser = await chromium.launch({ channel: "chrome" });
    t.after(() => browser.close());
    const page = await browser.newPage();
    const client = await page.context().newCDPSession(page);
    const root = path.resolve(__dirname, "../../main/resources/static");
    const report = {
        id: 1, title: "Streetlight", status: "REPORTED", category: "OTHER",
        area: "Park", upvoteCount: 1
    };
    let failRequest = false;
    await page.route("http://cityfix.test/**", async route => {
        const url = new URL(route.request().url());
        if (!url.pathname.startsWith("/api/")) {
            return route.fulfill({ path: path.join(root, url.pathname) });
        }
        if (failRequest) {
            return route.fulfill({ status: 400, json: { message: "Request failed" } });
        }
        const responses = {
            "/api/reports": [report],
            "/api/reports/stats": { byStatus: { REPORTED: 1 } },
            "/api/reports/1": report,
            "/api/reports/1/upvotes": { ...report, upvoteCount: 2 },
            "/api/reports/1/status": { ...report, status: "FIXED" }
        };
        return route.fulfill({ json: responses[url.pathname] });
    });

    async function checkMessage(role, text) {
        const region = page.getByRole(role).filter({ hasText: text });
        await region.waitFor();
        const { nodes } = await client.send("Accessibility.getFullAXTree");
        const message = nodes.find(node => node.role.value === "StaticText" && node.name.value.includes(text));
        assert.ok(message, "message is exposed to assistive technology");
        let ancestor = message;
        while (ancestor && ancestor.role.value !== role) {
            ancestor = nodes.find(node => node.nodeId === ancestor.parentId);
        }
        assert.ok(ancestor, "message belongs to the expected live region");
        const live = ancestor.properties.find(property => property.name === "live");
        assert.equal(live.value.value, role === "status" ? "polite" : "assertive");
    }

    await page.goto("http://cityfix.test/index.html");
    await checkMessage("status", "1 report loaded.");
    assert.equal(await page.locator("#stats").evaluate(el => el.closest("[role=status], [role=alert], [aria-live]") === null), true);

    await page.goto("http://cityfix.test/report.html?id=1");
    await page.getByRole("button", { name: "Upvote" }).click();
    await checkMessage("status", "Thank you, your vote was added. 2 votes total.");
    failRequest = true;
    await page.getByRole("button", { name: "Upvote" }).click();
    await checkMessage("alert", "Request failed");
    assert.equal(await page.locator("#upvote-message").textContent(), "");

    await page.goto("http://cityfix.test/new-report.html");
    await page.getByRole("button", { name: "Submit report" }).click();
    await checkMessage("alert", "Request failed");

    failRequest = false;
    await page.goto("http://cityfix.test/admin.html");
    await page.getByRole("combobox").selectOption("FIXED");
    await checkMessage("status", 'Status of "Streetlight" changed to Fixed.');
});
