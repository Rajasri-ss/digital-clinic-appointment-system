document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.removeItem('token');
        window.location.href = 'login.html';
    });

    // updateClock();
    // setInterval(updateClock, 1000);

    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    document.getElementById('currentDate').textContent = new Date().toLocaleDateString(undefined, options);

    fetchDashboardData(token);
});

// function updateClock() {
//     const now = new Date();
//     document.getElementById('clock').textContent = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
// }

async function fetchDashboardData(token) {
    try {
        const response = await fetch('/api/doctors/dashboard', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            throw new Error("Failed to load dashboard data");
        }

        const dashboardData = await response.json();
        
        // Update generic welcome text (can be improved later with doctor name)
        document.getElementById('welcomeText').textContent = "Welcome back, Doctor!";

        processDashboard(dashboardData);

    } catch (error) {
        console.error("Dashboard error:", error);
        document.getElementById('loader').innerHTML = `<p class="text-danger flex items-center gap-2"><i class="fas fa-exclamation-triangle"></i> Error loading dashboard.</p>`;
    }
}

function processDashboard(data) {
    const metrics = data.metrics || {};
    
    document.getElementById('statToday').textContent = metrics.todayAppointments || 0;
    document.getElementById('statCompleted').textContent = metrics.completedAppointments || 0;
    document.getElementById('statPending').textContent = metrics.pendingAppointments || 0;
    document.getElementById('statEmergency').textContent = (data.emergencyAppointments ? data.emergencyAppointments.length : 0);

    renderEmergencyAlerts(data.emergencyAppointments || []);
    renderTimeline(data.todayAppointments || []);
}

function renderEmergencyAlerts(emergencies) {
    const alertsContainer = document.getElementById('emergencyAlerts');
    if (emergencies.length > 0) {
        let html = `<h3><i class="fas fa-exclamation-triangle"></i> ${emergencies.length} Emergency Alert(s)</h3>`;
        emergencies.forEach(app => {
            const timeStr = formatTime(app.appointmentTime);
            html += `
                <div class="alert-item">
                    <div>
                        <strong>${app.patientName || app.patient?.name || 'Patient'}</strong> - ${app.reason || 'Emergency Priority'}
                    </div>
                    <div style="color:var(--danger); font-weight:bold;">${timeStr}</div>
                </div>
            `;
        });
        alertsContainer.innerHTML = html;
        alertsContainer.classList.remove('hidden');
    } else {
        alertsContainer.classList.add('hidden');
    }
}

function renderTimeline(appointments) {
    document.getElementById('loader').classList.add('hidden');
    const timeline = document.getElementById('queueList');
    const noAppts = document.getElementById('noAppointments');

    if (appointments.length === 0) {
        noAppts.classList.remove('hidden');
        timeline.innerHTML = '';
        return;
    }

    noAppts.classList.add('hidden');
    let html = '';

    appointments.forEach(app => {
        const isEmergency = app.isEmergency === true || (app.priority && app.priority.toLowerCase() === 'high');
        const isCompleted = app.status === 'COMPLETED';
        let itemClass = '';
        let badge = '';

        if (isEmergency && !isCompleted) itemClass = 'emergency';
        else if (isCompleted) itemClass = 'completed';

        if (isCompleted) badge = `<span class="badge" style="background:var(--secondary-light); color:var(--secondary); padding: 2px 8px; border-radius:12px; font-size: 0.75rem;">Done</span>`;
        if (isEmergency && !isCompleted) badge = `<span class="badge" style="background:var(--danger); color:white; padding: 2px 8px; border-radius:12px; font-size: 0.75rem;"><i class="fas fa-bolt"></i> Urgent</span>`;

        html += `
            <div class="queue-item ${itemClass}" onclick="window.location.href='app_updation.html?id=${app.appointmentId}'" style="cursor: pointer;">
                <div class="item-content">
                    <div class="item-header">
                        <span class="item-time"><i class="far fa-clock"></i> ${formatTime(app.appointmentTime)}</span>
                        ${badge}
                    </div>
                    <div class="item-patient">${app.patientName || 'Unknown Patient'}</div>
                    <div class="item-reason">${app.reasonForVisit || 'General Checkup'}</div>
                </div>
            </div>
        `;
    });

    timeline.innerHTML = html;
}

