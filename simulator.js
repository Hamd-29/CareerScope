// ==================================================
// CAREERSCOPE - PLACEMENT SIMULATOR
// MAX HEAP RANKING
// ==================================================

let students = [];
let heap = [];
let removedStudents = [];
let simulationStarted = false;
let simulationTimer = null;


// ==================================================
// GET SCORE SAFELY
// ==================================================

function getScore(student) {

    const value =
        student.overall_score ??
        student.overallScore ??
        student.score ??
        student.placement_score ??
        student.placementScore ??
        0;

    const score = parseFloat(value);

    return Number.isFinite(score) ? score : 0;
}


// ==================================================
// GET STUDENT NAME SAFELY
// ==================================================

function getStudentName(student) {

    return (
        student.name ??
        student.student_name ??
        student.studentName ??
        "Unknown Student"
    );

}


// ==================================================
// LOAD STUDENTS FROM BACKEND
// ==================================================

async function loadStudents() {

    try {

        const response =
            await fetch("http://localhost:8080/api/simulator");

        if (!response.ok) {
            throw new Error("Backend response failed");
        }

        students = await response.json();

        console.log("Students loaded:", students);

        // Check actual backend data
        if (students.length > 0) {
            console.log("First student:", students[0]);
            console.log("First student score:", getScore(students[0]));
        }

        resetSimulation();

    } catch (error) {

        console.error("Error loading students:", error);

        const statusText =
            document.getElementById("statusText");

        if (statusText) {
            statusText.textContent =
                "Unable to load students.";
        }

    }

}


// ==================================================
// MAX HEAP - INSERT
// ==================================================

function insertHeap(student) {

    heap.push(student);

    let index = heap.length - 1;

    while (index > 0) {

        const parent =
            Math.floor((index - 1) / 2);

        if (
            getScore(heap[parent]) >=
            getScore(heap[index])
        ) {
            break;
        }

        [
            heap[parent],
            heap[index]
        ] = [
            heap[index],
            heap[parent]
        ];

        index = parent;
    }

}


// ==================================================
// MAX HEAP - REMOVE MAX
// ==================================================

function removeMax() {

    if (heap.length === 0) {
        return null;
    }

    if (heap.length === 1) {
        return heap.pop();
    }

    const max = heap[0];

    heap[0] = heap.pop();

    heapifyDown(0);

    return max;

}


// ==================================================
// HEAPIFY DOWN
// ==================================================

function heapifyDown(index) {

    const length = heap.length;

    while (true) {

        let largest = index;

        const left =
            2 * index + 1;

        const right =
            2 * index + 2;


        if (
            left < length &&
            getScore(heap[left]) >
            getScore(heap[largest])
        ) {

            largest = left;

        }


        if (
            right < length &&
            getScore(heap[right]) >
            getScore(heap[largest])
        ) {

            largest = right;

        }


        if (largest === index) {
            break;
        }


        [
            heap[index],
            heap[largest]
        ] = [
            heap[largest],
            heap[index]
        ];


        index = largest;

    }

}


// ==================================================
// START SIMULATION
// ==================================================

function startSimulation() {

    if (students.length === 0) {

        alert("No students available for simulation.");

        return;

    }


    if (simulationTimer) {

        clearInterval(simulationTimer);

        simulationTimer = null;

    }


    heap = [];
    removedStudents = [];
    simulationStarted = true;


    const rankingList =
        document.getElementById("rankingList");

    if (rankingList) {
        rankingList.innerHTML = "";
    }


    students.forEach(student => {

        insertHeap(student);

    });


    renderHeap();


    const statusText =
        document.getElementById("statusText");

    if (statusText) {

        statusText.textContent =
            "Simulation running...";

    }


    const startBtn =
        document.querySelector(".start-btn");

    if (startBtn) {

        startBtn.disabled = true;

    }


    // AUTOMATIC RUN

    simulationTimer =
        setInterval(() => {

            if (heap.length === 0) {

                clearInterval(simulationTimer);

                simulationTimer = null;

                if (statusText) {

                    statusText.textContent =
                        "Simulation completed.";

                }

                if (startBtn) {

                    startBtn.disabled = false;

                }

                return;

            }

            nextStep();

        }, 1200);

}


