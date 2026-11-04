const reportId = new URLSearchParams(window.location.search).get("id");
const message = document.getElementById("message");
const reportDetails = document.getElementById("report");
const upvoteForm = document.getElementById("upvote-form");
const upvoteMessage = document.getElementById("upvote-message");

function formatFixedOn(report) {
    if (report.status !== "FIXED") {
        return "Not fixed yet";
    }
    if (!report.fixedAt || isNaN(new Date(report.fixedAt))) {
        return "Date not recorded";
    }
    return formatDate(report.fixedAt);
}

function showReport(report) {
    document.getElementById("title").textContent = report.title;
    document.getElementById("status").textContent = formatLabel(report.status);
    document.getElementById("status").className = badgeClass(report.status);
    document.getElementById("votes").textContent = formatVotes(report.upvoteCount);
    document.getElementById("description").textContent = report.description;
    document.getElementById("category").textContent = formatLabel(report.category);
    document.getElementById("area").textContent = report.area;
    document.getElementById("address").textContent = report.address || "Not given";
    document.getElementById("reporter").textContent = report.reporterName;
    document.getElementById("created").textContent = formatDate(report.createdAt);
    const showFixed = report.status !== "REJECTED";
    document.getElementById("fixed-label").hidden = !showFixed;
    document.getElementById("fixed").hidden = !showFixed;
    document.getElementById("fixed").textContent = formatFixedOn(report);
}

async function loadReport() {
    if (!reportId) {
        message.className = "message message-error";
        message.textContent = "No report selected.";
        return;
    }

    try {
        const report = await getReport(reportId);
        showReport(report);
        message.textContent = "";
        reportDetails.hidden = false;
        upvoteForm.hidden = false;
    } catch (error) {
        message.className = "message message-error";
        message.textContent = "Could not load the report: " + error.message;
    }
}

upvoteForm.addEventListener("submit", async event => {
    event.preventDefault();
    upvoteMessage.className = "message";
    upvoteMessage.textContent = "Sending...";

    try {
        const report = await upvoteReport(reportId, document.getElementById("voterEmail").value);
        showReport(report);
        upvoteForm.reset();
        upvoteMessage.className = "message message-success";
        upvoteMessage.textContent = "Thank you, your vote was added.";
    } catch (error) {
        upvoteMessage.className = "message message-error";
        upvoteMessage.textContent = error.message;
    }
});

loadReport();