function formatTime(timeStr) {
    if (!timeStr) return '--:--';
    // Handle HH:mm:ss to HH:mm AM/PM
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

function refreshData() {
    const token = localStorage.getItem('token');
    document.getElementById('loader').classList.remove('hidden');
    document.getElementById('queueList').innerHTML = '';
    fetchDashboardData(token);
}

// Clinic Management Functions
let clinicsVisible = false;

function toggleClinicManagement() {
    const card = document.getElementById('clinicManagementCard');
    clinicsVisible = !clinicsVisible;
    
    if (clinicsVisible) {
        card.style.display = 'block';
        loadClinics();
    } else {
        card.style.display = 'none';
    }
}

async function loadClinics() {
    const token = localStorage.getItem('token');
    try {
        const response = await fetch('/api/doctors/me/clinics', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            throw new Error('Failed to load clinics');
        }

        const clinics = await response.json();
        renderClinics(clinics);
    } catch (error) {
        console.error('Error loading clinics:', error);
        document.getElementById('clinicsList').innerHTML = '<p style="color: var(--danger);">Failed to load clinics</p>';
    }
}

function renderClinics(clinics) {
    const container = document.getElementById('clinicsList');
    
    if (clinics.length === 0) {
        container.innerHTML = '<p style="color: #6B7280; text-align: center;">No clinics added yet</p>';
        return;
    }

    let html = '';
    clinics.forEach(clinic => {
        html += `
            <div style="border: 1px solid #E5E7EB; border-radius: 8px; padding: 1rem; margin-bottom: 0.5rem;">
                <div style="display: flex; justify-content: space-between; align-items: start;">
                    <div style="flex: 1;">
                        <h4 style="margin: 0 0 0.5rem 0; color: var(--dark);">${clinic.clinicName}</h4>
                        <p style="margin: 0; font-size: 0.875rem; color: #6B7280;">${clinic.address}</p>
                        <p style="margin: 0.25rem 0 0 0; font-size: 0.875rem; color: #6B7280;">${clinic.city}, ${clinic.state} - ${clinic.pincode}</p>
                        ${clinic.phone ? `<p style="margin: 0.25rem 0 0 0; font-size: 0.875rem; color: #6B7280;"><i class="fas fa-phone"></i> ${clinic.phone}</p>` : ''}
                        ${clinic.consultationFeeAtThisLocation ? `<p style="margin: 0.5rem 0 0 0; font-weight: 600; color: var(--primary);">Fee: ₹${clinic.consultationFeeAtThisLocation}</p>` : ''}
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        <button onclick="editClinic(${clinic.locationId})" style="padding: 0.5rem; border: none; background: var(--primary-light); color: var(--primary); border-radius: 6px; cursor: pointer;" title="Edit">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button onclick="deleteClinic(${clinic.locationId})" style="padding: 0.5rem; border: none; background: var(--danger-light); color: var(--danger); border-radius: 6px; cursor: pointer;" title="Delete">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

function showAddClinicForm() {
    document.getElementById('clinicModalTitle').textContent = 'Add Clinic';
    document.getElementById('clinicForm').reset();
    document.getElementById('clinicId').value = '';
    document.getElementById('clinicModal').style.display = 'flex';
}

async function editClinic(clinicId) {
    const token = localStorage.getItem('token');
    try {
        const response = await fetch('/api/doctors/me/clinics', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to load clinic');

        const clinics = await response.json();
        const clinic = clinics.find(c => c.locationId === clinicId);

        if (!clinic) throw new Error('Clinic not found');

        document.getElementById('clinicModalTitle').textContent = 'Edit Clinic';
        document.getElementById('clinicId').value = clinic.locationId;
        document.getElementById('clinicName').value = clinic.clinicName;
        document.getElementById('clinicAddress').value = clinic.address;
        document.getElementById('clinicCity').value = clinic.city;
        document.getElementById('clinicState').value = clinic.state;
        document.getElementById('clinicPincode').value = clinic.pincode;
        document.getElementById('clinicPhone').value = clinic.phone || '';
        document.getElementById('clinicFee').value = clinic.consultationFeeAtThisLocation || '';
        document.getElementById('clinicModal').style.display = 'flex';
    } catch (error) {
        console.error('Error loading clinic:', error);
        alert('Failed to load clinic details');
    }
}

async function deleteClinic(clinicId) {
    if (!confirm('Are you sure you want to remove this clinic?')) return;

    const token = localStorage.getItem('token');
    try {
        const response = await fetch(`/api/doctors/clinics/${clinicId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to delete clinic');

        loadClinics();
    } catch (error) {
        console.error('Error deleting clinic:', error);
        alert('Failed to delete clinic');
    }
}

function closeClinicModal() {
    document.getElementById('clinicModal').style.display = 'none';
}

document.getElementById('clinicForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const token = localStorage.getItem('token');
    const clinicId = document.getElementById('clinicId').value;
    const isEdit = clinicId !== '';

    const clinicData = {
        clinicName: document.getElementById('clinicName').value,
        address: document.getElementById('clinicAddress').value,
        city: document.getElementById('clinicCity').value,
        state: document.getElementById('clinicState').value,
        pincode: document.getElementById('clinicPincode').value,
        phone: document.getElementById('clinicPhone').value,
        consultationFeeAtThisLocation: document.getElementById('clinicFee').value ? parseFloat(document.getElementById('clinicFee').value) : null
    };

    try {
        const url = isEdit ? `/api/doctors/clinics/${clinicId}` : '/api/doctors/clinics';
        const method = isEdit ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(clinicData)
        });

        if (!response.ok) throw new Error('Failed to save clinic');

        closeClinicModal();
        loadClinics();
    } catch (error) {
        console.error('Error saving clinic:', error);
        alert('Failed to save clinic');
    }
});

