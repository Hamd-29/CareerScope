// ==================================================
// CAREERSCOPE DASHBOARD
// ==================================================


// ==================================================
// GET STUDENT ID
// ==================================================

function getStudentId() {

    return localStorage.getItem("studentId");

}


// ==================================================
// GET SCORE
// ==================================================

function getScore(student) {

    const value =
        student.score ??
        student.overallScore ??
        student.overall_score ??
        student.placementScore ??
        student.placement_score ??
        0;

    const score =
        parseFloat(value);

    return Number.isFinite(score)
        ? score
        : 0;

}


// ==================================================
// GET NUMBER
// ==================================================

function getNumber(value) {

    const number =
        parseFloat(value);

    return Number.isFinite(number)
        ? number
        : 0;

}


// ==================================================
// ESCAPE HTML
// ==================================================

function escapeHtml(value) {

    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");

}


// ==================================================
// LOAD DASHBOARD
// ==================================================

async function loadDashboard() {

    const studentId =
        getStudentId();


    // ==================================================
    // CHECK LOGIN
    // ==================================================

    if (!studentId) {

        window.location.href =
            "login.html";

        return;

    }


    try {

        // ==================================================
        // GET STUDENT DASHBOARD DATA
        // ==================================================

        const response =
            await fetch(
                "http://localhost:8080/api/dashboard?studentId=" +
                encodeURIComponent(studentId)
            );


        if (!response.ok) {

            const errorText =
                await response.text();

            throw new Error(
                "Dashboard API Error: " +
                errorText
            );

        }


        const student =
            await response.json();


        console.log(
            "CareerScope Dashboard Data:",
            student
        );


        // ==================================================
        // BASIC DATA
        // ==================================================

        const studentName =
            student.name ||
            localStorage.getItem("studentName") ||
            "Student";


        const branch =
            student.branch ||
            "Student";


        const cgpa =
            getNumber(
                student.cgpa
            );


        const overallScore =
            getScore(student);


        const parameterScore =
            getNumber(
                student.parameterScore ??
                student.parameter_score
            );


        const cgpaScore =
            getNumber(
                student.cgpaScore ??
                student.cgpa_score
            );


        // ==================================================
        // PROFILE MINI
        // ==================================================

        const profileName =
            document.querySelector(
                ".profile-mini strong"
            );


        const profileBranch =
            document.querySelector(
                ".profile-mini span"
            );


        const avatar =
            document.querySelector(
                ".avatar"
            );


        if (profileName) {

            profileName.textContent =
                studentName;

        }


        if (profileBranch) {

            profileBranch.textContent =
                branch;

        }


        if (avatar) {

            const initials =
                studentName
                    .trim()
                    .split(/\s+/)
                    .map(
                        word =>
                            word.charAt(0)
                    )
                    .slice(0, 2)
                    .join("")
                    .toUpperCase();


            avatar.textContent =
                initials || "S";

        }


        // ==================================================
        // WELCOME
        // ==================================================

        const welcomeTitle =
            document.querySelector(
                ".welcome-card h2"
            );


        if (welcomeTitle) {

            welcomeTitle.textContent =
                "Welcome back, " +
                studentName +
                "!";

        }


        const welcomeScore =
            document.querySelector(
                ".welcome-score strong"
            );


        if (welcomeScore) {

            welcomeScore.textContent =
                overallScore.toFixed(2);

        }


        // ==================================================
        // STAT CARDS
        // ==================================================

        const statCards =
            document.querySelectorAll(
                ".stat-card"
            );


        // CGPA

        if (statCards[0]) {

            const value =
                statCards[0]
                    .querySelector("h3");


            if (value) {

                value.textContent =
                    cgpa.toFixed(2);

            }

        }


        // Parameter Score

        if (statCards[1]) {

            const value =
                statCards[1]
                    .querySelector("h3");


            if (value) {

                value.textContent =
                    parameterScore.toFixed(2);

            }

        }


        // Career Interest

        if (statCards[3]) {

            const value =
                statCards[3]
                    .querySelector("h3");


            if (value) {

                value.textContent =
                    student.careerInterest ??
                    student.career_interest ??
                    "Not Set";

            }

        }


        // ==================================================
        // SCORE CIRCLE
        // ==================================================

        const scoreCircle =
            document.querySelector(
                ".score-circle strong"
            );


        if (scoreCircle) {

            scoreCircle.textContent =
                overallScore.toFixed(2);

        }


        // ==================================================
        // SCORE BREAKDOWN
        // ==================================================

        const scoreRows =
            document.querySelectorAll(
                ".score-row"
            );


        // CGPA SCORE

        if (scoreRows[0]) {

            const value =
                scoreRows[0]
                    .querySelector("strong");


            if (value) {

                value.textContent =
                    cgpaScore.toFixed(2);

            }

        }


        // PARAMETER SCORE

        if (scoreRows[1]) {

            const value =
                scoreRows[1]
                    .querySelector("strong");


            if (value) {

                value.textContent =
                    parameterScore.toFixed(2);

            }

        }


        // ==================================================
        // SCORE FORMULA
        // ==================================================
        //
        // CGPA SCORE = /5
        // PARAMETER SCORE = /5
        // TOTAL = /10
        //
        // ==================================================

        const formula =
            document.querySelector(
                ".formula strong"
            );


        if (formula) {

            formula.textContent =
                cgpaScore.toFixed(2) +
                " + " +
                parameterScore.toFixed(2) +
                " = " +
                overallScore.toFixed(2);

        }


        // ==================================================
        // PROGRESS BARS
        // ==================================================

        const progressBars =
            document.querySelectorAll(
                ".score-details .progress"
            );


        // CGPA PROGRESS

        if (progressBars[0]) {

            const fill =
                progressBars[0]
                    .querySelector(
                        ".progress-fill"
                    );


            if (fill) {

                const width =
                    Math.min(
                        Math.max(
                            (cgpa / 10) * 100,
                            0
                        ),
                        100
                    );


                fill.style.width =
                    width + "%";

            }

        }


        // PARAMETER PROGRESS
        // Parameter score maximum = 5

        if (progressBars[1]) {

            const fill =
                progressBars[1]
                    .querySelector(
                        ".progress-fill"
                    );


            if (fill) {

                const width =
                    Math.min(
                        Math.max(
                            (parameterScore / 5) * 100,
                            0
                        ),
                        100
                    );


                fill.style.width =
                    width + "%";

            }

        }


        // ==================================================
        // PARAMETER LEVELS
        // ==================================================

        const parameterLevels = [

            student.programmingLevel ??
            student.programming_level ??
            "None",

            student.dsaLevel ??
            student.dsa_level ??
            "None",

            student.communicationLevel ??
            student.communication_level ??
            "None",

            student.aptitudeLevel ??
            student.aptitude_level ??
            "None",

            student.projectLevel ??
            student.project_level ??
            "None"

        ];


        const parameters =
            document.querySelectorAll(
                ".parameter"
            );


        parameters.forEach(
            function(
                parameter,
                index
            ) {

                const level =
                    parameter.querySelector(
                        ".parameter-top strong"
                    );


                if (level) {

                    level.textContent =
                        parameterLevels[index] ||
                        "None";

                }

            }
        );


        // ==================================================
        // PARAMETER PROGRESS
        // ==================================================

        const parameterBars =
            document.querySelectorAll(
                ".parameter .progress-fill"
            );


        parameterBars.forEach(
            function(
                bar,
                index
            ) {

                const level =
                    parameterLevels[index] ||
                    "None";


                bar.classList.remove(
                    "parameter-1",
                    "parameter-2",
                    "parameter-3"
                );


                if (
                    level === "None" ||
                    level === "Not Set" ||
                    !level
                ) {

                    bar.style.width =
                        "0%";

                }

                else if (
                    level === "Basic"
                ) {

                    bar.classList.add(
                        "parameter-1"
                    );

                    bar.style.width =
                        "33%";

                }

                else if (
                    level === "Moderate"
                ) {

                    bar.classList.add(
                        "parameter-2"
                    );

                    bar.style.width =
                        "66%";

                }

                else if (
                    level === "High"
                ) {

                    bar.classList.add(
                        "parameter-3"
                    );

                    bar.style.width =
                        "100%";

                }

                else if (
                    level === "Good"
                ) {

                    bar.classList.add(
                        "parameter-2"
                    );

                    bar.style.width =
                        "66%";

                }

                else if (
                    level === "Strong"
                ) {

                    bar.classList.add(
                        "parameter-3"
                    );

                    bar.style.width =
                        "100%";

                }

                else {

                    bar.style.width =
                        "0%";

                }

            }
        );


        // ==================================================
        // LOAD RANKING
        // ==================================================

        const rankingResponse =
            await fetch(
                "http://localhost:8080/api/ranking"
            );


        if (!rankingResponse.ok) {

            throw new Error(
                "Ranking API failed"
            );

        }


        const ranking =
            await rankingResponse.json();


        console.log(
            "CareerScope Ranking:",
            ranking
        );


        // ==================================================
        // FIND CURRENT STUDENT
        // ==================================================

        const currentStudent =
            ranking.find(
                function(item) {

                    return Number(
                        item.studentId ??
                        item.student_id
                    ) ===
                    Number(studentId);

                }
            );


        // ==================================================
        // CURRENT RANK
        // ==================================================

        if (statCards[2]) {

            const rankValue =
                statCards[2]
                    .querySelector("h3");


            if (rankValue) {

                if (currentStudent) {

                    rankValue.textContent =
                        "#" +
                        (
                            currentStudent.rank ??
                            "--"
                        );

                }

                else {

                    rankValue.textContent =
                        "--";

                }

            }

        }


        // ==================================================
        // DISPLAY RANKING
        // ==================================================

        loadRanking(
            ranking
        );


        // ==================================================
        // TOP CANDIDATES
        // ==================================================

        loadTopCandidates(
            ranking
        );


    } catch (error) {

        console.error(
            "CareerScope Dashboard Error:",
            error
        );


        const rankingList =
            document.querySelector(
                ".ranking-list"
            );


        if (rankingList) {

            rankingList.innerHTML =
                `
                <p style="color:#ff5c5c;font-size:12px;">
                    Unable to load ranking.
                </p>
                `;

        }


        const candidateList =
            document.querySelector(
                "#candidateList"
            );


        if (candidateList) {

            candidateList.innerHTML =
                `
                <p style="color:#ff5c5c;font-size:12px;">
                    Unable to load candidates.
                </p>
                `;

        }

    }

}


