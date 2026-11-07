function setMessage(status, text, type) {
    const alert = document.getElementById(status.id + "-error");
    const isError = type === "error";
    const active = isError ? alert : status;
    const inactive = isError ? status : alert;

    inactive.textContent = "";
    status.classList.toggle("message-success", type === "success");
    if (active.textContent !== text) {
        active.textContent = text;
    }
}
