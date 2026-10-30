document.addEventListener('DOMContentLoaded', () => {
    // 1. Validate Token and Get Appointment ID
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    const getParams = () => new URLSearchParams(window.location.search);
    let appointmentId = getParams().get('appointmentId');

    // Fallback logic if appointmentId isn't in URL (for testing)
    if (!appointmentId) {
        appointmentId = localStorage.getItem('pendingPaymentAppointmentId');
    }

    if (!appointmentId) {
        showError("No appointment selected for payment. Please return to the dashboard.");
        document.getElementById('payButton').disabled = true;
        return;
    }

    let appointmentAmount = 0;

    // 2. Fetch Appointment Details
    fetchAppointmentDetails(appointmentId, token);

    // 3. Handle Radio Button Toggles for Payment Methods
    const methodCards = document.querySelectorAll('.method-card');
    methodCards.forEach(card => {
        card.addEventListener('click', function () {
            methodCards.forEach(c => c.classList.remove('active'));
            this.classList.add('active');
            this.querySelector('input').checked = true;
        });
    });

    // 4. Form Submission (Mock Payment Gateway processing)
    const paymentForm = document.getElementById('paymentForm');
    paymentForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const payBtn = document.getElementById('payButton');
        payBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';
        payBtn.disabled = true;

        const provider = document.querySelector('input[name="provider"]:checked').value;
        const cardNumber = document.getElementById('cardNumber').value.replace(/\s+/g, '');

        const requestBody = {
            appointmentId: parseInt(appointmentId),
            amount: appointmentAmount,
            provider: provider,
            mockCardNumber: cardNumber
        };

        try {
            // Step 1: Create Payment Intent
            const intentResponse = await fetch('/api/payments/create-intent', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            const intentData = await intentResponse.json();

            if (!intentResponse.ok || intentData.status !== 'SUCCESS') {
                showError("Could not create payment intent.");
                return;
            }

            // Step 2: Confirm Payment
            const confirmResponse = await fetch(`/api/payments/confirm?patientId=${localStorage.getItem('userId')}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            const confirmData = await confirmResponse.json();

            if (confirmResponse.ok && confirmData.status === 'SUCCESS') {
                showSuccessModal(confirmData.transactionId);
            } else {
                showError(confirmData.message || "Payment failed. Please try again or use a different card.");
            }
        } catch (error) {
            console.error('Payment Error:', error);
            showError("A network error occurred while processing your payment.");
        } finally {
            payBtn.innerHTML = `<span>Pay </span><span id="btnAmount">$${appointmentAmount.toFixed(2)}</span> <i class="fa-solid fa-lock"></i>`;
            payBtn.disabled = false;
        }
    });

    // 5. Success Modal Return Action
    document.getElementById('returnDashboardBtn').addEventListener('click', () => {
        const role = localStorage.getItem('role') || 'PATIENT';
        if (role === 'PATIENT') {
            window.location.href = '/patient_dashboard.html';
        } else {
            window.location.href = '/doctor_dashboard.html';
        }
    });

    // --- Helper Functions ---

    async function fetchAppointmentDetails(id, token) {
        try {
            const role = localStorage.getItem('role') || 'PATIENT';
            const response = await fetch(`/api/appointments/${id}?role=${role}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error("Could not fetch appointment details");
            }

            const data = await response.json();

            // Check if already paid
            if (data.paymentStatus === 'PAID') {
                showError("This appointment has already been paid for.");
                document.getElementById('payButton').disabled = true;
            }

            // Populate Summary UI
            document.getElementById('summaryDoctorName').textContent = data.doctorName || 'Dr. Assigned';
            document.getElementById('summarySpecialty').textContent = data.doctorSpecialization || 'Specialist';

            document.getElementById('summaryDate').textContent = formatDate(data.appointmentDate);
            document.getElementById('summaryTime').textContent = formatTime(data.appointmentTime);
            document.getElementById('summaryType').textContent = formatType(data.consultationType);

            // Calculate Totals (Mocking Consultation Fee if null to 150)
            const fee = data.consultationFee || 150.00;
            const platformFee = 2.00;
            appointmentAmount = fee + platformFee;

            document.getElementById('summaryFee').textContent = `$${fee.toFixed(2)}`;
            document.getElementById('summaryTotal').textContent = `$${appointmentAmount.toFixed(2)}`;
            document.getElementById('btnAmount').textContent = `$${appointmentAmount.toFixed(2)}`;

        } catch (error) {
            console.error("Error loading details:", error);
            showError("Unable to load appointment details. Please refresh.");
        }
    }

    function showError(msg) {
        const alertBox = document.getElementById('paymentAlert');
        alertBox.textContent = msg;
        alertBox.className = 'alert error';
        alertBox.classList.remove('hidden');
    }

    function showSuccessModal(txId) {
        document.getElementById('modalTxId').textContent = txId;
        document.getElementById('successModal').classList.remove('hidden');
        document.getElementById('paymentAlert').classList.add('hidden');
    }

    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    function formatTime(timeStr) {
        if (!timeStr) return 'N/A';
        const [hours, minutes] = timeStr.split(':');
        const date = new Date();
        date.setHours(hours, minutes);
        return date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
    }

    function formatType(type) {
        if (!type) return 'In-Person';
        return type.charAt(0).toUpperCase() + type.slice(1).toLowerCase().replace('_', '-');
    }

    // Input masking for visual effect
    document.getElementById('cardNumber').addEventListener('input', function (e) {
        e.target.value = e.target.value.replace(/[^\d]/g, '').replace(/(.{4})/g, '$1 ').trim();
    });

    document.getElementById('expiry').addEventListener('input', function (e) {
        let val = e.target.value.replace(/[^\d]/g, '');
        if (val.length > 2) {
            val = val.substring(0, 2) + '/' + val.substring(2, 4);
        }
        e.target.value = val;
    });
});
