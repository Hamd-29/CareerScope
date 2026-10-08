// =====================================================
// CAREERSCOPE LOGIN
// =====================================================

const loginForm = document.querySelector("form");


// =====================================================
// NORMAL EMAIL + PASSWORD LOGIN
// =====================================================

loginForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const email =
        document.getElementById("email").value.trim();

    const password =
        document.getElementById("password").value;


    if (email === "" || password === "") {

        alert("Please enter email and password.");

        return;
    }


    const data =
        "email=" + encodeURIComponent(email) +
        "&password=" + encodeURIComponent(password);


    try {

        const response = await fetch(
            "http://localhost:8080/api/login",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded"
                },

                body: data
            }
        );


        const result =
            await response.text();

        console.log(
            "Login Response:",
            result
        );


        // =============================================
        // LOGIN SUCCESS
        // =============================================

        if (
            response.ok &&
            result.startsWith("LOGIN_SUCCESS|")
        ) {

            const parts =
                result.split("|");


            const studentId =
                parts[1];

            const studentName =
                parts.slice(2).join("|");


            // Save student information

            localStorage.setItem(
                "studentId",
                studentId
            );

            localStorage.setItem(
                "studentName",
                studentName
            );


            alert(
                "Login successful!"
            );


            // Go to dashboard

            window.location.href =
                "dashboard.html";

        }


        // =============================================
        // LOGIN FAILED
        // =============================================

        else {

            alert(result);

        }

    }

    catch (error) {

        console.error(
            "Login Error:",
            error
        );


        alert(
            "Unable to connect to CareerScope server."
        );

    }

});


// =====================================================
// GOOGLE LOGIN BUTTON
// =====================================================

const googleLoginBtn =
    document.getElementById("googleLoginBtn");


if (googleLoginBtn) {

    googleLoginBtn.addEventListener(
        "click",
        function () {

            alert(
                "Google Login setup is not configured yet."
            );

        }
    );

}