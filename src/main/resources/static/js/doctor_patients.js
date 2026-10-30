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

    fetchPatients(token);
});

let allPatients = [];

async function fetchPatients(token) {
    document.getElementById('loader').classList.remove('hidden');

    try {
        // Option A: Specific endpoints that get all unique patients for a doctor
        // Option B: Derive from appointments. Assuming B for maximum robustness based on existing backend logic implicitly created previously.
        const response = await fetch('/api/appointments/doctor/me', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("Could not load patients list");

        const data = await response.json();
        const appointments = data.content || data;

        // Extract unique patients mapped to latest appointment logic
        const patientMap = new Map();
        appointments.forEach(app => {
            if (app.patientId && app.patientName) {
                if (!patientMap.has(app.patientId)) {
                    patientMap.set(app.patientId, {
                        id: app.patientId,
                        name: app.patientName,
                        phone: app.patient?.phone || '--',
                        email: app.patient?.user?.email || '--',
                        gender: app.patient?.gender || '--',
                        dob: app.patient?.dateOfBirth || '',
                        lastVisit: app.appointmentDate?.split('T')[0]
                    });
                } else {
                    // Update lastvisit if this app is newer
                    const ext = patientMap.get(app.patientId);
                    if (app.appointmentDate?.split('T')[0] > ext.lastVisit) {
                        ext.lastVisit = app.appointmentDate?.split('T')[0];
                        patientMap.set(app.patientId, ext);
                    }
                }
            }
        });

        allPatients = Array.from(patientMap.values());
        renderPatients(allPatients);

    } catch (e) {
        console.error(e);
        document.getElementById('empty-state').classList.remove('hidden');
        document.getElementById('empty-state').innerHTML = `<p class="text-danger">Failed to load patients. ${e.message}</p>`;
    } finally {
        document.getElementById('loader').classList.add('hidden');
    }
}

function renderPatients(patients) {
    const grid = document.getElementById('patientsList');
    const empty = document.getElementById('empty-state');

    if (!patients || patients.length === 0) {
        grid.innerHTML = '';
        empty.classList.remove('hidden');
        return;
    }

    empty.classList.add('hidden');
    let html = '';

    patients.forEach(p => {
        const initial = p.name ? p.name.charAt(0) : '?';
        const ageStr = calcAge(p.dob);

        html += `
            <div class="patient-card">
                <div class="pc-header">
                    <div class="pc-avatar">${initial}</div>
                    <div class="pc-info">
                        <h3>${p.name}</h3>
                        <p>ID: PT-${p.id}</p>
                    </div>
                </div>
                
                <div class="pc-details">
                    <div class="detail-item"><i class="fas fa-phone"></i> <span>${p.phone}</span></div>
                    <div class="detail-item"><i class="fas fa-venus-mars"></i> <span>${p.gender}</span></div>
                    <div class="detail-item"><i class="fas fa-calendar-alt"></i> <span>${ageStr}</span></div>
                    <div class="detail-item"><i class="fas fa-history"></i> <span>${p.lastVisit || '--'}</span></div>
                </div>
                
                <div class="pc-footer">
                    <!-- Navigates to history dashboard passing patient ID -->
                    <button class="btn btn-primary" onclick="window.location.href='doctor_patient_history.html?id=${p.id}'">
                        <i class="fas fa-folder-open"></i> Medical History
                    </button>
                    <!-- Quick Prescribe -->
                    <button class="btn btn-outline-primary" onclick="window.location.href='prescription_view.html?patientId=${p.id}'">
                        <i class="fas fa-prescription-bottle-alt"></i> Rx
                    </button>
                </div>
            </div>
        `;
    });

    grid.innerHTML = html;
}

function calcAge(dobStr) {
    if (!dobStr) return '-- yrs';
    const dob = new Date(dobStr);
    const diff = Date.now() - dob.getTime();
    const ageDate = new Date(diff);
    return Math.abs(ageDate.getUTCFullYear() - 1970) + ' yrs';
}

function filterPatients() {
    const term = document.getElementById('searchInput').value.toLowerCase();

    const filtered = allPatients.filter(p => {
        const idStr = `pt-${p.id}`.toLowerCase();
        return p.name.toLowerCase().includes(term) ||
            p.phone.includes(term) ||
            idStr.includes(term);
    });

    renderPatients(filtered);
}
