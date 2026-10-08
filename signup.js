const signupForm = document.getElementById("signupForm");

signupForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const name = document.getElementById("name").value.trim();
    const prn = document.getElementById("prn").value.trim();
    const email = document.getElementById("email").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const branch = document.getElementById("branch").value;
    const semester = document.getElementById("semester").value;
    const cgpa = parseFloat(document.getElementById("cgpa").value);
    const password = document.getElementById("password").value;
    const confirmPassword =
        document.getElementById("confirmPassword").value;


    // ================= PASSWORD CHECK =================

    if (password !== confirmPassword) {
        alert("Passwords do not match.");
        return;
    }


    // ================= CGPA CHECK =================

    if (isNaN(cgpa) || cgpa < 0 || cgpa > 10) {
        alert("CGPA must be between 0 and 10.");
        return;
    }


    // ================= PHONE CHECK =================

    if (!/^[0-9]{10}$/.test(phone)) {
        alert("Please enter a valid 10-digit phone number.");
        return;
    }


    // ================= PREPARE DATA =================

    const data =
        "name=" + encodeURIComponent(name) +
        "&prn=" + encodeURIComponent(prn) +
        "&email=" + encodeURIComponent(email) +
        "&phone=" + encodeURIComponent(phone) +
        "&branch=" + encodeURIComponent(branch) +
        "&semester=" + encodeURIComponent(semester) +
        "&cgpa=" + encodeURIComponent(cgpa) +
        "&password=" + encodeURIComponent(password);


    try {

        const response = await fetch(
            "http://localhost:8080/api/signup",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded"
                },

                body: data
            }
        );


        const result = await response.text();

        console.log("Signup Response:", result);


        // ================= SUCCESS =================

        if (response.ok) {

            /*
             * Backend response is currently unknown.
             * We will inspect it before creating
             * the Profile Setup redirect.
             */

            alert("Account created successfully!");

            console.log(
                "Backend returned:",
                result
            );

            signupForm.reset();


        } else {

            alert(result);

        }


    } catch (error) {

        console.error(
            "Signup Error:",
            error
        );

        alert(
            "Unable to connect to CareerScope server."
        );

    }

});