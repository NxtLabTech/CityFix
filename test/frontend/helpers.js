const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

class Element {
    constructor(id = "") {
        this.id = id;
        this.children = [];
        this.listeners = {};
        this.disabled = false;
        this.hidden = false;
        this.value = "";
        this.textContent = "";
        this.className = "";
    }

    append(...children) {
        this.children.push(...children);
    }

    replaceChildren(...children) {
        this.children = children;
    }

    reset() {
        this.value = "";
    }

    addEventListener(type, listener) {
        this.listeners[type] = listener;
    }

    dispatch(type) {
        return this.listeners[type]({ preventDefault() {} });
    }

    querySelector() {
        return this.children.find(child => child.type === "submit");
    }
}

function createDocument(ids) {
    const elements = new Map(ids.map(id => [id, new Element(id)]));
    const document = {
        getElementById: id => elements.get(id),
        createElement: tag => {
            const element = new Element();
            element.tagName = tag;
            return element;
        }
    };
    return { document, elements };
}

function addSubmitButton(form) {
    const button = new Element();
    button.type = "submit";
    form.append(button);
    return button;
}

function loadScript(file, context) {
    const source = fs.readFileSync(path.join(__dirname, "../../src/main/resources/static/js", file), "utf8");
    vm.runInNewContext(source, context);
}

function waitForAsyncHandlers() {
    return new Promise(resolve => setImmediate(resolve));
}

module.exports = { Element, createDocument, addSubmitButton, loadScript, waitForAsyncHandlers };