
const loginCard = document.getElementById('loginCard');
const examCard = document.getElementById('examCard');
const resultCard = document.getElementById('resultCard');

let student = '';
let remaining = 300;
let timerId;

// Format timer
function fmt(t) {
    return String(Math.floor(t / 60)).padStart(2, '0') +
        ':' + String(t % 60).padStart(2, '0');
}

// Start countdown
function startTimer() {
    clearInterval(timerId);
    remaining = 300;

    document.getElementById('timer').textContent = fmt(remaining);

    timerId = setInterval(() => {
        remaining--;
        document.getElementById('timer').textContent = fmt(remaining);

        if (remaining <= 0) {
            clearInterval(timerId);
            submitExam();
        }
    }, 1000);
}

// Student login
document.getElementById('loginForm').addEventListener('submit', async (ev) => {
    ev.preventDefault();

    student = document.getElementById('studentName').value.trim();

    try {
        const r = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: 'name=' + encodeURIComponent(student)
        });

        const data = await r.json();

        if (!data.ok) {
            alert(data.message);
            return;
        }

        loginCard.classList.add('hidden');
        resultCard.classList.add('hidden');
        examCard.classList.remove('hidden');

        document.getElementById('studentWelcome').textContent =
            'Student: ' + student;

        await loadQuestions();
        startTimer();

    } catch (error) {
        alert('Unable to start exam. Please check the Java server.');
        console.error(error);
    }
});

// Load exam questions
async function loadQuestions() {
    const response = await fetch('/api/questions');
    const data = await response.json();

    const host = document.getElementById('questions');
    host.innerHTML = '';

    data.questions.forEach((q, i) => {
        const section = document.createElement('section');
        section.className = 'question';

        const title = document.createElement('h3');
        title.textContent = `Q${i + 1}. ${q.text}`;
        section.appendChild(title);

        q.options.forEach((opt, j) => {
            const label = document.createElement('label');
            label.className = 'option';

            const radio = document.createElement('input');
            radio.type = 'radio';
            radio.name = 'q' + i;
            radio.value = j;

            label.appendChild(radio);
            label.appendChild(document.createTextNode(' ' + opt));

            section.appendChild(label);
        });

        host.appendChild(section);
    });
}

// Submit exam
document.getElementById('examForm').addEventListener('submit', (ev) => {
    ev.preventDefault();
    submitExam();
});

async function submitExam() {
    if (examCard.classList.contains('hidden')) {
        return;
    }

    clearInterval(timerId);

    const fd = new FormData(document.getElementById('examForm'));
    const body = new URLSearchParams();

    for (let i = 0; i < 5; i++) {
        body.append('q' + i, fd.get('q' + i) ?? '-1');
    }

    try {
        const response = await fetch('/api/submit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: body
        });

        const data = await response.json();

        examCard.classList.add('hidden');
        resultCard.classList.remove('hidden');

        document.getElementById('resultName').textContent =
            'Student: ' + student;

        document.getElementById('score').textContent =
            `${data.score} / ${data.total} (${data.percent}%)`;

        document.getElementById('resultMessage').textContent =
            data.percent >= 40
                ? 'Congratulations! You passed.'
                : 'Keep practicing and try again!';

    } catch (error) {
        alert('Unable to submit exam.');
        console.error(error);
    }
}

// Cancel exam
document.getElementById('cancelBtn').addEventListener('click', () => {
    clearInterval(timerId);
    examCard.classList.add('hidden');
    loginCard.classList.remove('hidden');
});

// Restart exam
document.getElementById('restartBtn').addEventListener('click', () => {
    document.getElementById('examForm').reset();

    resultCard.classList.add('hidden');
    loginCard.classList.remove('hidden');
});
