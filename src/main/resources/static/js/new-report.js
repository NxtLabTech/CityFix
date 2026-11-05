const reportForm = document.getElementById("report-form");
const message = document.getElementById("message");

const fieldNames = [
    "title",
    "description",
    "category",
    "area",
    "address",
    "reporterName",
    "reporterEmail"
];

const fieldLabels = {
    title: "Title",
    description: "Description",
    category: "Category",
    area: "Area",
    address: "Address",
    reporterName: "Your name",
    reporterEmail: "Your email"
};

function clearFieldErrors() {
    fieldNames.forEach(name => {
        const field = document.getElementById(name);
        const error = document.getElementById(name + "-error");

        error.textContent = "";
        field.removeAttribute("aria-invalid");
    });
}

function showFieldErrors(errorMessage) {
    const errors = errorMessage.split(";").map(error => error.trim());
    let hasFieldError = false;

    errors.forEach(errorText => {
        const fieldName = fieldNames.find(name =>
            errorText.toLowerCase().startsWith(name.toLowerCase() + " ")
        );

        if (!fieldName) {
            return;
        }

        const field = document.getElementById(fieldName);
        const errorElement = document.getElementById(fieldName + "-error");
        const detail = errorText.slice(fieldName.length).trim();

        errorElement.textContent = fieldLabels[fieldName] + " " + detail;
        field.setAttribute("aria-invalid", "true");

        hasFieldError = true;
    });

    return hasFieldError;
}

reportForm.addEventListener("submit", async event => {
    event.preventDefault();

    clearFieldErrors();

    message.className = "message";
    message.textContent = "Sending...";

    const report = Object.fromEntries(new FormData(reportForm));
    report.category = report.category || null;

    try {
        const createdReport = await createReport(report);
        window.location.href = "report.html?id=" + createdReport.id;
    } catch (error) {
        message.className = "message";
        message.textContent = "";

        const hasFieldErrors = showFieldErrors(error.message);

        if (hasFieldErrors) {
            const firstInvalidField = reportForm.querySelector(
                '[aria-invalid="true"]'
            );

            firstInvalidField?.focus();
        } else {
            message.className = "message message-error";
            message.textContent = error.message;
        }
    }
});