// ==================================================
// LOAD RANKING
// ==================================================

function loadRanking(
    ranking
) {

    const rankingList =
        document.querySelector(
            ".ranking-list"
        );


    if (!rankingList) {
        return;
    }


    rankingList.innerHTML =
        "";


    const currentStudentId =
        Number(
            localStorage.getItem(
                "studentId"
            )
        );


    if (
        !Array.isArray(ranking) ||
        ranking.length === 0
    ) {

        rankingList.innerHTML =
            `
            <p style="font-size:12px;color:#8D9AAC;">
                No ranking data available.
            </p>
            `;

        return;

    }


    ranking
        .slice(0, 4)
        .forEach(
            function(student) {

                const item =
                    document.createElement(
                        "div"
                    );


                item.className =
                    "rank-item";


                const studentId =
                    Number(
                        student.studentId ??
                        student.student_id
                    );


                const isCurrentStudent =
                    studentId ===
                    currentStudentId;


                if (
                    isCurrentStudent
                ) {

                    item.classList.add(
                        "current-student"
                    );

                }


                const rank =
                    Number(
                        student.rank || 0
                    );


                let rankClass =
                    "";


                if (rank === 1) {

                    rankClass =
                        "rank-first";

                }

                else if (rank === 2) {

                    rankClass =
                        "rank-second";

                }

                else if (rank === 3) {

                    rankClass =
                        "rank-third";

                }


                const name =
                    student.name ||
                    "Student";


                const score =
                    getScore(
                        student
                    ).toFixed(2);


                item.innerHTML =
                    `
                    <span class="rank-number ${rankClass}">
                        ${rank}
                    </span>

                    <div class="student-info">

                        <strong>
                            ${escapeHtml(name)}

                            ${
                                isCurrentStudent
                                    ? '<b class="you-badge">You</b>'
                                    : ''
                            }

                        </strong>

                        <span>
                            Student ID:
                            ${studentId}
                        </span>

                    </div>

                    <strong class="rank-score">
                        ${score}
                    </strong>
                    `;


                rankingList.appendChild(
                    item
                );

            }
        );

}


