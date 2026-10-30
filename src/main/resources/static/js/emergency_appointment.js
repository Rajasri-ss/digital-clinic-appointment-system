const getToken = () => localStorage.getItem('token');
const authHeader = () => ({ 'Authorization': `Bearer ${getToken()}`, 'Content-Type': 'application/json' });

let selectedClinic = null;
let selectedDoctor = null;

document.addEventListener('DOMContentLoaded', () => {
    if (!getToken()) {
        window.location.href = 'login.html';
        return;
    }
    loadNearbyClinics();
});

function nextStep(step) {
    document.querySelectorAll('.triage-step').forEach(s => s.classList.remove('active'));
    document.getElementById(`step-${step}`).classList.add('active');

    if (step === 3) {
        populateSummary();
    }
}

function prevStep(step) {
    document.querySelectorAll('.triage-step').forEach(s => s.classList.remove('active'));
    document.getElementById(`step-${step}`).classList.add('active');
}

async function loadNearbyClinics() {
    // We use the new nearby endpoint
    try {
        const response = await fetch('/api/doctors/nearby?lat=0&lng=0&radius=10', { headers: authHeader() });
        if (response.ok) {
            const doctors = await response.json();
            renderClinics(doctors);
        }
    } catch (err) {
        console.error("Failed to load nearby clinics", err);
    }
}

function renderClinics(doctors) {
    const list = document.getElementById('clinicList');
    list.innerHTML = doctors.map(doc => `
        <div class="clinic-card" onclick="selectClinic(this, ${doc.doctorId}, '${doc.fullName}', '${doc.clinicName}')">
            <div class="clinic-name">${doc.clinicName || 'City Clinic'}</div>
            <div class="doctor-name">Dr. ${doc.fullName}</div>
            <div class="doctor-spec">${doc.specialization}</div>
            <div class="distance"><i class="fas fa-map-marker-alt"></i> 1.${Math.floor(Math.random()*9)} km away</div>
        </div>
    `).join('');
}

function selectClinic(el, docId, docName, clinicName) {
    document.querySelectorAll('.clinic-card').forEach(c => c.classList.remove('selected'));
    el.classList.add('selected');
    selectedDoctor = { id: docId, name: docName };
    selectedClinic = { name: clinicName };
}

function populateSummary() {
    document.getElementById('summary-complaint').textContent = document.getElementById('chiefComplaint').value || 'Not specified';
    document.getElementById('summary-priority').textContent = document.querySelector('input[name="urgency"]:checked').value;
    document.getElementById('summary-location').textContent = selectedClinic ? selectedClinic.name : 'Not selected';
    document.getElementById('summary-doctor').textContent = selectedDoctor ? `Dr. ${selectedDoctor.name}` : 'Not selected';
}

async function useCurrentLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(pos => {
            // In a real app we'd fetch with these coords
            loadNearbyClinics();
        });
    }
}

async function bookEmergency() {
    if (!selectedDoctor) {
        alert("Please select a clinic/doctor");
        return;
    }

    const payload = {
        doctorId: selectedDoctor.id,
        locationId: 1, // Default mock location for now or we could store locationId in card
        appointmentDate: new Date().toISOString().split('T')[0],
        appointmentTime: new Date().toTimeString().split(' ')[0].substring(0, 5),
        reasonForVisit: "Emergency: " + document.getElementById('chiefComplaint').value,
        isEmergency: true,
        urgencyLevel: document.querySelector('input[name="urgency"]:checked').value,
        chiefComplaint: document.getElementById('chiefComplaint').value,
        symptoms: document.getElementById('extraSymptoms').value,
        consultationType: "IN_PERSON"
    };

    const btn = document.getElementById('bookBtn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';

    try {
        const response = await fetch('/api/appointments/emergency', {
            method: 'POST',
            headers: authHeader(),
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const data = await response.json();
            showSuccess(data.bookingReference);
        } else {
            const err = await response.json();
            alert(err.message || "Failed to book emergency appointment");
        }
    } catch (err) {
        alert("Network error");
    } finally {
        btn.disabled = false;
        btn.innerHTML = 'Book Now <i class="fas fa-ambulance"></i>';
    }
}

function showSuccess(ref) {
    document.getElementById('bookingRef').textContent = `REF: #${ref}`;
    document.getElementById('successModal').style.display = 'flex';
}
