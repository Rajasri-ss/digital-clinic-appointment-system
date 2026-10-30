document.addEventListener('DOMContentLoaded', ()=> {
    const token=localStorage.getItem('token');
    const role=localStorage.getItem('userRole');

    if ( !token || role !=='PATIENT') {
        window.location.href='login.html';
        return;
    }

    const userEmail = localStorage.getItem('userEmail');
    document.getElementById('userName').innerText = userEmail || 'Patient User';
    
    // Fetch dashboard data
    fetchDashboardData(token);
});

async function fetchDashboardData(token) {
    try {
        const response = await fetch('/api/patients/dashboard', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            console.error('Failed to load dashboard data');
            return;
        }

        const data = await response.json();
        updateDashboard(data);

    } catch (error) {
        console.error('Dashboard error:', error);
    }
}

function updateDashboard(data) {
    // Update welcome name
    if (data.patientName) {
        const firstName = data.patientName.split(' ')[0];
        document.getElementById('welcomeName').textContent = firstName;
        document.getElementById('userInitial').textContent = firstName.charAt(0).toUpperCase();
    }

    // Update stats
    const upcomingCount = data.upcomingAppointments || 0;
    const completedCount = data.completedAppointments || 0;
    
    document.querySelector('.stat-card:nth-child(1) h3').textContent = upcomingCount;
    document.querySelector('.stat-card:nth-child(2) h3').textContent = completedCount;

    // Update health snapshot
    if (data.bloodGroup) {
        document.querySelector('.health-item:nth-child(1) .health-value').textContent = data.bloodGroup;
    }
    if (data.allergies) {
        document.querySelector('.health-item:nth-child(2) .health-value').textContent = data.allergies;
    }
    if (data.emergencyContact && data.emergencyContactName) {
        document.querySelector('.health-item:nth-child(3) .health-value').textContent = data.emergencyContact;
        document.querySelector('.health-item:nth-child(3) .health-subtext').textContent = `(${data.emergencyContactName})`;
    }

    // Update recent appointments
    if (data.recentAppointments && data.recentAppointments.length > 0) {
        updateRecentAppointments(data.recentAppointments);
    }
}

function updateRecentAppointments(appointments) {
    const container = document.querySelector('.dashboard-grid-2col .card:first-child');
    
    // Clear existing appointments
    const existingItems = container.querySelectorAll('.appointment-item');
    existingItems.forEach(item => item.remove());

    // Add new appointments
    appointments.forEach(apt => {
        const appointmentHTML = `
            <div class="appointment-item">
                <div class="appointment-doc">
                    <div class="doc-avatar"></div>
                    <div class="doc-info">
                        <h4>${apt.doctorName || 'Doctor'}</h4>
                        <p>${apt.specialization || 'Specialist'} • ${apt.clinicName || 'Clinic'}</p>
                    </div>
                </div>
                <div class="appointment-time">
                    <p class="appointment-date">${formatDate(apt.appointmentDate)}</p>
                    <p class="appointment-slot">${formatTime(apt.appointmentTime)}</p>
                    <span class="badge badge-upcoming">${apt.status || 'Confirmed'}</span>
                </div>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', appointmentHTML);
    });
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatTime(timeStr) {
    if (!timeStr) return '--:--';
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

function logout() {
    localStorage.clear();
    window.location.href='login.html';
}