// ==================================================
// LOAD TOP CANDIDATES
// ==================================================

function loadTopCandidates(
    ranking
) {

    const candidateList =
        document.getElementById(
            "candidateList"
        );


    if (!candidateList) {
        return;
    }


    candidateList.innerHTML =
        "";


    if (
        !Array.isArray(ranking) ||
        ranking.length === 0
    ) {

        candidateList.innerHTML =
            `
            <p style="font-size:12px;color:#8D9AAC;">
                No candidates available.
            </p>
            `;

        return;

    }


    ranking
        .slice(0, 3)
        .forEach(
            function(student) {

                const candidate =
                    document.createElement(
                        "div"
                    );


                candidate.className =
                    "candidate";


                const name =
                    student.name ||
                    "Student";


                const studentId =
                    Number(
                        student.studentId ??
                        student.student_id
                    );


                const rank =
                    Number(
                        student.rank || 0
                    );


                const score =
                    getScore(
                        student
                    ).toFixed(2);


                candidate.innerHTML =
                    `
                    <div class="candidate-rank">
                        ${rank}
                    </div>

                    <div class="student-info">

                        <strong>
                            ${escapeHtml(name)}
                        </strong>

                        <span>
                            Student ID:
                            ${studentId}
                        </span>

                    </div>

                    <strong>
                        ${score}
                    </strong>
                    `;


                candidateList.appendChild(
                    candidate
                );

            }
        );

}


