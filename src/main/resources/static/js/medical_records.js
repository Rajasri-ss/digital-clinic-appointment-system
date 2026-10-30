let vitalsChartInstance = null;

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

    fetchAllData(token);
});

function switchTab(tabId, btn) {
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
    document.getElementById(`tab-${tabId}`).classList.add('active');
    btn.classList.add('active');

    if (tabId === 'vitals' && vitalsChartInstance) {
        vitalsChartInstance.resize(); // Fix chart rendering on unhide
    }
}

async function fetchAllData(token) {
    // Decoded token would ideal to get patientId, but API accepts /me or by getting profile
    // Assuming backend returns records by authenticated user directly or we fetch patient profile first
    document.getElementById('loader').style.display = 'block';

    try {
        // 1. Get Profile to find Patient ID
        const profileRes = await fetch('/api/patients/me', { headers: { 'Authorization': `Bearer ${token}` } });
        if (!profileRes.ok) throw new Error("Could not fetch profile");
        const profile = await profileRes.json();
        const patientId = profile.id || profile.patientId;

        // 2. Fetch parallel requests
        const [prescRes, vitalsRes, recordsRes] = await Promise.all([
            fetch(`/api/prescriptions/patient/${patientId}`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch('/api/vitals/me', { headers: { 'Authorization': `Bearer ${token}` } }), // Assuming existing or mock endpoint
            fetch(`/api/records/patient/${patientId}`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        const prescriptions = prescRes.ok ? await prescRes.json() : [];
        const vitals = vitalsRes.ok ? await vitalsRes.json() : [];
        const records = recordsRes.ok ? await recordsRes.json() : [];

        renderPrescriptions(prescriptions);
        renderVitals(vitals);
        renderDocuments(records);

    } catch (e) {
        console.error(e);
        alert('Failed to load medical records');
    } finally {
        document.getElementById('loader').style.display = 'none';
    }
}

function renderPrescriptions(prescriptions) {
    const list = document.getElementById('prescriptionsList');
    const empty = document.getElementById('empty-prescriptions');

    if (!prescriptions || prescriptions.length === 0) {
        empty.classList.remove('hidden');
        list.innerHTML = '';
        return;
    }

    let html = '';
    prescriptions.forEach(p => {
        const d = new Date(p.prescriptionDate).toLocaleDateString();
        html += `
            <div class="record-card">
                <div class="rc-header">
                    <div class="rc-icon blue"><i class="fas fa-file-prescription"></i></div>
                    <div class="rc-date">${d}</div>
                </div>
                <div class="rc-title">${p.diagnosis || 'General Checkup'}</div>
                <div class="rc-doctor">Dr. ${p.doctorName || 'Unknown'}</div>
                <div class="rc-desc">${p.medications ? p.medications.length : 0} medications prescribed</div>
                <div class="rc-footer">
                    <button class="btn btn-outline-primary" style="width:100%" onclick="downloadPdf('${p.pdfUrl}')">
                        <i class="fas fa-download"></i> Download PDF
                    </button>
                </div>
            </div>
        `;
    });
    list.innerHTML = html;
}

function renderDocuments(records) {
    const labList = document.getElementById('labList');
    const docList = document.getElementById('docsList');
    const emptyLab = document.getElementById('empty-lab');
    const emptyDocs = document.getElementById('empty-docs');

    let labs = '';
    let docs = '';
    let labCount = 0;
    let docCount = 0;

    records.forEach(r => {
        const d = new Date(r.uploadDate).toLocaleDateString();
        const iconClasses = r.recordType === 'LAB_REPORT' ? 'yellow fa-flask' : 'green fa-file-alt';
        const cardHtml = `
            <div class="record-card">
                <div class="rc-header">
                    <div class="rc-icon ${iconClasses.split(' ')[0]}"><i class="fas ${iconClasses.split(' ')[1]}"></i></div>
                    <div class="rc-date">${d}</div>
                </div>
                <div class="rc-title">${r.recordName}</div>
                <div class="rc-desc">${r.description || 'No description provided'}</div>
                <div class="rc-footer">
                    <button class="btn btn-outline-primary" style="width:100%" onclick="downloadPdf('${r.fileUrl}')">
                        <i class="fas fa-download"></i> Download File
                    </button>
                </div>
            </div>
        `;

        if (r.recordType === 'LAB_REPORT') { labs += cardHtml; labCount++; }
        else { docs += cardHtml; docCount++; }
    });

    labList.innerHTML = labs;
    docList.innerHTML = docs;

    if (labCount === 0) emptyLab.classList.remove('hidden');
    if (docCount === 0) emptyDocs.classList.remove('hidden');
}

function renderVitals(vitals) {
    const table = document.getElementById('vitalsTableBody');
    const empty = document.getElementById('empty-vitals');

    if (!vitals || vitals.length === 0) {
        empty.classList.remove('hidden');
        document.querySelector('.chart-container').style.display = 'none';
        document.querySelector('.table-responsive').style.display = 'none';
        return;
    }

    let tHtml = '';
    const labels = [];
    const hrData = [];
    const bpSystolic = [];

    // Sort chronological for chart
    const sorted = [...vitals].sort((a, b) => new Date(a.recordDate) - new Date(b.recordDate));

    sorted.forEach(v => {
        const dStr = new Date(v.recordDate).toLocaleDateString();
        labels.push(dStr);
        hrData.push(v.heartRate || 0);

        if (v.bloodPressure) {
            const parts = v.bloodPressure.split('/');
            bpSystolic.push(parts[0] ? parseInt(parts[0]) : 0);
        } else {
            bpSystolic.push(0);
        }
    });

    // Populate Table (Reverse for latest first)
    vitals.forEach(v => {
        const dStr = new Date(v.recordDate).toLocaleDateString();
        tHtml += `<tr>
            <td><strong>${dStr}</strong></td>
            <td>${v.bloodPressure || '--'}</td>
            <td>${v.heartRate || '--'}</td>
            <td>${v.temperature || '--'}</td>
            <td>${v.weight || '--'}</td>
            <td>${v.oxygenSaturation || '--'}</td>
        </tr>`;
    });
    table.innerHTML = tHtml;

    initChart(labels, hrData, bpSystolic);
}

function initChart(labels, hrData, bpData) {
    const ctx = document.getElementById('vitalsChart').getContext('2d');
    if (vitalsChartInstance) vitalsChartInstance.destroy();

    vitalsChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Heart Rate (bpm)',
                    data: hrData,
                    borderColor: '#4F46E5',
                    backgroundColor: 'rgba(79, 70, 229, 0.1)',
                    tension: 0.4,
                    fill: true
                },
                {
                    label: 'Systolic BP (mmHg)',
                    data: bpData,
                    borderColor: '#EF4444',
                    backgroundColor: 'transparent',
                    borderDash: [5, 5],
                    tension: 0.4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'top' } },
            scales: { y: { beginAtZero: false, suggestedMin: 50 } }
        }
    });
}

