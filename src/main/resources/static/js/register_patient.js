/**
 * register_patient.js
 * Handles form logic for patient registration — password strength,
 * field validation, and API submission.
 */

// ── Password toggle ──────────────────────────────────────────────────────────
function togglePassword(fieldId, iconEl) {
    const input = document.getElementById(fieldId);
    if (input.type === 'password') {
        input.type = 'text';
        iconEl.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        input.type = 'password';
        iconEl.classList.replace('fa-eye-slash', 'fa-eye');
    }
}

// ── Password strength meter ──────────────────────────────────────────────────
function checkStrength() {
    const pwd = document.getElementById('password').value;
    const bar = document.getElementById('strengthBar');
    const text = document.getElementById('strengthText');

    let strength = 0;
    if (pwd.length > 5)              strength += 25;
    if (pwd.match(/[a-z]+/))         strength += 25;
    if (pwd.match(/[A-Z]+/))         strength += 25;
    if (pwd.match(/[@$!%*#?&]+/))    strength += 25;

    bar.style.width = strength + '%';

    if (strength <= 25) {
        bar.style.background = '#EF4444';
        text.innerText = 'Weak';
    } else if (strength <= 50) {
        bar.style.background = '#F59E0B';
        text.innerText = 'Fair';
    } else if (strength <= 75) {
        bar.style.background = '#3B82F6';
        text.innerText = 'Good';
    } else {
        bar.style.background = '#10B981';
        text.innerText = 'Strong';
    }
}

// ── Error helper ─────────────────────────────────────────────────────────────
function showError(msg) {
    const el = document.getElementById('errorMessage');
    el.innerText = msg;
    el.style.display = 'block';
}

function resetBtn() {
    document.getElementById('btnText').style.display   = 'block';
    document.getElementById('btnLoader').style.display = 'none';
    document.getElementById('registerBtn').disabled    = false;
}

// ── Form submit ──────────────────────────────────────────────────────────────
document.getElementById('registerPatientForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const fullName = document.getElementById('fullName').value.trim();
    const email    = document.getElementById('email').value.trim();
    const phone    = document.getElementById('phone').value.trim();
    const password = document.getElementById('password').value;
    const confirm  = document.getElementById('confirmPassword').value;
    const terms    = document.getElementById('terms').checked;
    const errEl    = document.getElementById('errorMessage');

    errEl.style.display = 'none';

    // Validation
    if (!fullName || !email || !phone || !password) {
        showError('Please fill all required fields.');
        return;
    }

    if (password !== confirm) {
        showError('Passwords do not match.');
        return;
    }

    if (!terms) {
        showError('You must agree to the Terms of Service and Privacy Policy.');
        return;
    }

    // Show loader
    document.getElementById('btnText').style.display   = 'none';
    document.getElementById('btnLoader').style.display = 'block';
    document.getElementById('registerBtn').disabled    = true;

    const payload = {
        role:       'PATIENT',
        fullName:   fullName,
        email:      email,
        phone:      phone,
        password:   password,
        dob:        document.getElementById('dob').value        || null,
        gender:     document.getElementById('gender').value     || null,
        bloodGroup: document.getElementById('bloodGroup').value || null,
        address:    document.getElementById('address').value    || null,
        city:       document.getElementById('city').value       || null,
        state:      document.getElementById('state').value      || null,
        pincode:    document.getElementById('pincode').value    || null
    };

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (response.ok) {
            alert('Registration successful! Redirecting to login...');
            window.location.href = 'login.html';
        } else {
            showError(data.error || 'Registration failed. Please try again.');
            resetBtn();
        }
    } catch (err) {
        showError('Server error. Please try again later.');
        resetBtn();
    }
});