// ==================================================
// LOGOUT
// ==================================================

const logoutButton =
    document.getElementById(
        "logoutButton"
    );


if (logoutButton) {

    logoutButton.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            localStorage.removeItem(
                "studentId"
            );


            localStorage.removeItem(
                "studentName"
            );


            window.location.href =
                "login.html";

        }
    );

}


// ==================================================
// DASHBOARD NAVIGATION
// ==================================================

const dashboardLink =
    document.getElementById(
        "dashboardLink"
    );


if (dashboardLink) {

    dashboardLink.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            window.scrollTo({

                top: 0,

                behavior: "smooth"

            });

        }
    );

}


// ==================================================
// PROFILE
// ==================================================

const profileLink =
    document.getElementById(
        "profileLink"
    );


if (profileLink) {

    profileLink.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            window.location.href =
                "profile.html";

        }
    );

}


// ==================================================
// MY SCORE
// ==================================================

const scoreLink =
    document.getElementById(
        "scoreLink"
    );


if (scoreLink) {

    scoreLink.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            const section =
                document.getElementById(
                    "scoreSection"
                );


            if (section) {

                section.scrollIntoView({

                    behavior: "smooth"

                });

            }

        }
    );

}


// ==================================================
// RANKINGS
// ==================================================

const rankingLink =
    document.getElementById(
        "rankingLink"
    );


if (rankingLink) {

    rankingLink.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            const section =
                document.getElementById(
                    "rankingSection"
                );


            if (section) {

                section.scrollIntoView({

                    behavior: "smooth"

                });

            }

        }
    );

}


// ==================================================
// TOP CANDIDATES
// ==================================================

const candidateLink =
    document.getElementById(
        "candidateLink"
    );


if (candidateLink) {

    candidateLink.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            const section =
                document.getElementById(
                    "candidateSection"
                );


            if (section) {

                section.scrollIntoView({

                    behavior: "smooth"

                });

            }

        }
    );

}


// ==================================================
// SIMULATOR
// ==================================================

const simulatorLink =
    document.getElementById(
        "simulatorLink"
    );


if (simulatorLink) {

    simulatorLink.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            window.location.href =
                "simulator.html";

        }
    );

}


// ==================================================
// VIEW ALL RANKING
// ==================================================

const viewAllRanking =
    document.getElementById(
        "viewAllRanking"
    );


if (viewAllRanking) {

    viewAllRanking.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            const section =
                document.getElementById(
                    "rankingSection"
                );


            if (section) {

                section.scrollIntoView({

                    behavior: "smooth"

                });

            }

        }
    );

}


// ==================================================
// SETTINGS
// ==================================================

const settingsLink =
    document.getElementById(
        "settingsLink"
    );


if (settingsLink) {

    settingsLink.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            alert(
                "Settings will be available soon."
            );

        }
    );

}


// ==================================================
// START DASHBOARD
// ==================================================

document.addEventListener(
    "DOMContentLoaded",
    function() {

        loadDashboard();

    }
);