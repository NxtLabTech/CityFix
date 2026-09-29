const assert = require("node:assert/strict");
const test = require("node:test");
const { addSubmitButton, createDocument, loadScript, waitForAsyncHandlers } = require("./helpers");

function deferred() {
    let resolve;
    let reject;
    const promise = new Promise((resolvePromise, rejectPromise) => {
        resolve = resolvePromise;
        reject = rejectPromise;
    });
    return { promise, resolve, reject };
}

function newReportContext(createReport) {
    const { document, elements } = createDocument(["report-form", "message"]);
    const form = elements.get("report-form");
    const button = addSubmitButton(form);
    const context = {
        document,
        FormData: class { [Symbol.iterator]() { return [][Symbol.iterator](); } },
        createReport,
        window: { location: {} }
    };
    loadScript("new-report.js", context);
    return { context, form, button };
}

test("report submit button stays disabled while request is pending and enables after success", async () => {
    const request = deferred();
    const { context, form, button } = newReportContext(() => request.promise);

    form.dispatch("submit");
    assert.equal(button.disabled, true);
    request.resolve({ id: 7 });
    await waitForAsyncHandlers();

    assert.equal(button.disabled, false);
    assert.equal(context.window.location.href, "report.html?id=7");
});

test("report submit button enables after failure and ignores a duplicate submission", async () => {
    const request = deferred();
    let calls = 0;
    const { form, button } = newReportContext(() => {
        calls += 1;
        return request.promise;
    });

    form.dispatch("submit");
    form.dispatch("submit");
    assert.equal(calls, 1);
    request.reject(new Error("Could not save report"));
    await waitForAsyncHandlers();

    assert.equal(button.disabled, false);
});

function upvoteContext(upvoteReport) {
    const ids = ["message", "report", "upvote-form", "upvote-message", "title", "status", "votes", "description", "category", "area", "address", "reporter", "created", "fixed", "voterEmail"];
    const { document, elements } = createDocument(ids);
    const form = elements.get("upvote-form");
    const button = addSubmitButton(form);
    const context = {
        document,
        URLSearchParams: class { get() { return "7"; } },
        window: { location: { search: "?id=7" } },
        formatLabel: value => value,
        badgeClass: value => value,
        formatVotes: value => String(value),
        formatDate: value => value,
        getReport: async () => ({ title: "Pothole", status: "REPORTED", upvoteCount: 1, description: "Deep", category: "POTHOLE", area: "Downtown", reporterName: "Ana", createdAt: "today" }),
        upvoteReport
    };
    loadScript("report-detail.js", context);
    return { elements, form, button };
}

test("upvote button stays disabled while request is pending and enables after success", async () => {
    const request = deferred();
    const { form, button } = upvoteContext(() => request.promise);
    await waitForAsyncHandlers();

    form.dispatch("submit");
    assert.equal(button.disabled, true);
    request.resolve({ title: "Pothole", status: "REPORTED", upvoteCount: 2, description: "Deep", category: "POTHOLE", area: "Downtown", reporterName: "Ana", createdAt: "today" });
    await waitForAsyncHandlers();

    assert.equal(button.disabled, false);
});

test("upvote button enables after failure and ignores a duplicate submission", async () => {
    const request = deferred();
    let calls = 0;
    const { form, button } = upvoteContext(() => {
        calls += 1;
        return request.promise;
    });
    await waitForAsyncHandlers();

    form.dispatch("submit");
    form.dispatch("submit");
    assert.equal(calls, 1);
    request.reject(new Error("Already voted"));
    await waitForAsyncHandlers();

    assert.equal(button.disabled, false);
});