const reportForm = document.getElementById("report-form");
const message = document.getElementById("message");
const submitButton = reportForm.querySelector("button[type=\"submit\"]");

reportForm.addEventListener("submit", async event => {
    event.preventDefault();
    if (submitButton.disabled) {
        return;
    }

    submitButton.disabled = true;
    message.className = "message";
    message.textContent = "Sending...";

    const report = Object.fromEntries(new FormData(reportForm));
    report.category = report.category || null;

    try {
        const createdReport = await createReport(report);
        window.location.href = "report.html?id=" + createdReport.id;
    } catch (error) {
        message.className = "message message-error";
        message.textContent = error.message;
    } finally {
        submitButton.disabled = false;
    }
});
