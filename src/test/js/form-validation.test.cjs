const test = require("node:test");
const assert = require("node:assert/strict");

const {
    parseFieldErrors,
    clearFieldError,
    showFieldErrors
} = require("../../main/resources/static/js/form-validation.js");

function createField(name) {
    const attributes = { "aria-describedby": name + "-error", "aria-invalid": "false" };

    return {
        name,
        attributes,
        focused: false,
        getAttribute(attribute) {
            return attributes[attribute] || null;
        },
        setAttribute(attribute, value) {
            attributes[attribute] = value;
        },
        focus() {
            this.focused = true;
        }
    };
}

function withDocument(errorElements, callback) {
    const previousDocument = global.document;
    global.document = {
        getElementById(id) {
            return errorElements[id] || null;
        }
    };

    try {
        callback();
    } finally {
        if (previousDocument === undefined) {
            delete global.document;
        } else {
            global.document = previousDocument;
        }
    }
}

test("maps backend field names to readable labels and messages", () => {
    const errors = parseFieldErrors(
        "area must not be blank; category must not be null; description must not be blank; reporterEmail must be a well-formed email address; reporterEmail must not be blank; reporterName must not be blank; title must not be blank",
        ["area", "category", "description", "reporterEmail", "reporterName", "title"]
    );

    assert.deepEqual(errors, {
        area: "Area is required.",
        category: "Category is required.",
        description: "Description is required.",
        reporterEmail: "Your email is required.",
        reporterName: "Your name is required.",
        title: "Title is required."
    });
});

test("prefers a required error when the backend returns multiple errors for one field", () => {
    const errors = parseFieldErrors(
        "reporterEmail must be a well-formed email address; reporterEmail must not be blank",
        ["reporterEmail"]
    );

    assert.deepEqual(errors, {
        reporterEmail: "Your email is required."
    });
});

test("ignores fields that do not belong to the submitted form", () => {
    assert.deepEqual(
        parseFieldErrors("voterEmail must not be blank; title must not be blank", ["voterEmail"]),
        { voterEmail: "Your email is required." }
    );
});

test("does not treat an ordinary API error as field validation", () => {
    assert.deepEqual(parseFieldErrors("Report with id 42 not found"), {});
    assert.deepEqual(parseFieldErrors("Something went wrong"), {});
});

test("shows inline errors and focuses the first invalid field in form order", () => {
    const title = createField("title");
    const email = createField("reporterEmail");
    const errorElements = {
        "title-error": { textContent: "", hidden: true },
        "reporterEmail-error": { textContent: "", hidden: true }
    };
    const form = { elements: [title, email] };

    withDocument(errorElements, () => {
        const shown = showFieldErrors(form, {
            reporterEmail: "Your email must be a valid email address.",
            title: "Title is required."
        });

        assert.equal(shown, true);
        assert.equal(title.attributes["aria-invalid"], "true");
        assert.equal(errorElements["title-error"].textContent, "Title is required.");
        assert.equal(errorElements["title-error"].hidden, false);
        assert.equal(email.attributes["aria-invalid"], "true");
        assert.equal(errorElements["reporterEmail-error"].hidden, false);
        assert.equal(title.focused, true);
        assert.equal(email.focused, false);
    });
});

test("clears an inline error and resets the field accessibility state", () => {
    const email = createField("voterEmail");
    email.attributes["aria-invalid"] = "true";
    const errorElement = { textContent: "Your email is required.", hidden: false };

    withDocument({ "voterEmail-error": errorElement }, () => {
        clearFieldError(email);

        assert.equal(email.attributes["aria-invalid"], "false");
        assert.equal(errorElement.textContent, "");
        assert.equal(errorElement.hidden, true);
    });
});
