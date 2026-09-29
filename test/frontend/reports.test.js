const assert = require("node:assert/strict");
const test = require("node:test");
const { createDocument, loadScript, waitForAsyncHandlers } = require("./helpers");

test("reports render when stats fail", async () => {
    const { document, elements } = createDocument(["filter-form", "stats", "message", "report-list"]);
    const context = {
        document,
        FormData: class { [Symbol.iterator]() { return [][Symbol.iterator](); } },
        getReports: async () => [{ id: 1, title: "Pothole", status: "REPORTED", category: "POTHOLE", area: "Downtown", upvoteCount: 0 }],
        getStats: async () => { throw new Error("Stats unavailable"); },
        formatLabel: value => value,
        badgeClass: value => value,
        formatVotes: value => String(value)
    };

    loadScript("reports.js", context);
    await waitForAsyncHandlers();

    assert.equal(elements.get("report-list").children.length, 1);
    assert.equal(elements.get("stats").textContent, "Could not load stats: Stats unavailable");
    assert.equal(elements.get("message").textContent, "");
});