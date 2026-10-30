document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const patientId = urlParams.get('patientId');

    if (!patientId) {
        alert("No patient ID provided.");
        window.history.back();
        return;
    }

    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.removeItem('token');
        window.location.href = 'login.html';
    });

    window.patientId = patientId;
    window.rowCounter = 0;

    initForm(token);
    addMedicationRow(); // First default row
});

function goBack() {
    window.history.back();
}

async function initForm(token) {
    try {
        // Set basic patient info manually or via API
        document.getElementById('patientNameHeader').textContent = `Patient ID: PT-${window.patientId}`;

        // Get user profile (doctor)
        const dRes = await fetch('/api/doctors/me', { headers: { 'Authorization': `Bearer ${token}` } });
        if (dRes.ok) {
            const doc = await dRes.json();
            window.doctorId = doc.id;
        }

        // Fetch appointments for this patient to link
        const aRes = await fetch('/api/appointments/doctor/me', { headers: { 'Authorization': `Bearer ${token}` } });
        if (aRes.ok) {
            let apps = await aRes.json();
            apps = apps.content || apps;
            const ptApps = apps.filter(a => String(a.patientId) === String(window.patientId) && a.status !== 'CANCELLED');

            const sel = document.getElementById('rxAppointmentId');
            ptApps.forEach(a => {
                const opt = document.createElement('option');
                opt.value = a.id;
                opt.textContent = `${a.appointmentDate.split('T')[0]} - ${a.reason || 'General'}`;
                sel.appendChild(opt);

                // Also set patient name from the first match
                document.getElementById('patientNameHeader').textContent =
                    `Issuing for ${a.patientName || a.patient?.name} (PT-${window.patientId})`;
            });
        }
    } catch (e) {
        console.error("Init form error", e);
    }
}

function addMedicationRow() {
    window.rowCounter++;
    const tbody = document.getElementById('medsBody');
    const tr = document.createElement('tr');
    tr.id = `medRow-${window.rowCounter}`;

    tr.innerHTML = `
        <td><input type="text" class="form-control med-name" placeholder="e.g., Paracetamol" required></td>
        <td><input type="text" class="form-control med-dosage" placeholder="500mg" required></td>
        <td><input type="text" class="form-control med-freq" placeholder="1-0-1" required></td>
        <td><input type="text" class="form-control med-dur" placeholder="5 days"></td>
        <td><input type="text" class="form-control med-inst" placeholder="After food"></td>
        <td style="text-align:center;">
            <button type="button" class="btn btn-danger-icon" onclick="removeMedicationRow(${window.rowCounter})">
                <i class="fas fa-trash"></i>
            </button>
        </td>
    `;
    tbody.appendChild(tr);
}

function removeMedicationRow(id) {
    const tbody = document.getElementById('medsBody');
    if (tbody.children.length === 1) {
        alert("You must include at least one medication, or clear the fields.");
        return;
    }
    const tr = document.getElementById(`medRow-${id}`);
    tr.remove();
}

async function submitPrescription() {
    const token = localStorage.getItem('token');
    const btn = document.getElementById('generateBtn');

    // Gather Meds
    const meds = [];
    const rows = document.getElementById('medsBody').children;
    for (let r of rows) {
        const name = r.querySelector('.med-name').value;
        const dose = r.querySelector('.med-dosage').value;
        const freq = r.querySelector('.med-freq').value;
        if (name && dose && freq) {
            meds.push({
                medicineName: name,
                dosage: dose,
                frequency: freq,
                duration: r.querySelector('.med-dur').value,
                instructions: r.querySelector('.med-inst').value
            });
        }
    }

    if (meds.length === 0) {
        alert("Please provide valid medication details.");
        return;
    }

    const payload = {
        patientId: window.patientId,
        doctorId: window.doctorId || 1, // Fallback logic
        appointmentId: document.getElementById('rxAppointmentId').value || null,
        diagnosis: document.getElementById('rxDiagnosis').value,
        generalInstructions: document.getElementById('rxGeneral').value,
        followUpNotes: document.getElementById('rxFollowUp').value,
        medications: meds
    };

    btn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> Generating...`;
    btn.disabled = true;

    try {
        const res = await fetch('/api/prescriptions', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error("Failed to generate prescription");

        const prescSaved = await res.json();

        // Setup success modal with dynamic PDF link
        const pdfBtn = document.getElementById('downloadPdfBtn');
        pdfBtn.onclick = () => {
            window.open(prescSaved.pdfUrl, '_blank');
        };

        // Auto download strategy (optional, prompt requested auth auto-download)
        window.open(prescSaved.pdfUrl, '_blank');

        document.getElementById('successModal').style.display = 'flex';

    } catch (e) {
        alert(e.message);
        btn.innerHTML = `<i class="fas fa-magic"></i> Generate PDF & Save`;
        btn.disabled = false;
    }
}
