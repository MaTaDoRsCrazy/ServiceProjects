const API_URL = localStorage.getItem("apiUrl") || "http://localhost:8080/api";

const state = {
    token: localStorage.getItem("token"),
    user: JSON.parse(localStorage.getItem("user") || "null"),
    currentView: "dashboard"
};

const labels = {
    NEW: "Новая",
    IN_PROGRESS: "В работе",
    WAITING_PARTS: "Ожидает запчасти",
    RESOLVED: "Решена",
    CLOSED: "Закрыта",
    LOW: "Низкий",
    MEDIUM: "Средний",
    HIGH: "Высокий",
    CRITICAL: "Критический",
    IN_SERVICE: "В эксплуатации",
    REPAIR: "Ремонт",
    RESERVED: "Резерв",
    WRITTEN_OFF: "Списано"
};

const loginScreen = document.querySelector("#loginScreen");
const app = document.querySelector("#app");
const loginForm = document.querySelector("#loginForm");
const loginError = document.querySelector("#loginError");
const userPill = document.querySelector("#userPill");
const viewTitle = document.querySelector("#viewTitle");

async function api(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };
    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(`${API_URL}${path}`, {
        ...options,
        headers
    });

    if (response.status === 401) {
        logout();
        throw new Error("Сессия истекла. Войдите снова.");
    }

    if (!response.ok) {
        let message = `Ошибка ${response.status}`;
        try {
            const body = await response.json();
            message = body.message || message;
        } catch {
            message = response.statusText || message;
        }
        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function showApp() {
    loginScreen.classList.add("hidden");
    app.classList.remove("hidden");
    userPill.textContent = `${state.user.fullName} · ${state.user.role}`;
    switchView(state.currentView);
}

function showLogin() {
    app.classList.add("hidden");
    loginScreen.classList.remove("hidden");
}

function logout() {
    state.token = null;
    state.user = null;
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    showLogin();
}

function switchView(view) {
    state.currentView = view;
    document.querySelectorAll(".view").forEach(section => section.classList.add("hidden"));
    document.querySelector(`#${view}View`).classList.remove("hidden");
    document.querySelectorAll(".nav").forEach(button => button.classList.toggle("active", button.dataset.view === view));
    viewTitle.textContent = {
        dashboard: "Сводка",
        tickets: "Заявки",
        equipment: "Оборудование"
    }[view];

    if (view === "dashboard") {
        loadDashboard();
    }
    if (view === "tickets") {
        loadTickets();
    }
    if (view === "equipment") {
        loadEquipment();
    }
}

async function loadDashboard() {
    const dashboard = await api("/reports/dashboard");
    const cards = [
        ["Всего заявок", dashboard.totalTickets],
        ["Открытые", dashboard.openTickets],
        ["Решенные", dashboard.resolvedTickets],
        ["Оборудование", dashboard.equipmentTotal]
    ];

    document.querySelector("#dashboardCards").innerHTML = cards.map(([title, value]) => `
        <article class="card">
            <span>${title}</span>
            <strong>${value}</strong>
        </article>
    `).join("");

    const max = Math.max(1, ...Object.values(dashboard.ticketsByStatus));
    document.querySelector("#ticketBars").innerHTML = Object.entries(dashboard.ticketsByStatus).map(([status, count]) => `
        <div class="bar-row">
            <strong>${labels[status] || status}</strong>
            <div class="bar"><span style="width: ${(count / max) * 100}%"></span></div>
            <span>${count}</span>
        </div>
    `).join("");
}

async function loadTickets() {
    const tickets = await api("/tickets");
    document.querySelector("#ticketsBody").innerHTML = tickets.map(ticket => `
        <tr>
            <td>#${ticket.id}</td>
            <td>${escapeHtml(ticket.title)}</td>
            <td><span class="priority">${labels[ticket.priority] || ticket.priority}</span></td>
            <td><span class="status">${labels[ticket.status] || ticket.status}</span></td>
            <td>${escapeHtml(ticket.createdByName)}</td>
            <td>
                <select data-ticket-status="${ticket.id}">
                    ${["NEW", "IN_PROGRESS", "WAITING_PARTS", "RESOLVED", "CLOSED"].map(status => `
                        <option value="${status}" ${ticket.status === status ? "selected" : ""}>${labels[status]}</option>
                    `).join("")}
                </select>
            </td>
        </tr>
    `).join("");

    document.querySelectorAll("[data-ticket-status]").forEach(select => {
        select.addEventListener("change", async event => {
            const id = event.target.dataset.ticketStatus;
            await api(`/tickets/${id}/status`, {
                method: "PATCH",
                body: JSON.stringify({
                    status: event.target.value,
                    comment: `Статус изменен через web-админку на ${labels[event.target.value]}`
                })
            });
            await loadDashboard();
            await loadTickets();
        });
    });
}

async function loadEquipment() {
    const equipment = await api("/equipment");
    document.querySelector("#equipmentGrid").innerHTML = equipment.map(item => `
        <article class="equipment-card">
            <span class="status">${labels[item.status] || item.status}</span>
            <h4>${escapeHtml(item.title)}</h4>
            <p><strong>Инв. номер:</strong> ${escapeHtml(item.inventoryNumber)}</p>
            <p><strong>Тип:</strong> ${item.type}</p>
            <p><strong>Локация:</strong> ${escapeHtml(item.location)}</p>
            <p><strong>Ответственный:</strong> ${escapeHtml(item.assignedToName || "Не назначен")}</p>
        </article>
    `).join("");
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

loginForm.addEventListener("submit", async event => {
    event.preventDefault();
    loginError.textContent = "";
    try {
        const auth = await api("/auth/login", {
            method: "POST",
            body: JSON.stringify({
                username: document.querySelector("#username").value,
                password: document.querySelector("#password").value
            })
        });
        state.token = auth.token;
        state.user = auth.user;
        localStorage.setItem("token", auth.token);
        localStorage.setItem("user", JSON.stringify(auth.user));
        showApp();
    } catch (error) {
        loginError.textContent = error.message;
    }
});

document.querySelector("#ticketForm").addEventListener("submit", async event => {
    event.preventDefault();
    await api("/tickets", {
        method: "POST",
        body: JSON.stringify({
            title: document.querySelector("#ticketTitle").value,
            description: document.querySelector("#ticketDescription").value,
            priority: document.querySelector("#ticketPriority").value
        })
    });
    event.target.reset();
    document.querySelector("#ticketPriority").value = "MEDIUM";
    await loadTickets();
    await loadDashboard();
});

document.querySelector("#logoutButton").addEventListener("click", logout);
document.querySelector("#refreshTickets").addEventListener("click", loadTickets);
document.querySelector("#refreshEquipment").addEventListener("click", loadEquipment);
document.querySelectorAll(".nav").forEach(button => {
    button.addEventListener("click", () => switchView(button.dataset.view));
});

if (state.token && state.user) {
    showApp();
} else {
    showLogin();
}

