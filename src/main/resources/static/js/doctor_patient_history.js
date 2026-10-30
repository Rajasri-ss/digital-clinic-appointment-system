document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const patientId = urlParams.get('id');

    if (!patientId) {
        alert("No patient ID provided.");
        window.location.href = 'doctor_patients.html';
        return;
    }

    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.removeItem('token');
        window.location.href = 'login.html';
    });

    // Store globally for submission
    window.currentPatientId = patientId;

    fetchPatientData(patientId, token);
});

async function fetchPatientData(patientId, token) {
    document.getElementById('loader').classList.remove('hidden');

    try {
        // Fetch patient details (could be part of appointments summary or separate api)
        // Since we don't have a specific `GET /api/patients/{id}` verified, mock name or fetch via appointments
        const nameFallback = `Patient PT-${patientId}`;
        document.getElementById('headerPatientName').textContent = `${nameFallback} History`;

        // 1. Fetch Past Vitals
        const vRes = await fetch(`/api/vitals/patient/${patientId}`, { headers: { 'Authorization': `Bearer ${token}` } });
        if (vRes.ok) {
            const vitals = await vRes.json();
            renderPastVitals(vitals);
        }

        // 2. Fetch Past Prescriptions
        const pRes = await fetch(`/api/prescriptions/patient/${patientId}`, { headers: { 'Authorization': `Bearer ${token}` } });
        if (pRes.ok) {
            const prescs = await pRes.json();
            renderPastPrescriptions(prescs);
        }

        // 3. Appointments Timeline (Filtering doctor's appointments for this patient)
        const aRes = await fetch('/api/appointments/doctor/me', { headers: { 'Authorization': `Bearer ${token}` } });
        if (aRes.ok) {
            let userApps = await aRes.json();
            userApps = userApps.content || userApps;
            const ptApps = userApps.filter(a => String(a.patientId) === String(patientId));

            // Set real name from appointment
            if (ptApps.length > 0) {
                document.getElementById('headerPatientName').textContent = `${ptApps[0].patientName || ptApps[0].patient?.name} History`;
            }
            renderTimeline(ptApps);
        }

        document.getElementById('contentGrid').classList.remove('hidden');
    } catch (e) {
        console.error(e);
        alert(e.message);
    } finally {
        document.getElementById('loader').classList.add('hidden');
    }
}

// Auto BMI Calculation
function calculateBMI() {
    const h = parseFloat(document.getElementById('vitalHeight').value); // cm
    const w = parseFloat(document.getElementById('vitalWeight').value); // kg

    if (h > 0 && w > 0) {
        const hm = h / 100;
        const bmi = w / (hm * hm);
        document.getElementById('vitalBMI').value = bmi.toFixed(1);
    } else {
        document.getElementById('vitalBMI').value = '';
    }
}

async function submitVitals() {
    const token = localStorage.getItem('token');
    const ptId = window.currentPatientId;
    const btn = document.getElementById('btnSaveVitals');

    const payload = {
        patientId: ptId,
        height: parseFloat(document.getElementById('vitalHeight').value),
        weight: parseFloat(document.getElementById('vitalWeight').value),
        bloodPressure: document.getElementById('vitalBP').value,
        heartRate: parseInt(document.getElementById('vitalHR').value),
        temperature: parseFloat(document.getElementById('vitalTemp').value),
        oxygenSaturation: parseInt(document.getElementById('vitalSpo2').value),
        respiratoryRate: parseInt(document.getElementById('vitalRR').value),
        notes: document.getElementById('vitalNotes').value
    };

    btn.textContent = 'Saving...'; btn.disabled = true;

    try {
        const res = await fetch('/api/vitals', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error("Failed to save vitals");

        document.getElementById('vitalsForm').reset();
        calculateBMI(); // Reset BMI
        fetchPatientData(ptId, token); // Refresh

    } catch (e) {
        alert(e.message);
    } finally {
        btn.textContent = 'Save Vitals'; btn.disabled = false;
    }
}

function renderPastVitals(vitals) {
    const table = document.getElementById('pastVitalsTable');
    if (!vitals || vitals.length === 0) {
        table.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No past vitals recorded.</td></tr>`;
        return;
    }

    let html = '';
    vitals.forEach(v => {
        const d = new Date(v.recordDate).toLocaleDateString();
        let bmi = '--';
        if (v.height && v.weight) {
            const h = v.height / 100; bmi = (v.weight / (h * h)).toFixed(1);
        }

        html += `
            <tr>
                <td>${d}</td>
                <td>${v.bloodPressure || '--'}</td>
                <td>${v.heartRate || '--'}</td>
                <td>${v.temperature || '--'}</td>
                <td>${v.weight || '--'}</td>
                <td>${bmi}</td>
            </tr>
        `;
    });
    table.innerHTML = html;
}

function renderTimeline(appointments) {
    const tl = document.getElementById('appointmentTimeline');
    if (!appointments || appointments.length === 0) {
        tl.innerHTML = `<p class="text-muted">No appointments found.</p>`;
        return;
    }

    // Sort desc 
    appointments.sort((a, b) => new Date(b.appointmentDate) - new Date(a.appointmentDate));

    let html = '';
    appointments.forEach(a => {
        const dStr = new Date(a.appointmentDate).toISOString().split('T')[0];
        const statusColor = a.status === 'COMPLETED' ? 'var(--primary)' : 'var(--warning)';

        html += `
            <div class="timeline-item">
                <div class="timeline-date">${dStr} @ ${a.appointmentTime || '--:--'}</div>
                <div class="timeline-content" style="border-left: 3px solid ${statusColor}">
                    <strong>${a.reason || 'General Checkup'}</strong>
                    <span style="font-size:0.85rem; color:var(--text-muted)">Status: ${a.status}</span>
                </div>
            </div>
        `;
    });
    tl.innerHTML = html;
}

function renderPastPrescriptions(prescs) {
    const list = document.getElementById('pastPrescriptionsList');
    if (!prescs || prescs.length === 0) {
        list.innerHTML = `<p class="text-muted">No prescriptions issued.</p>`;
        return;
    }

    let html = '';
    prescs.forEach(p => {
        const d = new Date(p.prescriptionDate).toLocaleDateString();
        html += `
            <div class="rx-item">
                <div class="rx-info">
                    <h4>${p.diagnosis || 'General Prescription'}</h4>
                    <p>${d} • ${p.medications ? p.medications.length : 0} meds prescribed</p>
                </div>
                <div class="rx-action">
                    <button class="btn" onclick="window.open('${p.pdfUrl}', '_blank')" title="Download PDF">
                        <i class="fas fa-file-pdf"></i>
                    </button>
                </div>
            </div>
        `;
    });
    list.innerHTML = html;
}

function openPrescribe() {
    window.location.href = `prescription_view.html?patientId=${window.currentPatientId}`;
}

function viewLabReports() {
    alert("Navigating to Medical Records for Patient PT-" + window.currentPatientId);
    // Usually points to a shared view component, out of scope for mockup detail
}
