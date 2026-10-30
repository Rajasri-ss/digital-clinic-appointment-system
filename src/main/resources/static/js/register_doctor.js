/**
 * register_doctor.js
 * Handles form logic for doctor registration — profile photo preview,
 * password strength, field validation, and API submission.
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
    const pwd  = document.getElementById('password').value;
    const bar  = document.getElementById('strengthBar');
    const text = document.getElementById('strengthText');

    let strength = 0;
    if (pwd.length > 5)           strength += 25;
    if (pwd.match(/[a-z]+/))      strength += 25;
    if (pwd.match(/[A-Z]+/))      strength += 25;
    if (pwd.match(/[@$!%*#?&]+/)) strength += 25;

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

// ── Profile photo preview ────────────────────────────────────────────────────
function previewPhoto(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            const preview = document.getElementById('photoPreview');
            preview.src = e.target.result;
            preview.style.display = 'block';
            document.getElementById('cameraIcon').style.display = 'none';
            document.getElementById('uploadText').innerText = 'Click to change photo';
        };
        reader.readAsDataURL(input.files[0]);
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
document.getElementById('registerDoctorForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const fullName      = document.getElementById('fullName').value.trim();
    const email         = document.getElementById('email').value.trim();
    const phone         = document.getElementById('phone').value.trim();
    const specialization = document.getElementById('specialization').value.trim();
    const licenseNumber = document.getElementById('licenseNumber').value.trim();
    const password      = document.getElementById('password').value;
    const confirm       = document.getElementById('confirmPassword').value;
    const terms         = document.getElementById('terms').checked;

    document.getElementById('errorMessage').style.display = 'none';

    // Validation
    if (!fullName || !email || !phone || !specialization || !licenseNumber || !password) {
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

    /**
     * Use FormData to support the profile photo file upload.
     * The backend accepts multipart/form-data for this endpoint.
     */
    const formData = new FormData();

    // Account fields
    formData.append('fullName',       fullName);
    formData.append('email',          email);
    formData.append('phone',          phone);
    formData.append('password',       password);

    // Professional fields (map to doctors table columns)
    formData.append('specialization', specialization);
    formData.append('licenseNumber',  licenseNumber);
    formData.append('qualifications', document.getElementById('qualifications').value.trim() || '');
    formData.append('experienceYears', document.getElementById('experienceYears').value || 0);
    formData.append('languagesSpoken', document.getElementById('languagesSpoken').value.trim() || '');
    formData.append('consultationFee', document.getElementById('consultationFee').value || 0);
    formData.append('about',           document.getElementById('about').value.trim() || '');

    // Profile photo (if selected)
    const photoFile = document.getElementById('profilePhoto').files[0];
    if (photoFile) {
        formData.append('profilePhoto', photoFile);
    }

    try {
        const response = await fetch('/api/auth/register/doctor', {
            method: 'POST',
            // NOTE: Do NOT set Content-Type header — the browser sets it automatically
            // with the correct multipart boundary when using FormData.
            body: formData
        });

        const data = await response.json();

        if (response.ok) {
            alert('Registration successful! Your account is pending verification. Redirecting to login...');
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
