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
    statsText.className = "stats";
    statsText.textContent = parts.join(" | ");
}

async function loadReports() {
    message.className = "message";
    message.textContent = "Loading reports...";
    reportList.replaceChildren();

    const filters = Object.fromEntries(new FormData(filterForm));
    const reportsPromise = getReports(filters);
    const statsPromise = getStats();

    try {
        const reports = await reportsPromise;
        message.textContent = reports.length === 0 ? "No reports found. Try changing the filters." : "";
        reports.forEach(report => reportList.append(createReportItem(report)));
    } catch (error) {
        message.className = "message message-error";
        message.textContent = "Could not load reports: " + error.message;
    }

    try {
        const stats = await statsPromise;
        showStats(stats);
    } catch (error) {
        statsText.className = "stats stats-error";
        statsText.textContent = "Could not load stats: " + error.message;
    }
}

filterForm.addEventListener("submit", event => {
    event.preventDefault();
    loadReports();
});

loadReports();
