/**
 * login.js
 * Handles login form submission, password visibility toggle,
 * and role-based redirect after successful authentication.
 */

// ── Password toggle ──────────────────────────────────────────────────────────
function togglePassword() {
    const pwdInput = document.getElementById('password');
    const eyeIcon  = document.querySelector('.fa-eye') || document.querySelector('.fa-eye-slash');
    if (pwdInput.type === 'password') {
        pwdInput.type = 'text';
        eyeIcon.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        pwdInput.type = 'password';
        eyeIcon.classList.replace('fa-eye-slash', 'fa-eye');
    }
}

// ── Login form submit ────────────────────────────────────────────────────────
document.getElementById('loginForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const btn          = document.getElementById('loginBtn');
    const originalHTML = btn.innerHTML;
    const errEl        = document.getElementById('errorMessage');

    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
    btn.disabled  = true;
    errEl.style.display = 'none';

    const payload = {
        identifier: document.getElementById('identifier').value.trim(),
        password:   document.getElementById('password').value
    };

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (response.ok) {
            // Persist session info
            localStorage.setItem('token',     data.token);
            localStorage.setItem('userRole',  data.role);
            localStorage.setItem('userId',    data.id);
            localStorage.setItem('userEmail', data.email);

            // Route to the correct dashboard based on role
            if (data.role === 'PATIENT') {
                window.location.href = 'patient_dashboard.html';
            } else if (data.role === 'DOCTOR') {
                window.location.href = 'doctor_dashboard.html';
            } else {
                window.location.href = 'admin_dashboard.html';
            }
        } else {
            errEl.innerText      = data.error || 'Invalid credentials. Please try again.';
            errEl.style.display  = 'block';
            resetBtn();
        }
    } catch (err) {
        errEl.innerText     = 'Server error. Please try again later.';
        errEl.style.display = 'block';
        resetBtn();
    }

    function resetBtn() {
        btn.innerHTML = originalHTML;
        btn.disabled  = false;
    }
});