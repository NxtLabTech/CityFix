const filterForm = document.getElementById("filter-form");
const statsText = document.getElementById("stats");
const message = document.getElementById("message");
const reportList = document.getElementById("report-list");

function createReportItem(report) {
    const status = document.createElement("span");
    status.className = badgeClass(report.status);
    status.textContent = formatLabel(report.status);

    const category = document.createElement("span");
    category.className = "tag";
    category.textContent = formatLabel(report.category);

    const badges = document.createElement("div");
    badges.className = "card-badges";
    badges.append(status, category);

    const link = document.createElement("a");
    link.href = "report.html?id=" + report.id;
    link.textContent = report.title;

    const title = document.createElement("h2");
    title.className = "card-title";
    title.append(link);

    const place = document.createElement("p");
    place.className = "card-place";
    place.textContent = report.address ? report.area + " - " + report.address : report.area;

    const votes = document.createElement("p");
    votes.className = "votes";
    votes.textContent = formatVotes(report.upvoteCount);

    const item = document.createElement("li");
    item.className = "card report-card";
    item.append(badges, title, place, votes);
    return item;
}

function showStats(stats) {
    const parts = Object.entries(stats.byStatus)
        .map(([status, count]) => formatLabel(status) + ": " + count);
    statsText.textContent = parts.join(" | ");
}

async function loadReports() {
    message.className = "message";
    message.textContent = "Loading reports...";
    reportList.replaceChildren();

    try {
        const filters = Object.fromEntries(new FormData(filterForm));
        const [reports, stats] = await Promise.all([getReports(filters), getStats()]);
        showStats(stats);
        message.textContent = reports.length === 0 ? "No reports found. Try changing the filters." : "";
        reports.forEach(report => reportList.append(createReportItem(report)));
    } catch (error) {
        message.className = "message message-error";
        message.textContent = "Could not load reports: " + error.message;
    }
}

// The URL is the source of truth for the filters.
// Only these query parameters are read; anything else is ignored.
const FILTER_NAMES = ["area", "status", "category", "sort"];
const DEFAULT_SORT = "newest";

function readFiltersFromUrl() {
    const params = new URLSearchParams(window.location.search);

    FILTER_NAMES.forEach(name => {
        const field = filterForm.elements[name];
        const value = (params.get(name) || "").trim();

        if (field.tagName === "SELECT") {
            // URL values are lowercase (status=fixed); option values are uppercase.
            // Unknown values fall back to the first option.
            const match = Array.from(field.options)
                .find(option => option.value.toLowerCase() === value.toLowerCase());
            field.value = match ? match.value : field.options[0].value;
        } else {
            field.value = value;
        }
    });
}

function writeFiltersToUrl() {
    const params = new URLSearchParams();

    FILTER_NAMES.forEach(name => {
        const field = filterForm.elements[name];
        const value = field.value.trim();
        const urlValue = field.tagName === "SELECT" ? value.toLowerCase() : value;

        // Leave out empty values and the default sort to keep the URL short
        if (urlValue && !(name === "sort" && urlValue === DEFAULT_SORT)) {
            params.set(name, urlValue);
        }
    });

    const query = params.toString();
    const newUrl = window.location.pathname + (query ? "?" + query : "");

    if (newUrl !== window.location.pathname + window.location.search) {
        history.pushState(null, "", newUrl);
    }
}

filterForm.addEventListener("submit", event => {
    event.preventDefault();
    filterForm.elements.area.value = filterForm.elements.area.value.trim();
    writeFiltersToUrl();
    loadReports();
});

// Browser Back/Forward between filtered views on this page
window.addEventListener("popstate", () => {
    readFiltersFromUrl();
    loadReports();
});

readFiltersFromUrl();
loadReports();