// ==================================================
// NEXT STEP
// ==================================================

function nextStep() {

    if (heap.length === 0) {

        if (simulationTimer) {

            clearInterval(simulationTimer);

            simulationTimer = null;

        }

        return;

    }


    const maxStudent =
        removeMax();


    if (!maxStudent) {
        return;
    }


    removedStudents.push(maxStudent);


    // CURRENT MAX

    const maxStudentElement =
        document.getElementById("maxStudent");

    const maxScoreElement =
        document.getElementById("maxScore");


    if (maxStudentElement) {

        maxStudentElement.textContent =
            getStudentName(maxStudent);

    }


    if (maxScoreElement) {

        maxScoreElement.textContent =
            getScore(maxStudent).toFixed(2);

    }


    // RANKING

    renderRanking();


    // HEAP

    renderHeap();


    // COUNT

    const candidateCount =
        document.getElementById("candidateCount");

    if (candidateCount) {

        candidateCount.textContent =
            removedStudents.length;

    }


    // STATUS

    const statusText =
        document.getElementById("statusText");

    if (statusText) {

        statusText.textContent =
            `Rank ${removedStudents.length}: ${getStudentName(maxStudent)}`;

    }


    // FINISHED

    if (heap.length === 0) {

        if (simulationTimer) {

            clearInterval(simulationTimer);

            simulationTimer = null;

        }


        if (statusText) {

            statusText.textContent =
                "Simulation completed.";

        }


        const startBtn =
            document.querySelector(".start-btn");

        if (startBtn) {

            startBtn.disabled = false;

        }

    }

}


// ==================================================
// RENDER HEAP
// ==================================================

function renderHeap() {

    const container =
        document.getElementById("heapContainer");

    if (!container) {
        return;
    }


    container.innerHTML = "";


    // EMPTY HEAP

    if (heap.length === 0) {

        container.innerHTML = `
            <div class="empty-heap">
                Heap is empty
            </div>
        `;

        const heapSize =
            document.getElementById("heapSize");

        if (heapSize) {
            heapSize.textContent = "0";
        }

        return;

    }


    // HEAP SIZE

    const heapSize =
        document.getElementById("heapSize");

    if (heapSize) {

        heapSize.textContent =
            heap.length;

    }


    // ==================================================
    // TREE
    // ==================================================

    const tree =
        document.createElement("div");

    tree.className =
        "heap-tree";


    const maxLevel =
        Math.floor(Math.log2(heap.length));


    const bottomCount =
        Math.pow(2, maxLevel);


    const treeWidth =
        Math.max(
            650,
            bottomCount * 100
        );


    const levelHeight = 85;


    tree.style.width =
        treeWidth + "px";


    tree.style.height =
        ((maxLevel + 1) * levelHeight + 50) + "px";


    tree.style.position =
        "relative";


    // IMPORTANT:
    // Add tree to DOM BEFORE calculating SVG dimensions

    container.appendChild(tree);


    // ==================================================
    // SVG
    // ==================================================

    const svg =
        document.createElementNS(
            "http://www.w3.org/2000/svg",
            "svg"
        );


    svg.classList.add("heap-arrows");


    svg.setAttribute(
        "width",
        treeWidth
    );


    svg.setAttribute(
        "height",
        tree.offsetHeight
    );


    svg.setAttribute(
        "viewBox",
        `0 0 ${treeWidth} ${tree.offsetHeight}`
    );


    svg.style.position =
        "absolute";

    svg.style.top =
        "0";

    svg.style.left =
        "0";

    svg.style.width =
        "100%";

    svg.style.height =
        "100%";

    svg.style.pointerEvents =
        "none";

    svg.style.zIndex =
        "1";


    tree.appendChild(svg);


    // ==================================================
    // CREATE NODES
    // ==================================================

    const nodeElements = [];


    heap.forEach((student, index) => {

        const node =
            document.createElement("div");


        node.className =
            "heap-node";


        if (index === 0) {

            node.classList.add("root");

        }


        // TREE LEVEL

        const level =
            Math.floor(
                Math.log2(index + 1)
            );


        const firstIndex =
            Math.pow(2, level) - 1;


        const position =
            index - firstIndex;


        const nodesInLevel =
            Math.pow(2, level);


        const spacing =
            treeWidth / nodesInLevel;


        const x =
            (position + 0.5) * spacing;


        const y =
            level * levelHeight;


        node.style.position =
            "absolute";


        node.style.left =
            (x - 24) + "px";


        node.style.top =
            y + "px";


        node.style.zIndex =
            "3";


        // SCORE

        const score =
            document.createElement("div");


        score.className =
            "node-score";


        score.textContent =
            getScore(student).toFixed(2);


        // NAME

        const name =
            document.createElement("div");


        name.className =
            "node-name";


        name.textContent =
            getStudentName(student);


        node.appendChild(score);

        node.appendChild(name);


        tree.appendChild(node);

        nodeElements.push(node);

    });


    // ==================================================
    // DRAW CONNECTION LINES
    // ==================================================

    heap.forEach((student, index) => {

        if (index === 0) {
            return;
        }


        const parentIndex =
            Math.floor(
                (index - 1) / 2
            );


        const parentNode =
            nodeElements[parentIndex];


        const childNode =
            nodeElements[index];


        if (!parentNode || !childNode) {
            return;
        }


        const x1 =
            parentNode.offsetLeft +
            parentNode.offsetWidth / 2;


        const y1 =
            parentNode.offsetTop +
            parentNode.offsetHeight;


        const x2 =
            childNode.offsetLeft +
            childNode.offsetWidth / 2;


        const y2 =
            childNode.offsetTop;


        // LINE

        const line =
            document.createElementNS(
                "http://www.w3.org/2000/svg",
                "line"
            );


        line.setAttribute(
            "x1",
            x1
        );

        line.setAttribute(
            "y1",
            y1
        );

        line.setAttribute(
            "x2",
            x2
        );

        line.setAttribute(
            "y2",
            y2
        );


        line.classList.add(
            "heap-arrow"
        );


        svg.appendChild(line);

    });

}


