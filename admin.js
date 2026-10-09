// ============================================
// CareerScope Admin Panel
// Works with the supplied admin.html
// ============================================

const API_BASE = "http://localhost:8080";

let adminToken =
    sessionStorage.getItem("careerscopeAdminToken") || "";

let allStudents = [];
let allRankings = [];

document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const loginMessage = document.getElementById("loginMessage");

    const loginScreen = document.getElementById("loginScreen");
    const dashboardScreen = document.getElementById("dashboardScreen");

    // ---------------- LOGIN ----------------
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value;

        if (!username || !password) {
            showLoginMessage("Enter username and password.", true);
            return;
        }

        const button = loginForm.querySelector('button[type="submit"]');
        button.disabled = true;
        button.innerHTML = "Signing in...";

        try {
            const response = await fetch(`${API_BASE}/api/admin/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body:
                    "username=" + encodeURIComponent(username) +
                    "&password=" + encodeURIComponent(password)
            });

            const text = await response.text();
            console.log("Admin login status:", response.status);
            console.log("Admin login response:", text);

            let result;

            try {
                result = JSON.parse(text);
            } catch {
                throw new Error(
                    "Unexpected server response. Check AdminHandler.java."
                );
            }

            if (!response.ok || !result.success || !result.token) {
                showLoginMessage(
                    result.message || "Invalid username or password.",
                    true
                );
                return;
            }

            adminToken = result.token;

            sessionStorage.setItem(
                "careerscopeAdminToken",
                adminToken
            );

            // Reveal dashboard in this same HTML file.
            loginScreen.classList.add("hidden");
            dashboardScreen.classList.remove("hidden");

            loginForm.reset();
            showGlobalMessage("Admin login successful.");

            await loadDashboardData();

        } catch (error) {
            console.error("Admin login error:", error);
            showLoginMessage(error.message, true);
        } finally {
            button.disabled = false;
            button.innerHTML =
                'Sign in to dashboard <span>→</span>';
        }
    });

    // ---------------- NAVIGATION ----------------
    document.querySelectorAll("[data-view]").forEach((button) => {
        button.addEventListener("click", () => {
            openView(button.dataset.view);
        });
    });

    // ---------------- REFRESH BUTTONS ----------------
    document.getElementById("refreshOverview")
        .addEventListener("click", loadDashboardData);

    document.getElementById("refreshStudents")
        .addEventListener("click", loadStudents);

    document.getElementById("refreshRanking")
        .addEventListener("click", loadRanking);

    // ---------------- SEARCH ----------------
    document.getElementById("studentSearch")
        .addEventListener("input", renderStudents);

    // ---------------- LOGOUT ----------------
    document.getElementById("logoutBtn")
        .addEventListener("click", logout);

    // ---------------- RESTORE SESSION ----------------
    if (adminToken) {
        loginScreen.classList.add("hidden");
        dashboardScreen.classList.remove("hidden");
        loadDashboardData();
    }

    function showLoginMessage(message, isError = false) {
        loginMessage.textContent = message;
        loginMessage.style.color = isError ? "#ff7777" : "#65d6a3";
    }
});

// ============================================
// NAVIGATION
// ============================================

function openView(view) {
    const views = {
        overview: document.getElementById("overviewView"),
        students: document.getElementById("studentsView"),
        ranking: document.getElementById("rankingView")
    };

    if (!views[view]) return;

    Object.entries(views).forEach(([name, element]) => {
        element.classList.toggle("hidden", name !== view);
    });

    document.querySelectorAll("[data-view]").forEach((button) => {
        button.classList.toggle(
            "active",
            button.dataset.view === view
        );
    });

    if (view === "students") loadStudents();
    if (view === "ranking") loadRanking();
}

// ============================================
// API REQUEST
// ============================================

async function adminFetch(endpoint) {
    if (!adminToken) {
        throw new Error("Admin session missing. Please log in again.");
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "GET",
        headers: {
            "X-Admin-Token": adminToken
        }
    });

    const text = await response.text();

    if (response.status === 401 || response.status === 403) {
        logout();
        throw new Error("Admin session expired. Please log in again.");
    }

    if (!response.ok) {
        throw new Error(
            `API ${endpoint} returned ${response.status}: ${text}`
        );
    }

    try {
        return JSON.parse(text);
    } catch {
        throw new Error(`Invalid JSON returned by ${endpoint}.`);
    }
}

// Accept either an array or a named array in JSON.
function extractArray(result, keys) {
    if (Array.isArray(result)) return result;

    for (const key of keys) {
        if (Array.isArray(result?.[key])) {
            return result[key];
        }
    }

    return [];
}

// ============================================
// LOAD DASHBOARD
// ============================================

async function loadDashboardData() {
    showGlobalMessage("Loading dashboard data...");

    try {
        const [studentsResult, rankingResult] = await Promise.all([
            adminFetch("/api/admin/students"),
            adminFetch("/api/admin/ranking")
        ]);

        allStudents = extractArray(
            studentsResult,
            ["students", "data", "records"]
        );

        allRankings = extractArray(
            rankingResult,
            ["ranking", "rankings", "students", "data"]
        );

        renderOverview();
        renderStudents();
        renderRanking();

        showGlobalMessage("Dashboard data loaded.");
    } catch (error) {
        console.error("Dashboard error:", error);
        showGlobalMessage(error.message, true);
    }
}

// ============================================
// OVERVIEW STATS
// ============================================

function renderOverview() {
    const branches = new Set(
        allStudents
            .map(student => student.branch)
            .filter(Boolean)
    );

    const scores = allRankings
        .map(student => Number(
            student.overall_score ??
            student.overallScore ??
            student.score
        ))
        .filter(Number.isFinite);

    const average = scores.length
        ? scores.reduce((sum, score) => sum + score, 0) / scores.length
        : 0;

    document.getElementById("totalStudents").textContent =
        allStudents.length;

    document.getElementById("averageScore").textContent =
        average.toFixed(2);

    document.getElementById("totalBranches").textContent =
        branches.size;

    const top = [...allRankings]
        .sort((a, b) => getScore(b) - getScore(a))
        .slice(0, 5);

    document.getElementById("topStudents").innerHTML =
        makeTable(
            ["Rank", "Student", "PRN", "Branch", "Overall Score"],
            top.map((student, index) => [
                index + 1,
                student.name ?? "-",
                student.prn ?? "-",
                student.branch ?? "-",
                formatScore(student)
            ])
        );
}

// ============================================
// STUDENTS
// ============================================

async function loadStudents() {
    try {
        allStudents = extractArray(
            await adminFetch("/api/admin/students"),
            ["students", "data", "records"]
        );

        document.getElementById("studentCount").textContent =
            `${allStudents.length} registered student(s)`;

        renderStudents();
        renderOverview();
    } catch (error) {
        console.error("Students error:", error);
        document.getElementById("studentsTable").textContent =
            error.message;
        showGlobalMessage(error.message, true);
    }
}

function renderStudents() {
    const query = (
        document.getElementById("studentSearch")?.value || ""
    ).toLowerCase().trim();

    const filtered = allStudents.filter(student => {
        const searchable = [
            student.name,
            student.prn,
            student.email,
            student.branch,
            student.phone
        ].join(" ").toLowerCase();

        return searchable.includes(query);
    });

    document.getElementById("studentCount").textContent =
        `${filtered.length} student(s) shown`;

    document.getElementById("studentsTable").innerHTML =
        makeTable(
            ["ID", "Name", "PRN", "Email", "Phone", "Branch", "Semester", "CGPA"],
            filtered.map(student => [
                student.student_id ?? student.id ?? "-",
                student.name ?? "-",
                student.prn ?? "-",
                student.email ?? "-",
                student.phone ?? "-",
                student.branch ?? "-",
                student.semester ?? "-",
                student.cgpa ?? "-"
            ])
        );
}

// ============================================
// PLACEMENT RANKING
// ============================================

async function loadRanking() {
    try {
        allRankings = extractArray(
            await adminFetch("/api/admin/ranking"),
            ["ranking", "rankings", "students", "data"]
        );

        renderRanking();
        renderOverview();
    } catch (error) {
        console.error("Ranking error:", error);
        document.getElementById("rankingTable").textContent =
            error.message;
        showGlobalMessage(error.message, true);
    }
}

function renderRanking() {
    const sorted = [...allRankings].sort(
        (a, b) => getScore(b) - getScore(a)
    );

    document.getElementById("rankingTable").innerHTML =
        makeTable(
            ["Rank", "Student", "PRN", "Branch", "CGPA", "Overall Score"],
            sorted.map((student, index) => [
                index + 1,
                student.name ?? "-",
                student.prn ?? "-",
                student.branch ?? "-",
                student.cgpa ?? "-",
                formatScore(student)
            ])
        );
}

function getScore(student) {
    const score = Number(
        student.overall_score ??
        student.overallScore ??
        student.score
    );

    return Number.isFinite(score) ? score : 0;
}

function formatScore(student) {
    const score =
        student.overall_score ??
        student.overallScore ??
        student.score;

    return score === undefined || score === null
        ? "-"
        : escapeHTML(score);
}

// ============================================
// SAFE TABLE GENERATOR
// ============================================

function makeTable(headers, rows) {
    if (!rows.length) {
        return '<p class="empty-state">No records found.</p>';
    }

    const head = headers.map(header =>
        `<th>${escapeHTML(header)}</th>`
    ).join("");

    const body = rows.map(row =>
        `<tr>${row.map(value =>
            `<td>${escapeHTML(value ?? "-")}</td>`
        ).join("")}</tr>`
    ).join("");

    return `
        <table>
            <thead><tr>${head}</tr></thead>
            <tbody>${body}</tbody>
        </table>
    `;
}

function escapeHTML(value) {
    return String(value).replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[char]);
}

// ============================================
// MESSAGES
// ============================================

function showGlobalMessage(message, isError = false) {
    const element = document.getElementById("globalMessage");

    if (!element) return;

    element.textContent = message;
    element.style.color = isError ? "#ff7777" : "";
}

// ============================================
// LOGOUT
// ============================================

function logout() {
    sessionStorage.removeItem("careerscopeAdminToken");
    adminToken = "";

    document.getElementById("dashboardScreen").classList.add("hidden");
    document.getElementById("loginScreen").classList.remove("hidden");

    document.getElementById("loginForm").reset();
    document.getElementById("loginMessage").textContent = "";

    allStudents = [];
    allRankings = [];
}
