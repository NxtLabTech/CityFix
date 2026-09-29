const API_URL = "/api/reports";

async function request(url, options) {
    let response;
    let body;

    try {
        response = await fetch(url, options);
        body = await response.text();
    } catch (error) {
        if (error instanceof TypeError || error && error.name === "TypeError") {
            throw new Error("Cannot reach the server. Please try again.");
        }
        throw error;
    }

    let data = null;
    if (body.trim()) {
        try {
            data = JSON.parse(body);
        } catch (error) {
            if (!response.ok) {
                throw new Error("Request failed with status " + response.status + ".");
            }
            throw new Error("Server returned an invalid response.");
        }
    }

    if (!response.ok) {
        throw new Error(data && data.message || "Request failed with status " + response.status + ".");
    }
    return data;
}

function jsonOptions(method, body) {
    return {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    };
}

function getReports(filters) {
    const params = new URLSearchParams();
    for (const [name, value] of Object.entries(filters || {})) {
        if (value) {
            params.append(name, value);
        }
    }
    return request(API_URL + "?" + params);
}

function getReport(id) {
    return request(API_URL + "/" + id);
}

function createReport(report) {
    return request(API_URL, jsonOptions("POST", report));
}

function upvoteReport(id, voterEmail) {
    return request(API_URL + "/" + id + "/upvotes", jsonOptions("POST", { voterEmail: voterEmail }));
}

function updateStatus(id, status) {
    return request(API_URL + "/" + id + "/status", jsonOptions("PATCH", { status: status }));
}

function getStats() {
    return request(API_URL + "/stats");
}

function formatLabel(value) {
    const text = value.replace("_", " ").toLowerCase();
    return text.charAt(0).toUpperCase() + text.slice(1);
}

function badgeClass(status) {
    return "badge badge-" + status.toLowerCase().replace("_", "-");
}

function formatVotes(count) {
    return count + (count === 1 ? " vote" : " votes");
}

function formatDate(isoDate) {
    return new Date(isoDate).toLocaleDateString();
}
