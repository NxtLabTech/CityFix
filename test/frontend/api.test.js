const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const apiSource = fs.readFileSync(path.join(__dirname, "../../src/main/resources/static/js/api.js"), "utf8");

function loadApi(fetch) {
    const context = { fetch, URLSearchParams, console, TypeError };
    vm.runInNewContext(apiSource, context);
    return context;
}

function response(body, ok = true, status = 200) {
    return { ok, status, text: async () => body };
}

test("request returns a successful JSON response", async () => {
    const api = loadApi(async () => response('{"id": 1}'));

    const result = await api.getReport(1);
    assert.equal(result.id, 1);
});

test("request reports a readable error for a non-JSON response", async () => {
    const api = loadApi(async () => response("<html>Server error</html>", true));

    await assert.rejects(api.getReport(1), { message: "Server returned an invalid response." });
});

test("request accepts an empty response", async () => {
    const api = loadApi(async () => response("", true, 204));

    assert.equal(await api.getReport(1), null);
});

test("request uses the API error message for an HTTP error", async () => {
    const api = loadApi(async () => response('{"message":"Report not found"}', false, 404));

    await assert.rejects(api.getReport(1), { message: "Report not found" });
});

test("request uses a friendly message for a network failure", async () => {
    const api = loadApi(async () => {
        throw new TypeError("Failed to fetch");
    });

    await assert.rejects(api.getReport(1), { message: "Cannot reach the server. Please try again." });
});