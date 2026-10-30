const API_BASE = 'http://localhost:8080';
const getToken = () => localStorage.getItem('token');
const authHeader = () => ({
    'Authorization': 'Bearer ' + getToken(),
    'Content-Type': 'application/json'
});

let allDoctors = [];
let userLat = null;
let userLng = null;

// ===== CHECK AUTH =====
function checkAuth() {
    if (!getToken()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// ===== LOAD DOCTORS =====
async function loadDoctors() {
    const grid = document.getElementById('doctorsGrid');
    const countEl = document.getElementById('doctorCount');
    
    if (!grid) {
        console.error('doctorsGrid element not found!');
        return;
    }
    
    grid.innerHTML = `
        <div style="grid-column:1/-1; text-align:center; padding:3rem;">
            <i class="fas fa-spinner fa-spin" style="font-size:2rem; color:#4F46E5;"></i>
            <p style="margin-top:1rem; color:#6B7280;">Loading doctors...</p>
        </div>
    `;
    
    const endpoints = [
        '/api/doctors/search',
        '/api/doctors/search?page=0&size=50',
        '/api/doctors'
    ];
    
    let doctors = null;
    
    for (const endpoint of endpoints) {
        try {
            console.log('Trying endpoint:', endpoint);
            const response = await fetch(`${API_BASE}${endpoint}`, {
                headers: authHeader()
            });
            
            if (!response.ok) {
                console.log(endpoint, 'status:', response.status);
                continue;
            }
            
            const data = await response.json();
            console.log(endpoint, 'response:', data);
            
            if (Array.isArray(data)) {
                doctors = data;
            } else if (data.content) {
                doctors = data.content;
            } else if (data.doctors) {
                doctors = data.doctors;
            } else if (data.data) {
                doctors = data.data;
            }
            
            if (doctors && doctors.length > 0) {
                console.log('Successfully loaded', doctors.length, 'doctors from', endpoint);
                break;
            }
        } catch (err) {
            console.log(endpoint, 'failed:', err.message);
        }
    }
    
    if (!doctors || doctors.length === 0) {
        grid.innerHTML = `
            <div style="grid-column:1/-1; text-align:center; padding:3rem;">
                <i class="fas fa-user-md" style="font-size:3rem; color:#D1D5DB;"></i>
                <p style="margin-top:1rem; color:#6B7280; font-size:1.1rem; font-weight:600;">No doctors found</p>
                <p style="color:#9CA3AF; font-size:0.875rem; margin-top:0.5rem;">Check console for API errors</p>
                <button onclick="loadDoctors()" style="margin-top:1rem; padding:0.5rem 1.5rem; background:#4F46E5; color:white; border:none; border-radius:8px; cursor:pointer; font-weight:600;">
                    <i class="fas fa-redo"></i> Retry
                </button>
            </div>
        `;
        return;
    }
    
    allDoctors = doctors;
    renderDoctors(allDoctors);
    if (countEl) countEl.textContent = `${allDoctors.length} doctor${allDoctors.length !== 1 ? 's' : ''} found`;
}

// ===== RENDER DOCTORS =====
function renderDoctors(doctors) {
    const grid = document.getElementById('doctorsGrid');
    const countEl = document.getElementById('doctorCount');
    
    if (!grid) return;
    
    if (!doctors || doctors.length === 0) {
        grid.innerHTML = `
            <div style="grid-column:1/-1; text-align:center; padding:3rem;">
                <i class="fas fa-user-md" style="font-size:3rem; color:#D1D5DB;"></i>
                <p style="margin-top:1rem; color:#6B7280; font-size:1.1rem;">No doctors match your search</p>
            </div>
        `;
        if (countEl) countEl.textContent = '0 doctors found';
        return;
    }
    
    if (countEl) countEl.textContent = `${doctors.length} doctor${doctors.length !== 1 ? 's' : ''} found`;
    
    grid.innerHTML = doctors.map(doctor => {
        const id = doctor.id || doctor.doctorId || doctor.doctor_id;
        const name = doctor.fullName ||
            `${doctor.firstName || ''} ${doctor.lastName || ''}`.trim() ||
            'Unknown Doctor';
        const initials = name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) || 'DR';
        const specialization = doctor.specialization || doctor.specialty || 'General Medicine';
        const experience = doctor.experienceYears || doctor.experience_years || 0;
        const fee = doctor.consultationFee || doctor.consultation_fee || 0;
        const rating = parseFloat(doctor.averageRating || doctor.average_rating || 0).toFixed(1);
        const reviews = doctor.totalReviews || doctor.total_reviews || 0;
        const isAvailable = doctor.isAvailable || doctor.available || doctor.is_available;
        const hospitalName = doctor.hospitalName || doctor.hospital_name || '';
        
        return `
            <div class="doctor-card">
                <div class="doctor-card-header">
                    <div class="doctor-avatar-wrap">
                        <div class="doctor-avatar">
                            ${initials}
                        </div>
                    </div>
                    <span class="availability-badge ${isAvailable ? 'available' : 'unavailable'}">
                        <i class="fas fa-circle"></i>
                        ${isAvailable ? 'Available' : 'Unavailable'}
                    </span>
                </div>
                
                <div class="doctor-card-body">
                    <h3 class="doctor-name">Dr. ${name}</h3>
                    <p class="doctor-specialization">
                        <i class="fas fa-stethoscope"></i> ${specialization}
                    </p>
                    ${doctor.qualifications ? `
                    <p class="doctor-qualification">
                        <i class="fas fa-graduation-cap"></i> ${doctor.qualifications}
                    </p>` : ''}
                    ${hospitalName ? `
                    <p class="doctor-hospital">
                        <i class="fas fa-hospital"></i> ${hospitalName}
                    </p>` : ''}
                    
                    <div class="doctor-rating-row">
                        ${generateStars(rating)}
                        <span class="rating-text">${rating} (${reviews} reviews)</span>
                    </div>
                    
                    <div class="doctor-stats-row">
                        <div class="stat-pill">
                            <i class="fas fa-briefcase"></i> ${experience} yrs
                        </div>
                        <div class="stat-pill fee">
                            <i class="fas fa-rupee-sign"></i> ₹${fee}
                        </div>
                    </div>
                </div>
                
                <div class="doctor-card-footer">
                    <button class="btn-view-profile" onclick="viewDoctorProfile(${id})">
                        <i class="fas fa-user"></i> Profile
                    </button>
                    <button class="btn-book ${!isAvailable ? 'btn-book-disabled' : ''}"
                        onclick="${isAvailable
                            ? `bookAppointment(${id}, '${name.replace(/'/g, "\\'")}')`
                            : `showToast('Doctor is not available', 'error')`
                        }"
                        ${!isAvailable ? 'disabled' : ''}>
                        <i class="fas fa-calendar-plus"></i>
                        ${isAvailable ? 'Book Now' : 'Unavailable'}
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

// ===== GENERATE STARS =====
function generateStars(rating) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= Math.floor(rating)) {
            stars += '<i class="fas fa-star" style="color:#F59E0B; font-size:0.8rem;"></i>';
        } else if (i - 0.5 <= rating) {
            stars += '<i class="fas fa-star-half-alt" style="color:#F59E0B; font-size:0.8rem;"></i>';
        } else {
            stars += '<i class="far fa-star" style="color:#D1D5DB; font-size:0.8rem;"></i>';
        }
    }
    return stars;
}

