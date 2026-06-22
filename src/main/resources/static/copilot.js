const form = document.querySelector("#chatForm");
const input = document.querySelector("#messageInput");
const messages = document.querySelector("#messages");
const sendButton = document.querySelector("#sendButton");
const apiKeyInput = document.querySelector("#apiKey");

const sessionId = crypto.randomUUID ? crypto.randomUUID() : `session-${Date.now()}`;
const chatApiUrl = window.location.protocol === "file:"
    ? "http://localhost:8080/api/v1/chat"
    : "/api/v1/chat";

document.querySelectorAll("[data-example]").forEach((button) => {
    button.addEventListener("click", () => {
        input.value = button.dataset.example;
        input.focus();
        resizeInput();
    });
});

input.addEventListener("input", resizeInput);

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const message = input.value.trim();
    if (!message) {
        return;
    }

    appendMessage("user", message);
    input.value = "";
    resizeInput();

    const loading = appendMessage("assistant", "Thinking...");
    setBusy(true);

    try {
        const response = await fetch(chatApiUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-API-Key": apiKeyInput.value.trim()
            },
            body: JSON.stringify({ message, sessionId })
        });

        const payload = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(payload.message || `Request failed with status ${response.status}`);
        }

        loading.querySelector(".bubble").textContent = payload.answer || "No answer returned.";
        const meta = entityMeta(payload);
        if (meta) {
            loading.querySelector(".bubble").appendChild(meta);
        }
    } catch (error) {
        loading.querySelector(".bubble").textContent = error.message || "Unable to reach the copilot.";
    } finally {
        setBusy(false);
    }
});

function appendMessage(role, text) {
    const article = document.createElement("article");
    article.className = `message ${role}`;

    const bubble = document.createElement("div");
    bubble.className = "bubble";
    bubble.textContent = text;

    article.appendChild(bubble);
    messages.appendChild(article);
    messages.scrollTop = messages.scrollHeight;
    return article;
}

function entityMeta(payload) {
    if (!payload.entityType || !payload.entityId) {
        return null;
    }

    const meta = document.createElement("span");
    meta.className = "meta";
    meta.textContent = `${payload.entityType} ${payload.entityId}`;
    return meta;
}

function setBusy(isBusy) {
    sendButton.disabled = isBusy;
    input.disabled = isBusy;
}

function resizeInput() {
    input.style.height = "auto";
    input.style.height = `${Math.min(input.scrollHeight, 140)}px`;
}
