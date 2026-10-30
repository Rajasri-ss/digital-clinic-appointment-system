function nextStep(step) {
            document.getElementById('step1').style.display = 'none';
            document.getElementById('step2').style.display = 'none';
            document.getElementById('step3').style.display = 'none';

            document.getElementById('step' + step).style.display = 'block';
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

        function finishReset() {
            let pass1 = document.getElementById('newPassword').value;
            let pass2 = document.getElementById('confirmPassword').value;

            if (pass1 && pass1 === pass2) {
                alert("Password reset successfully! Redirecting to login.");
                // Here we would call the actual API endpoint
                window.location.href = 'login.html';
            } else {
                alert("Passwords do not match or are empty.");
            }
        }