function downloadPdf(url) {
    if (!url) { alert("File not available"); return; }
    // We add token to URL as query param or fetch as blob. 
    // Since direct link `<img src="">` worked in prompt before, we assume direct link works for download.
    // However, secure way is fetching blob, but window.open is standard for 'downloading'.
    window.open(url, '_blank');
}

// Modal Upload Logic
const modal = document.getElementById('uploadModal');
function openUploadModal() { modal.style.display = 'flex'; }
function closeUploadModal() { modal.style.display = 'none'; document.getElementById('uploadForm').reset(); }

async function uploadDocument() {
    const token = localStorage.getItem('token');
    const name = document.getElementById('docName').value;
    const type = document.getElementById('docType').value;
    const desc = document.getElementById('docDesc').value;
    const fileInput = document.getElementById('docFile');

    if (!name || !fileInput.files.length) {
        alert("Name and File are required"); return;
    }

    const file = fileInput.files[0];

    // Get patient ID
    const profileRes = await fetch('/api/patients/me', { headers: { 'Authorization': `Bearer ${token}` } });
    const profile = await profileRes.json();
    const patientId = profile.id || profile.patientId;

    const formData = new FormData();
    formData.append('patientId', patientId);
    formData.append('file', file);
    formData.append('recordType', type);
    formData.append('recordName', name);
    formData.append('description', desc);

    const btn = document.getElementById('submitUploadBtn');
    btn.textContent = 'Uploading...'; btn.disabled = true;

    try {
        const response = await fetch('/api/records/upload', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
        });

        if (!response.ok) throw new Error("Upload failed");

        closeUploadModal();
        fetchAllData(token); // Refresh
    } catch (e) {
        alert(e.message);
    } finally {
        btn.textContent = 'Upload'; btn.disabled = false;
    }
}
