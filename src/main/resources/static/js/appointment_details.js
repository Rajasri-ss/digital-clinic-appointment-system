document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const appointmentId = urlParams.get('id');

    if (!appointmentId) {
        showError("No appointment ID provided.");
        return;
    }

    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.removeItem('token');
        window.location.href = 'login.html';
    });

    fetchAppointmentDetails(appointmentId, token);
});

async function fetchAppointmentDetails(id, token) {
    try {
        const response = await fetch(`/api/appointments/${id}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error("Failed to load appointment details.");
        }

        const data = await response.json();
        renderDetails(data, id);
    } catch (error) {
        showError(error.message);
    }
}

function renderDetails(data, id) {
    document.getElementById('loader').classList.add('hidden');
    document.getElementById('details-content').classList.remove('hidden');

    document.getElementById('appointment-id-display').textContent = id;
    document.getElementById('patient-name').textContent = data.patientName || data.patient?.name || 'Unknown Patient';
    document.getElementById('patient-contact').textContent = data.patientContact || data.patient?.phone || 'Loading...';

    document.getElementById('doctor-name').textContent = `Dr. ${data.doctorName || data.doctor?.name || 'Unknown'}`;
    document.getElementById('doctor-specialty').textContent = data.doctorSpecialty || data.doctor?.specialty || '--';

    const dateObj = new Date(data.appointmentDate);
    document.getElementById('appointment-date').textContent = dateObj.toLocaleDateString();
    document.getElementById('appointment-time').textContent = data.appointmentTime || dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    document.getElementById('appointment-reason').textContent = data.reason || 'Not specified';

    // Status
    const statusLower = data.status.toLowerCase();
    const statusBadge = document.getElementById('appointment-status-badge');
    statusBadge.textContent = data.status;
    statusBadge.className = `badge badge-${statusLower}`;

    // QR Code (<img src="/api/appointments/{id}/qrcode">)
    const qrImg = document.getElementById('qr-code-img');
    const qrFallback = document.getElementById('qr-fallback');
    if (statusLower === 'confirmed') {
        qrImg.src = `/api/appointments/${id}/qrcode`;
        /* Assuming backend uses token via header, but img src uses cookies or we append token as query if needed. 
           If the prompt specifically says <img src="/api/appointments/{id}/qrcode"> without token we just set it. 
           Since it's highly specific in the prompt, I'll use exactly that. */
        qrImg.classList.remove('hidden');
        qrFallback.classList.add('hidden');
    } else {
        qrImg.classList.add('hidden');
        qrFallback.classList.remove('hidden');
    }

    // Timeline
    renderTimeline(statusLower, data);

    // Actions
    const actionPanel = document.getElementById('action-buttons');
    if (statusLower === 'pending' || statusLower === 'confirmed') {
        actionPanel.innerHTML = `
            <button class="btn btn-outline-danger" onclick="openCancelModal()">
                <i class="fas fa-times"></i> Cancel Appointment
            </button>
        `;
    } else {
        actionPanel.innerHTML = `<p class="text-muted">No actions available for this status.</p>`;
    }

    // Cancel setup
    document.getElementById('confirmCancelBtn').onclick = () => cancelAppointment(id);
}

function renderTimeline(status, data) {
    const timeline = document.getElementById('status-timeline');
    let html = '';

    const steps = [
        { key: 'pending', label: 'Appointment Requested' },
        { key: 'confirmed', label: 'Confirmed by Clinic' },
        { key: 'completed', label: 'Appointment Completed' }
    ];

    if (status === 'cancelled') {
        html += `<div class="timeline-item active">
            <div class="timeline-title">Requested</div>
        </div>`;
        html += `<div class="timeline-item active">
            <div class="timeline-title" style="color: var(--danger);">Cancelled</div>
        </div>`;
    } else {
        let reached = true;
        steps.forEach(step => {
            if (reached) {
                html += `<div class="timeline-item active">
                    <div class="timeline-title">${step.label}</div>
                </div>`;
                if (step.key === status) reached = false;
            } else {
                html += `<div class="timeline-item">
                    <div class="timeline-title">${step.label}</div>
                </div>`;
            }
        });
    }

    timeline.innerHTML = html;
}

function showError(msg) {
    document.getElementById('loader').classList.add('hidden');
    const err = document.getElementById('error-message');
    err.classList.remove('hidden');
    err.textContent = msg;
}

// Modal logic
const modal = document.getElementById('cancelModal');
const closeBtn = document.querySelector('.close');

function openCancelModal() { modal.style.display = 'flex'; }
function closeCancelModal() { modal.style.display = 'none'; }
closeBtn.onclick = closeCancelModal;
window.onclick = function (event) {
    if (event.target == modal) {
        closeCancelModal();
    }
}

async function cancelAppointment(id) {
    const token = localStorage.getItem('token');
    const reason = document.getElementById('cancel-reason').value;
    try {
        const response = await fetch(`/api/appointments/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ reason })
        });

        if (!response.ok) throw new Error("Could not cancel appointment");

        closeCancelModal();
        window.location.reload();
    } catch (e) {
        alert(e.message);
    }
}
