function selectRole(role, element) {
            document.querySelectorAll('.role-card').forEach(card => card.classList.remove('active'));
            element.classList.add('active');

            // Toggle form fields based on role
            const patientFields = document.querySelectorAll('.patient-fields');
            if (role === 'PATIENT') {
                patientFields.forEach(el => el.style.display = window.innerWidth > 768 && el.classList.contains('form-row') ? 'grid' : 'block');
            } else {
                patientFields.forEach(el => el.style.display = 'none');
            }
        }

        function togglePassword() {
            const pwdInput = document.getElementById('password');
            const eyeIcon = document.querySelector('.fa-eye');
            if (pwdInput.type === 'password') {
                pwdInput.type = 'text';
                eyeIcon.classList.remove('fa-eye');
                eyeIcon.classList.add('fa-eye-slash');
            } else {
                pwdInput.type = 'password';
                eyeIcon.classList.remove('fa-eye-slash');
                eyeIcon.classList.add('fa-eye');
            }
        }

        function checkStrength() {
            const pwd = document.getElementById('password').value;
            const bar = document.getElementById('strengthBar');
            const text = document.getElementById('strengthText');

            let strength = 0;
            if (pwd.length > 5) strength += 25;
            if (pwd.match(/[a-z]+/)) strength += 25;
            if (pwd.match(/[A-Z]+/)) strength += 25;
            if (pwd.match(/[@$!%*#?&]+/)) strength += 25;

            bar.style.width = strength + '%';

            if (strength <= 25) {
                bar.style.background = '#EF4444'; // Danger
                text.innerText = 'Weak';
            } else if (strength <= 50) {
                bar.style.background = '#F59E0B'; // Warning
                text.innerText = 'Fair';
            } else if (strength <= 75) {
                bar.style.background = '#3B82F6'; // Blue
                text.innerText = 'Good';
            } else {
                bar.style.background = '#10B981'; // Success
                text.innerText = 'Strong';
            }
        }

        function showOtp() {
            // Basic validation
            if (!document.getElementById('fullName').value || !document.getElementById('email').value ||
                !document.getElementById('phone').value || !document.getElementById('password').value) {
                alert("Please fill all required fields first.");
                return;
            }

            // Show OTP section simulate OTP sent
            document.getElementById('verifyBtn').style.display = 'none';
            document.getElementById('otpSection').style.display = 'block';
            document.getElementById('registerBtn').style.display = 'flex';
        }

        function moveToNext(current, event) {
            if (current.value.length === 1 && event.key !== 'Backspace') {
                let next = current.nextElementSibling;
                if (next) next.focus();
            } else if (event.key === 'Backspace') {
                let prev = current.previousElementSibling;
                if (prev) prev.focus();
            }
        }

        document.getElementById('registerForm').addEventListener('submit', async function (e) {
            e.preventDefault();

            // Combine DOM validation + OTP check
            const inputs = document.querySelectorAll('.otp-input');
            let otp = '';
            inputs.forEach(input => otp += input.value);
            if (otp.length < 4) {
                alert("Please enter a valid 4-digit OTP");
                return;
            }

            // Show loading
            document.getElementById('btnText').style.display = 'none';
            document.getElementById('btnLoader').style.display = 'block';
            document.getElementById('registerBtn').disabled = true;
            document.getElementById('errorMessage').style.display = 'none';

            const role = document.querySelector('input[name="role"]:checked').value;

            const payload = {
                fullName: document.getElementById('fullName').value,
                email: document.getElementById('email').value,
                phone: document.getElementById('phone').value,
                password: document.getElementById('password').value,
                role: role,
                dob: document.getElementById('dob') ? document.getElementById('dob').value : null,
                gender: document.getElementById('gender') ? document.getElementById('gender').value : null,
                bloodGroup: document.getElementById('bloodGroup') ? document.getElementById('bloodGroup').value : null
            };

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payload)
                });

                const data = await response.json();

                if (response.ok) {
                    // Success! 
                    alert('Registration successful! Redirecting to login...');
                    window.location.href = 'login.html';
                } else {
                    document.getElementById('errorMessage').innerText = data.error || 'Registration failed.';
                    document.getElementById('errorMessage').style.display = 'block';
                    resetBtn();
                }
            } catch (error) {
                document.getElementById('errorMessage').innerText = 'Server error. Please try again.';
                document.getElementById('errorMessage').style.display = 'block';
                resetBtn();
            }
        });

        function resetBtn() {
            document.getElementById('btnText').style.display = 'block';
            document.getElementById('btnLoader').style.display = 'none';
            document.getElementById('registerBtn').disabled = false;
        }
