const reportForm = document.getElementById("report-form");
const message = document.getElementById("message");
const fieldValidation = CityFixFieldValidation;
const reportFields = ["title", "description", "category", "area", "reporterName", "reporterEmail"];

fieldValidation.bindFieldErrorClearing(reportForm);

reportForm.addEventListener("submit", async event => {
    event.preventDefault();
    fieldValidation.clearFieldErrors(reportForm);
    message.className = "message";
    message.textContent = "Sending...";

    const report = Object.fromEntries(new FormData(reportForm));
    report.category = report.category || null;

    try {
        const createdReport = await createReport(report);
        window.location.href = "report.html?id=" + createdReport.id;
    } catch (error) {
        const fieldErrors = fieldValidation.parseFieldErrors(error.message, reportFields);

        if (fieldValidation.showFieldErrors(reportForm, fieldErrors)) {
            message.className = "message";
            message.textContent = "";
            return;
        }

        message.className = "message message-error";
        message.textContent = error.message;
    }
});
