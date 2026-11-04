(function (root) {
    const FIELD_LABELS = Object.freeze({
        title: "Title",
        description: "Description",
        category: "Category",
        area: "Area",
        reporterName: "Your name",
        reporterEmail: "Your email",
        voterEmail: "Your email"
    });

    function formatFieldError(fieldName, validationMessage) {
        const label = FIELD_LABELS[fieldName] || fieldName;
        const detail = validationMessage.trim().replace(/[.]$/, "");

        if (detail === "must not be blank" || detail === "must not be null") {
            return label + " is required.";
        }

        if (detail === "must be a well-formed email address") {
            return label + " must be a valid email address.";
        }

        return label + " " + detail.charAt(0).toLowerCase() + detail.slice(1) + ".";
    }

    function parseFieldErrors(message, allowedFieldNames) {
        if (typeof message !== "string" || !message.trim()) {
            return {};
        }

        const allowedFields = new Set(allowedFieldNames || Object.keys(FIELD_LABELS));
        const errors = {};

        for (const entry of message.split(";")) {
            const normalizedEntry = entry.trim();
            const separator = normalizedEntry.indexOf(" ");
            if (separator <= 0) {
                continue;
            }

            const fieldName = normalizedEntry.slice(0, separator);
            const validationMessage = normalizedEntry.slice(separator + 1).trim();
            if (!allowedFields.has(fieldName) || !validationMessage) {
                continue;
            }

            const isRequiredError =
                validationMessage === "must not be blank" ||
                validationMessage === "must not be null";

            // A required-field error is more useful than a second constraint error
            // (for example, @Email on an empty email field).
            if (!errors[fieldName] || isRequiredError) {
                errors[fieldName] = formatFieldError(fieldName, validationMessage);
            }
        }

        return errors;
    }

    function clearFieldError(field) {
        if (!field || typeof field.getAttribute !== "function") {
            return;
        }

        const errorId = field.getAttribute("aria-describedby");
        if (!errorId) {
            return;
        }

        field.setAttribute("aria-invalid", "false");

        if (typeof document === "undefined") {
            return;
        }

        const errorElement = document.getElementById(errorId);
        if (errorElement) {
            errorElement.textContent = "";
            errorElement.hidden = true;
        }
    }

    function clearFieldErrors(form) {
        if (!form || !form.elements) {
            return;
        }

        Array.from(form.elements).forEach(clearFieldError);
    }

    function showFieldErrors(form, errors) {
        if (!form || !form.elements || !errors) {
            return false;
        }

        let firstInvalidField = null;

        // Form controls are visited in their visual/tab order, not API message order.
        for (const field of Array.from(form.elements)) {
            if (!field.name || !Object.prototype.hasOwnProperty.call(errors, field.name)) {
                continue;
            }

            const errorId = field.getAttribute("aria-describedby");
            const errorElement = errorId && typeof document !== "undefined"
                ? document.getElementById(errorId)
                : null;

            if (!errorElement) {
                continue;
            }

            field.setAttribute("aria-invalid", "true");
            errorElement.textContent = errors[field.name];
            errorElement.hidden = false;
            firstInvalidField = firstInvalidField || field;
        }

        if (firstInvalidField) {
            firstInvalidField.focus();
        }

        return Boolean(firstInvalidField);
    }

    function bindFieldErrorClearing(form) {
        if (!form || typeof form.addEventListener !== "function") {
            return;
        }

        const handleFieldEdit = event => clearFieldError(event.target);
        form.addEventListener("input", handleFieldEdit);
        form.addEventListener("change", handleFieldEdit);
    }

    const validation = {
        parseFieldErrors,
        formatFieldError,
        clearFieldError,
        clearFieldErrors,
        showFieldErrors,
        bindFieldErrorClearing
    };

    root.CityFixFieldValidation = validation;

    if (typeof module !== "undefined" && module.exports) {
        module.exports = validation;
    }
})(typeof window !== "undefined" ? window : globalThis);