// ===== USE MY LOCATION =====
function getLocation() {
    const btn = document.getElementById('locationBtn');
    const status = document.getElementById('locationStatus');
    
    if (!navigator.geolocation) {
        showToast('Geolocation is not supported by your browser', 'error');
        return;
    }
    
    if (btn) {
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Getting location...';
        btn.disabled = true;
    }
    
    navigator.geolocation.getCurrentPosition(
        async (position) => {
            userLat = position.coords.latitude;
            userLng = position.coords.longitude;
            
            console.log('Location detected:', userLat, userLng);
            
            if (btn) {
                btn.innerHTML = '<i class="fas fa-check-circle"></i> Location Found';
                btn.style.background = '#10B981';
                btn.disabled = false;
            }
            
            if (status) {
                status.textContent = `📍 Your location: ${userLat.toFixed(4)}, ${userLng.toFixed(4)}`;
                status.style.display = 'block';
            }
            
            showToast('Location detected! Showing nearby doctors.', 'success');
            
            // Try to load doctors near location
            await loadDoctorsByLocation(userLat, userLng);
        },
        (error) => {
            console.error('Geolocation error:', error);
            if (btn) {
                btn.innerHTML = '<i class="fas fa-map-marker-alt"></i> Use My Location';
                btn.disabled = false;
                btn.style.background = '';
            }
            
            let message = 'Could not get location.';
            if (error.code === 1) message = 'Location access denied. Please allow location in browser settings.';
            if (error.code === 2) message = 'Location unavailable. Try again.';
            if (error.code === 3) message = 'Location request timed out. Try again.';
            
            showToast(message, 'error');
        },
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
        }
    );
}

