const STATUSES = ["REPORTED", "IN_PROGRESS", "FIXED", "REJECTED"];

const message = document.getElementById("message");
const reportList = document.getElementById("report-list");

// Changing the dropdown only selects a status. It is saved when the Save button is used.
function createStatusControl(report, badge) {
    const select = document.createElement("select");
    select.setAttribute("aria-label", "Status of " + report.title);

    for (const status of STATUSES) {
        const option = document.createElement("option");
        option.value = status;
        option.textContent = formatLabel(status);
        option.selected = status === report.status;
        select.append(option);
    }

    const button = document.createElement("button");
    button.type = "submit";
    button.textContent = "Save";
    button.setAttribute("aria-label", "Save status of " + report.title);
    button.disabled = true;

    const feedback = document.createElement("p");
    feedback.className = "status-feedback";
    feedback.setAttribute("role", "status");

    const form = document.createElement("form");
    form.className = "status-control";
    form.append(select, button, feedback);

    select.addEventListener("change", () => {
        button.disabled = select.value === report.status;
        feedback.className = "status-feedback";
        feedback.textContent = "";
    });

    form.addEventListener("submit", event => {
        event.preventDefault();
        if (select.value !== report.status) {
            changeStatus(report, { select, button, feedback, badge });
        }
    });

    return form;
}

function createCell(content) {
    const cell = document.createElement("td");
    cell.append(content);
    return cell;
}

function createReportRow(report) {
    const link = document.createElement("a");
    link.href = "report.html?id=" + report.id;
    link.textContent = report.title;

    const badge = document.createElement("span");
    badge.className = badgeClass(report.status);
    badge.textContent = formatLabel(report.status);

    const row = document.createElement("tr");
    row.append(
        createCell(link),
        createCell(report.area),
        createCell(formatLabel(report.category)),
        createCell(badge),
        createCell(formatVotes(report.upvoteCount)),
        createCell(createStatusControl(report, badge))
    );
    return row;
}

async function changeStatus(report, control) {
    const { select, button, feedback, badge } = control;
    const newStatus = select.value;
    select.disabled = true;
    button.disabled = true;
    button.textContent = "Saving...";
    feedback.className = "status-feedback";
    feedback.textContent = "";

    try {
        await updateStatus(report.id, newStatus);
        report.status = newStatus;
        badge.className = badgeClass(newStatus);
        badge.textContent = formatLabel(newStatus);
        feedback.className = "status-feedback status-feedback-success";
        feedback.textContent = "Saved as " + formatLabel(newStatus) + ".";
    } catch (error) {
        feedback.className = "status-feedback status-feedback-error";
        feedback.textContent = "Could not save: " + error.message;
    } finally {
        select.disabled = false;
        button.textContent = "Save";
        button.disabled = select.value === report.status;
        select.focus();
    }
}

async function loadReports() {
    message.textContent = "Loading reports...";
    try {
        const reports = await getReports({});
        reportList.replaceChildren(...reports.map(createReportRow));
        message.textContent = "";
    } catch (error) {
        message.className = "message message-error";
        message.textContent = "Could not load reports: " + error.message;
    }
}

loadReports();
