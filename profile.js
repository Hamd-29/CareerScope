// ========================================
// CAREERSCOPE PROFILE
// ========================================


// ========================================
// GET LOGGED-IN STUDENT
// ========================================

const studentId =
    localStorage.getItem("studentId");


// ========================================
// CHECK LOGIN
// ========================================

if (!studentId) {

    alert("Please login first.");

    window.location.href =
        "login.html";
}


// ========================================
// LOAD PROFILE
// ========================================

async function loadProfile() {

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/dashboard?studentId=" +
                encodeURIComponent(studentId)
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load profile"
            );
        }


        const student =
            await response.json();


        // ========================================
        // PERSONAL INFORMATION
        // ========================================

        document.getElementById("name").value =
            student.name || "";

        document.getElementById("email").value =
            student.email || "";

        document.getElementById("college").value =
            student.branch || "";

        document.getElementById("year").value =
            student.semester || "";

        document.getElementById("prn").value =
            student.prn || "";

        document.getElementById("phone").value =
            student.phone || "";

        document.getElementById("cgpa").value =
            Number(student.cgpa || 0).toFixed(2);


        // ========================================
        // PROFILE HEADER
        // ========================================

        document.getElementById(
            "profileName"
        ).textContent =
            student.name || "Student";


        document.getElementById(
            "profileBranch"
        ).textContent =
            student.branch || "Computer Engineering";


        const firstLetter =
            (student.name || "S")
            .charAt(0)
            .toUpperCase();


        document.getElementById(
            "profileAvatar"
        ).textContent =
            firstLetter;


        // ========================================
        // CAREER PREPARATION
        // ========================================

        setSelectValue(
            "programming",
            student.programmingLevel
        );


        setSelectValue(
            "dsa",
            student.dsaLevel
        );


        setSelectValue(
            "communication",
            student.communicationLevel
        );


        setSelectValue(
            "aptitude",
            student.aptitudeLevel
        );


        setSelectValue(
            "projects",
            student.projectLevel
        );


        setSelectValue(
            "certifications",
            student.certificationLevel
        );


        setSelectValue(
            "experience",
            student.experienceLevel
        );


        document.getElementById(
            "interest"
        ).value =
            student.careerInterest || "";


        document.getElementById(
            "technicalSkills"
        ).value =
            student.technicalSkills || "";


        // ========================================
        // PLACEMENT SCORE
        // ========================================

        document.getElementById(
            "placementScore"
        ).textContent =
            Number(
                student.overallScore || 0
            ).toFixed(2);


    } catch (error) {

        console.error(
            "Profile Load Error:",
            error
        );


        alert(
            "Unable to load profile. " +
            "Please make sure the CareerScope server is running."
        );
    }
}



// ========================================
// SET SELECT VALUE
// ========================================

function setSelectValue(
    id,
    value
) {

    const select =
        document.getElementById(id);


    if (!select || !value) {
        return;
    }


    const optionExists =
        Array.from(
            select.options
        ).some(
            option =>
                option.value === value
        );


    if (optionExists) {

        select.value =
            value;
    }
}



// ========================================
// ENABLE EDIT
// ========================================

function enableEdit() {

    const editableFields = [

        "name",
        "email",
        "phone",

        "programming",
        "dsa",
        "communication",
        "aptitude",

        "projects",
        "certifications",
        "experience",

        "interest",
        "technicalSkills"
    ];


    editableFields.forEach(
        function (id) {

            const element =
                document.getElementById(id);


            if (element) {

                element.disabled =
                    false;
            }

        }
    );


    // ========================================
    // VISUAL FEEDBACK
    // ========================================

    alert(
        "Profile editing enabled."
    );
}



// ========================================
// SAVE PROFILE
// ========================================

async function saveProfile() {


    // ========================================
    // GET VALUES
    // ========================================

    const name =
        document.getElementById(
            "name"
        ).value.trim();


    const email =
        document.getElementById(
            "email"
        ).value.trim();


    const phone =
        document.getElementById(
            "phone"
        ).value.trim();


    const programming =
        document.getElementById(
            "programming"
        ).value;


    const dsa =
        document.getElementById(
            "dsa"
        ).value;


    const communication =
        document.getElementById(
            "communication"
        ).value;


    const aptitude =
        document.getElementById(
            "aptitude"
        ).value;


    const project =
        document.getElementById(
            "projects"
        ).value;


    const certification =
        document.getElementById(
            "certifications"
        ).value;


    const experience =
        document.getElementById(
            "experience"
        ).value;


    const careerInterest =
        document.getElementById(
            "interest"
        ).value.trim();


    const technicalSkills =
        document.getElementById(
            "technicalSkills"
        ).value.trim();



    // ========================================
    // VALIDATION
    // ========================================

    if (
        name === "" ||
        email === "" ||
        phone === ""
    ) {

        alert(
            "Please fill Name, Email and Phone."
        );

        return;
    }



    // ========================================
    // CREATE FORM DATA
    // ========================================

    const formData =
        new URLSearchParams();


    formData.append(
        "studentId",
        studentId
    );


    formData.append(
        "name",
        name
    );


    formData.append(
        "email",
        email
    );


    formData.append(
        "phone",
        phone
    );


    formData.append(
        "programming",
        programming
    );


    formData.append(
        "dsa",
        dsa
    );


    formData.append(
        "communication",
        communication
    );


    formData.append(
        "aptitude",
        aptitude
    );


    formData.append(
        "project",
        project
    );


    formData.append(
        "certification",
        certification
    );


    formData.append(
        "experience",
        experience
    );


    formData.append(
        "careerInterest",
        careerInterest
    );


    formData.append(
        "technicalSkills",
        technicalSkills
    );



    // ========================================
    // SEND TO JAVA BACKEND
    // ========================================

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/profile/update",
                {

                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/x-www-form-urlencoded"

                    },

                    body:
                        formData.toString()

                }
            );


        const result =
            await response.text();



        // ========================================
        // SUCCESS
        // ========================================

        if (
            response.ok &&
            result === "PROFILE_UPDATED"
        ) {


            // Update local name

            localStorage.setItem(
                "studentName",
                name
            );


            alert(
                "Profile updated successfully!"
            );


            // ========================================
            // DISABLE FIELDS AGAIN
            // ========================================

            const editableFields = [

                "name",
                "email",
                "phone",

                "programming",
                "dsa",
                "communication",
                "aptitude",

                "projects",
                "certifications",
                "experience",

                "interest",
                "technicalSkills"

            ];


            editableFields.forEach(
                function (id) {

                    const element =
                        document.getElementById(id);


                    if (element) {

                        element.disabled =
                            true;
                    }

                }
            );


            // ========================================
            // RELOAD DATA FROM DATABASE
            // ========================================

            await loadProfile();


        } else {


            // ========================================
            // UPDATE FAILED
            // ========================================

            alert(
                "Profile update failed: " +
                result
            );

        }


    } catch (error) {

        console.error(
            "Profile Update Error:",
            error
        );


        alert(
            "Unable to connect to CareerScope server."
        );

    }

}



// ========================================
// LOAD PROFILE WHEN PAGE OPENS
// ========================================

loadProfile();