// ==================================================
// RENDER RANKING
// ==================================================

function renderRanking() {

    const rankingList =
        document.getElementById("rankingList");

    if (!rankingList) {
        return;
    }


    rankingList.innerHTML = "";


    removedStudents.forEach(
        (student, index) => {

            const row =
                document.createElement("div");


            row.className =
                "ranking-item";


            row.innerHTML = `

                <div class="ranking-rank">
                    ${index + 1}
                </div>

                <div class="ranking-name">
                    ${getStudentName(student)}
                </div>

                <div class="ranking-score">
                    ${getScore(student).toFixed(2)}
                </div>

            `;


            rankingList.appendChild(row);

        }
    );

}


// ==================================================
// RESET SIMULATION
// ==================================================

function resetSimulation() {

    if (simulationTimer) {

        clearInterval(simulationTimer);

        simulationTimer = null;

    }


    simulationStarted = false;

    heap = [];

    removedStudents = [];


    students.forEach(student => {

        insertHeap(student);

    });


    renderHeap();


    const rankingList =
        document.getElementById("rankingList");

    if (rankingList) {

        rankingList.innerHTML = `
            <p>
                Start simulation to generate ranking.
            </p>
        `;

    }


    const maxStudent =
        document.getElementById("maxStudent");

    if (maxStudent) {

        maxStudent.textContent =
            "--";

    }


    const maxScore =
        document.getElementById("maxScore");

    if (maxScore) {

        maxScore.textContent =
            "0.00";

    }


    const candidateCount =
        document.getElementById("candidateCount");

    if (candidateCount) {

        candidateCount.textContent =
            "0";

    }


    const statusText =
        document.getElementById("statusText");

    if (statusText) {

        statusText.textContent =
            "Ready to start simulation.";

    }


    const startBtn =
        document.querySelector(".start-btn");

    if (startBtn) {

        startBtn.disabled = false;

    }

}


// ==================================================
// LOGOUT
// ==================================================

function logout() {

    if (simulationTimer) {

        clearInterval(simulationTimer);

        simulationTimer = null;

    }


    window.location.href =
        "login.html";

}


// ==================================================
// INITIALIZE
// ==================================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        loadStudents();

    }
);