// ===== LOAD DOCTORS BY LOCATION =====
async function loadDoctorsByLocation(lat, lng) {
    try {
        const response = await fetch(
            `${API_BASE}/api/doctors/nearby?lat=${lat}&lng=${lng}&radius=20`,
            { headers: authHeader() }
        );
        
        if (response.ok) {
            const doctors = await response.json();
            if (doctors && doctors.length > 0) {
                allDoctors = doctors;
                renderDoctors(doctors);
                showToast(`Found ${doctors.length} doctors near you!`, 'success');
                return;
            }
        }
    } catch (error) {
        console.log('Location based search not available, showing all doctors');
    }
    
    // Fallback - show all doctors if location search fails
    loadDoctors();
}

// ===== SEARCH AND FILTER =====
function searchDoctors() {
    const searchTerm = (document.getElementById('searchInput')?.value || '').toLowerCase();
    const specialization = (document.getElementById('specializationFilter')?.value || '').toLowerCase();
    const availability = document.getElementById('availabilityFilter')?.value || '';
    const sortBy = document.getElementById('sortFilter')?.value || '';
    
    let filtered = allDoctors.filter(doctor => {
        const name = (doctor.fullName || `${doctor.firstName || ''} ${doctor.lastName || ''}`).toLowerCase();
        const spec = (doctor.specialization || doctor.specialty || '').toLowerCase();
        const isAvailable = doctor.isAvailable || doctor.available || doctor.is_available;
        
        const matchSearch = !searchTerm || name.includes(searchTerm) || spec.includes(searchTerm);
        const matchSpec = !specialization || spec.includes(specialization);
        const matchAvail = !availability ||
            (availability === 'available' && isAvailable) ||
            (availability === 'unavailable' && !isAvailable);
        
        return matchSearch && matchSpec && matchAvail;
    });
    
    if (sortBy === 'rating') {
        filtered.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
    } else if (sortBy === 'experience') {
        filtered.sort((a, b) => (b.experienceYears || 0) - (a.experienceYears || 0));
    } else if (sortBy === 'fee_low') {
        filtered.sort((a, b) => (a.consultationFee || 0) - (b.consultationFee || 0));
    } else if (sortBy === 'fee_high') {
        filtered.sort((a, b) => (b.consultationFee || 0) - (a.consultationFee || 0));
    }
    
    renderDoctors(filtered);
}

// ===== BOOK APPOINTMENT =====
function bookAppointment(doctorId, doctorName) {
    localStorage.setItem('selectedDoctorId', doctorId);
    localStorage.setItem('selectedDoctorName', 'Dr. ' + doctorName);
    window.location.href = `appointment_booking.html?doctorId=${doctorId}`;
}

// ===== VIEW PROFILE =====
function viewDoctorProfile(doctorId) {
    window.location.href = `doctor_profile.html?doctorId=${doctorId}`;
}

// ===== LOGOUT =====
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'login.html';
}

// ===== TOAST =====
function showToast(message, type = 'success') {
    const existing = document.querySelector('.toast');
    if (existing) existing.remove();
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
        ${message}
    `;
    document.body.appendChild(toast);
    setTimeout(() => {
        if (toast.parentNode) toast.remove();
    }, 3000);
}

// ===== INITIALIZE =====
document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    loadDoctors();
    
    document.getElementById('searchInput')?.addEventListener('input', searchDoctors);
    document.getElementById('specializationFilter')?.addEventListener('change', searchDoctors);
    document.getElementById('availabilityFilter')?.addEventListener('change', searchDoctors);
    document.getElementById('sortFilter')?.addEventListener('change', searchDoctors);
});
