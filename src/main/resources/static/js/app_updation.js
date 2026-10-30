document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    const urlParams = new URLSearchParams(window.location.search);
    const appointmentId = urlParams.get('id');

    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    if (!appointmentId) {
        showToast('Error: No appointment selected', 'danger');
        setTimeout(() => window.location.href = 'doctor_dashboard.html', 2000);
        return;
    }

    loadAppointmentDetails(appointmentId, token);
});

async function loadAppointmentDetails(id, token) {
    try {
        const response = await fetch(`/api/appointments/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("Could not load appointment details");

        const data = await response.json();
        renderPatientInfo(data);
        populateForm(data);
    } catch (e) {
        showToast(e.message, 'danger');
        console.error('Error loading appointment:', e);
    }
}

function renderPatientInfo(data) {
    const infoDiv = document.getElementById('patientInfo');
    
    // Calculate age if date of birth is available
    let ageDisplay = 'N/A';
    if (data.patientDateOfBirth) {
        const birthDate = new Date(data.patientDateOfBirth);
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        ageDisplay = age;
    }
    
    infoDiv.innerHTML = `
        <div class="patient-info-item">
            <label>Name</label>
            <span>${data.patientName || 'N/A'}</span>
        </div>
        <div class="patient-info-item">
            <label>Age</label>
            <span>${ageDisplay}</span>
        </div>
        <div class="patient-info-item">
            <label>Gender</label>
            <span>${data.patientGender || 'N/A'}</span>
        </div>
        <div class="patient-info-item">
            <label>Contact</label>
            <span>${data.patientPhone || 'N/A'}</span>
        </div>
        <div class="patient-info-item">
            <label>Blood Group</label>
            <span style="color: var(--danger); font-weight: 700;">${data.patientBloodGroup || 'N/A'}</span>
        </div>
        <div class="patient-info-item">
            <label>Appointment Date</label>
            <span>${formatDate(data.appointmentDate)}</span>
        </div>
        <div class="patient-info-item">
            <label>Appointment Time</label>
            <span>${formatTime(data.appointmentTime)}</span>
        </div>
        <div class="complaint-box">
            <label>REASON FOR VISIT</label>
            <p>${data.reasonForVisit || data.chiefComplaint || data.symptoms || 'No reason specified'}</p>
        </div>
    `;

    // Update status badge
    updateStatusBadge(data.status);
}

function populateForm(data) {
    // Populate diagnosis if exists
    if (data.diagnosis) {
        document.getElementById('diagnosis').value = data.diagnosis;
    }
    
    // Populate doctor notes if exists
    if (data.doctorNotes) {
        document.getElementById('doctorNotes').value = data.doctorNotes;
    }
    
    // Set current status
    if (data.status) {
        document.getElementById('status').value = data.status;
    }
}

function updateStatusBadge(status) {
    const badge = document.getElementById('statusBadge');
    const statusMap = {
        'SCHEDULED': 'badge-scheduled',
        'IN_PROGRESS': 'badge-in-progress',
        'COMPLETED': 'badge-completed',
        'CANCELLED': 'badge-cancelled',
        'NO_SHOW': 'badge-no-show',
        'CONFIRMED': 'badge-scheduled'
    };
    
    const badgeClass = statusMap[status] || 'badge-scheduled';
    badge.innerHTML = `<span class="badge ${badgeClass}">${status || 'SCHEDULED'}</span>`;
}

async function saveAssessment(event) {
    event.preventDefault();
    
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');
    const token = localStorage.getItem('token');
    
    const diagnosis = document.getElementById('diagnosis').value.trim();
    const doctorNotes = document.getElementById('doctorNotes').value.trim();
    const status = document.getElementById('status').value;

    if (!diagnosis) {
        showToast('Please enter a diagnosis', 'danger');
        return;
    }

    if (!status) {
        showToast('Please select an appointment status', 'danger');
        return;
    }

    const payload = {
        diagnosis: diagnosis,
        doctorNotes: doctorNotes,
        status: status
    };

    try {
        const response = await fetch(`/api/appointments/${id}`, {
            method: 'PUT',
            headers: { 
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Failed to update appointment");
        }
        
        showToast("Appointment updated successfully", "success");
        
        // Update the status badge
        updateStatusBadge(status);
        
        // Optionally redirect back to dashboard after a delay
        setTimeout(() => {
            window.location.href = 'doctor_dashboard.html';
        }, 1500);
        
    } catch (e) {
        showToast(e.message, "danger");
        console.error('Error saving assessment:', e);
    }
}

function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric' 
    });
}

function formatTime(timeStr) {
    if (!timeStr) return 'N/A';
    // Handle HH:mm:ss format
    const parts = timeStr.split(':');
    if (parts.length >= 2) {
        let hrs = parseInt(parts[0], 10);
        const mins = parts[1];
        const ampm = hrs >= 12 ? 'PM' : 'AM';
        hrs = hrs % 12;
        hrs = hrs ? hrs : 12;
        return `${hrs}:${mins} ${ampm}`;
    }
    return timeStr;
}

function showToast(message, type = 'success') {
    const container = document.getElementById('alertContainer');
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.style.borderLeftColor = type === 'success' ? 'var(--secondary)' : 'var(--danger)';
    
    const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-triangle';
    const color = type === 'success' ? 'var(--secondary)' : 'var(--danger)';

    toast.innerHTML = `
        <i class="fas ${icon}" style="color: ${color}; font-size: 1.25rem;"></i>
        <div style="font-weight: 500;">${message}</div>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(20px)';
        toast.style.transition = 'all 0.3s ease-out';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}
