// ========================================
// CareerScope Login - Student + Admin
// ========================================

const API_BASE = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById("loginForm");
    const roleButtons = document.querySelectorAll(".role-btn");

    const studentFields = document.getElementById("studentFields");
    const adminFields = document.getElementById("adminFields");

    const emailInput = document.getElementById("email");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");

    const studentOptions = document.getElementById("studentOptions");
    const studentSignup = document.getElementById("studentSignup");
    const adminNotice = document.getElementById("adminNotice");

    const formHeading = document.getElementById("formHeading");
    const formDescription = document.getElementById("formDescription");
    const loginBtn = document.getElementById("loginBtn");
    const loginMessage = document.getElementById("loginMessage");

    const togglePassword = document.getElementById("togglePassword");
    const googleLoginBtn = document.getElementById("googleLoginBtn");

    let currentRole = "student";

    // ------------------------------------
    // Switch between Student and Admin
    // ------------------------------------
    roleButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            currentRole = button.dataset.role;

            roleButtons.forEach(function (btn) {
                btn.classList.toggle("active", btn === button);
            });

            const isAdmin = currentRole === "admin";

            studentFields.classList.toggle("hidden", isAdmin);
            adminFields.classList.toggle("hidden", !isAdmin);

            studentOptions.classList.toggle("hidden", isAdmin);
            studentSignup.classList.toggle("hidden", isAdmin);
            adminNotice.classList.toggle("hidden", !isAdmin);

            emailInput.disabled = isAdmin;
            emailInput.required = !isAdmin;

            usernameInput.disabled = !isAdmin;
            usernameInput.required = isAdmin;

            formHeading.textContent = isAdmin
                ? "Admin Login"
                : "Welcome Back!";

            formDescription.textContent = isAdmin
                ? "Sign in to manage student placement data."
                : "Sign in to continue your placement journey.";

            loginBtn.textContent = isAdmin
                ? "Sign in to dashboard"
                : "Login";

            loginMessage.textContent = "";
            passwordInput.value = "";
        });
    });

    // ------------------------------------
    // Show / hide password
    // ------------------------------------
    if (togglePassword) {
        togglePassword.addEventListener("click", function () {
            const isPassword = passwordInput.type === "password";

            passwordInput.type = isPassword ? "text" : "password";

            togglePassword.classList.toggle(
                "fa-eye",
                !isPassword
            );

            togglePassword.classList.toggle(
                "fa-eye-slash",
                isPassword
            );
        });
    }

    // ------------------------------------
    // Login form submission
    // ------------------------------------
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const password = passwordInput.value;

        if (!password) {
            showMessage("Please enter your password.", true);
            return;
        }

        let body;

        if (currentRole === "admin") {
            const username = usernameInput.value.trim();

            if (!username) {
                showMessage("Please enter admin username.", true);
                return;
            }

            body =
                "username=" + encodeURIComponent(username) +
                "&password=" + encodeURIComponent(password);
        } else {
            const email = emailInput.value.trim();

            if (!email) {
                showMessage("Please enter your email address.", true);
                return;
            }

            body =
                "email=" + encodeURIComponent(email) +
                "&password=" + encodeURIComponent(password);
        }

        loginBtn.disabled = true;
        loginBtn.textContent = "Signing in...";
        showMessage(
            currentRole === "admin"
                ? "Checking admin credentials..."
                : "Checking student credentials...",
            false
        );

        try {
            const endpoint = currentRole === "admin"
                ? "/api/admin/login"
                : "/api/login";

            const response = await fetch(API_BASE + endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: body
            });

            const responseText = await response.text();

            console.log("Login endpoint:", endpoint);
            console.log("Login status:", response.status);
            console.log("Login response:", responseText);

            if (!response.ok) {
                showMessage(
                    "Login failed (" + response.status + "): " +
                    responseText,
                    true
                );
                return;
            }

            // ADMIN LOGIN
            if (currentRole === "admin") {
                let result;

                try {
                    result = JSON.parse(responseText);
                } catch (error) {
                    showMessage(
                        "Admin API returned an unexpected response. Check AdminHandler.java.",
                        true
                    );
                    return;
                }

                if (result.success && result.token) {
                    sessionStorage.setItem(
                        "careerscopeAdminToken",
                        result.token
                    );

                    showMessage("Login successful! Opening dashboard...", false);

                    window.location.href = "admin.html";
                } else {
                    showMessage(
                        result.message || "Invalid admin username or password.",
                        true
                    );
                }

                return;
            }

            // STUDENT LOGIN - preserve existing backend response format
            if (responseText.startsWith("LOGIN_SUCCESS|")) {
                const parts = responseText.split("|");
                const studentId = parts[1];
                const studentName = parts.slice(2).join("|");

                localStorage.setItem("studentId", studentId);
                localStorage.setItem("studentName", studentName);

                showMessage("Login successful! Opening dashboard...", false);

                window.location.href = "dashboard.html";
            } else {
                showMessage(responseText || "Student login failed.", true);
            }
        } catch (error) {
            console.error("Login error:", error);

            showMessage(
                "Cannot connect to the backend. Make sure CareerScopeServer.java is running on port 8080.",
                true
            );
        } finally {
            loginBtn.disabled = false;
            loginBtn.textContent = currentRole === "admin"
                ? "Sign in to dashboard"
                : "Login";
        }
    });

    // ------------------------------------
    // Google login placeholder
    // ------------------------------------
    if (googleLoginBtn) {
        googleLoginBtn.addEventListener("click", function () {
            alert("Google Login setup is not configured yet.");
        });
    }

    // ------------------------------------
    // Forgot password placeholder
    // ------------------------------------
    const forgotPassword = document.getElementById("forgotPassword");

    if (forgotPassword) {
        forgotPassword.addEventListener("click", function (event) {
            event.preventDefault();
            alert("Please contact the CareerScope administrator to reset your password.");
        });
    }

    // ------------------------------------
    // Display login messages
    // ------------------------------------
    function showMessage(message, isError) {
        loginMessage.textContent = message;
        loginMessage.style.color = isError ? "#ff6b6b" : "#65d6a3";
    }
});