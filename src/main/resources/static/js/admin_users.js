/**
 * Admin Users Management - HealthConnect
 * Handles user listing, filtering, and doctor approval
 */

const API_BASE = '/api/admin/users';
const getToken = () => localStorage.getItem('token');
const authHeader = () => ({
    'Authorization': 'Bearer ' + getToken(),
    'Content-Type': 'application/json'
});

let currentPage = 0;
const pageSize = 10;
let currentModalType = null; // 'doctor', 'patient', 'admin'
let currentModalId = null;

// Check authentication
function checkAuth() {
    const token = getToken();
    const role = localStorage.getItem('userRole');

    if (!token || role !== 'ADMIN') {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}

// Show toast notification
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease-in';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Tab switching
function setupTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            // Update active tab button
            tabButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            // Show corresponding tab content
            const target = btn.dataset.target;
            document.querySelectorAll('.tab-content-section').forEach(section => {
                section.classList.remove('active');
            });
            document.getElementById(target).classList.add('active');
            
            // Reset page and load users
            currentPage = 0;
            loadUsers();
        });
    });
}

// Load users based on current tab
async function loadUsers() {
    const currentTab = document.querySelector('.tab-btn.active')?.dataset.target || 'patientsTab';
    let tbodyId = '';
    let endpoint = '';

    if (currentTab === 'patientsTab') {
        tbodyId = 'patientsTableBody';
        endpoint = `${API_BASE}/patients?page=${currentPage}&size=${pageSize}`;
    } else if (currentTab === 'doctorsTab') {
        tbodyId = 'doctorsTableBody';
        endpoint = `${API_BASE}/doctors?page=${currentPage}&size=${pageSize}`;
    } else {
        tbodyId = 'adminsTableBody';
        endpoint = `${API_BASE}/admins?page=${currentPage}&size=${pageSize}`;
    }

    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="7" class="text-center">Loading...</td></tr>';

    try {
        const response = await fetch(endpoint, {
            headers: authHeader()
        });

        if (response.ok) {
            const data = await response.json();
            
            if (currentTab === 'patientsTab') {
                displayPatients(data.content || [], data.totalPages || 1);
            } else if (currentTab === 'doctorsTab') {
                displayDoctors(data.content || [], data.totalPages || 1);
            } else {
                displayAdmins(data.content || [], data.totalPages || 1);
            }

            updatePagination(currentTab, currentPage, data.totalPages || 1);
        } else if (response.status === 403) {
            showToast('Access denied. Admin privileges required.', 'error');
            setTimeout(() => window.location.href = '/login.html', 2000);
        } else {
            throw new Error('Failed to load users');
        }
    } catch (error) {
        console.error('Error loading users:', error);
        tbody.innerHTML = '<tr><td colspan="7" class="text-center" style="color: #EF4444;">Failed to load users. Please try again.</td></tr>';
        showToast('Failed to load users', 'error');
    }
}

// Display patients
function displayPatients(patients, totalPages) {
    const tbody = document.getElementById('patientsTableBody');
    if (!tbody) return;

    if (patients.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No patients found</td></tr>';
        return;
    }

    tbody.innerHTML = patients.map(patient => `
        <tr>
            <td>#${patient.id}</td>
            <td>${patient.fullName || 'N/A'}</td>
            <td>${patient.user?.email || 'N/A'}</td>
            <td>${patient.user?.phone || 'N/A'}</td>
            <td><span class="badge ${patient.user?.isActive ? 'badge-success' : 'badge-danger'}">${patient.user?.isActive ? 'Active' : 'Inactive'}</span></td>
            <td>${formatDate(patient.user?.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    <button onclick="toggleUserStatus(${patient.user?.id}, 'patient')" class="action-btn toggle" title="Toggle Status">
                        <i class="fas fa-toggle-${patient.user?.isActive ? 'on' : 'off'}"></i>
                        <span>${patient.user?.isActive ? 'On' : 'Off'}</span>
                    </button>
                    <button onclick="viewUserDetails(${patient.id}, 'patient')" class="action-btn view" title="View Details">
                        <i class="fas fa-eye"></i>
                        <span>View</span>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Display doctors
function displayDoctors(doctors, totalPages) {
    const tbody = document.getElementById('doctorsTableBody');
    if (!tbody) return;

    if (doctors.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">No doctors found</td></tr>';
        return;
    }

    tbody.innerHTML = doctors.map(doctor => `
        <tr>
            <td>#${doctor.id}</td>
            <td>
                <div style="font-weight: 600;">${doctor.fullName || 'N/A'}</div>
                <div style="font-size: 0.85rem; color: #6B7280;">${doctor.specialization || doctor.specialty || 'N/A'}</div>
            </td>
            <td>${doctor.specialization || doctor.specialty || 'N/A'}</td>
            <td>${doctor.user?.email || 'N/A'}</td>
            <td>${doctor.user?.phone || 'N/A'}</td>
            <td>
                <span class="badge ${doctor.isVerified ? 'badge-success' : 'badge-warning'}">
                    ${doctor.isVerified ? 'Verified' : 'Pending'}
                </span>
                ${doctor.user?.isActive ? '' : '<span class="badge badge-danger" style="margin-left: 0.25rem;">Inactive</span>'}
            </td>
            <td>${formatDate(doctor.user?.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    ${!doctor.isVerified ? `
                        <button onclick="approveDoctor(${doctor.id})" class="action-btn approve" title="Approve Doctor">
                            <i class="fas fa-check-circle"></i>
                            <span>Approve</span>
                        </button>
                        <button onclick="rejectDoctor(${doctor.id})" class="action-btn reject" title="Reject Doctor">
                            <i class="fas fa-times-circle"></i>
                            <span>Reject</span>
                        </button>
                    ` : ''}
                    <button onclick="toggleUserStatus(${doctor.user?.id}, 'doctor')" class="action-btn toggle" title="Toggle Status">
                        <i class="fas fa-toggle-${doctor.user?.isActive ? 'on' : 'off'}"></i>
                        <span>${doctor.user?.isActive ? 'On' : 'Off'}</span>
                    </button>
                    <button onclick="viewUserDetails(${doctor.id}, 'doctor')" class="action-btn view" title="View Details">
                        <i class="fas fa-eye"></i>
                        <span>View</span>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Display admins
function displayAdmins(users, totalPages) {
    const tbody = document.getElementById('adminsTableBody');
    if (!tbody) return;

    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No admins found</td></tr>';
        return;
    }

    tbody.innerHTML = users.map(admin => `
        <tr>
            <td>#${admin.id}</td>
            <td>${admin.firstName || ''} ${admin.lastName || ''}</td>
            <td>${admin.email || 'N/A'}</td>
            <td>${admin.phone || 'N/A'}</td>
            <td><span class="badge ${admin.active ? 'badge-success' : 'badge-danger'}">${admin.active ? 'Active' : 'Inactive'}</span></td>
            <td>${formatDate(admin.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    <button onclick="toggleUserStatus(${admin.id}, 'admin')" class="action-btn toggle" title="Toggle Status">
                        <i class="fas fa-toggle-${admin.active ? 'on' : 'off'}"></i>
                        <span>${admin.active ? 'On' : 'Off'}</span>
                    </button>
                    <button onclick="viewUserDetails(${admin.id}, 'admin')" class="action-btn view" title="View Details">
                        <i class="fas fa-eye"></i>
                        <span>View</span>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Approve doctor
async function approveDoctor(doctorId) {
    if (!confirm('Are you sure you want to approve this doctor?')) return;

    try {
        const response = await fetch(`${API_BASE}/doctors/${doctorId}/approve`, {
            method: 'PATCH',
            headers: authHeader()
        });

        const data = await response.json();

        if (response.ok) {
            showToast('Doctor approved successfully!', 'success');
            loadUsers();
        } else {
            console.error('Approval error:', data);
            showToast(data.error || 'Failed to approve doctor', 'error');
        }
    } catch (error) {
        console.error('Error approving doctor:', error);
        showToast('Failed to approve doctor', 'error');
    }
}

// Reject doctor
async function rejectDoctor(doctorId) {
    if (!confirm('Are you sure you want to reject this doctor? This will set their status to unverified.')) return;

    try {
        const response = await fetch(`${API_BASE}/doctors/${doctorId}/reject`, {
            method: 'PATCH',
            headers: authHeader()
        });

        if (response.ok) {
            showToast('Doctor rejected', 'success');
            loadUsers();
        } else {
            const error = await response.json();
            showToast(error.error || 'Failed to reject doctor', 'error');
        }
    } catch (error) {
        console.error('Error rejecting doctor:', error);
        showToast('Failed to reject doctor', 'error');
    }
}

// Toggle user status
async function toggleUserStatus(userId, userType) {
    if (!confirm('Are you sure you want to toggle this user\'s status?')) return;

    try {
        const response = await fetch(`${API_BASE}/${userId}/toggle-status`, {
            method: 'PATCH',
            headers: authHeader()
        });

        if (response.ok) {
            showToast('User status updated', 'success');
            loadUsers();
        } else {
            showToast('Failed to update user status', 'error');
        }
    } catch (error) {
        console.error('Error toggling user status:', error);
        showToast('Failed to update user status', 'error');
    }
}

// View user details in modal
async function viewUserDetails(id, type) {
    currentModalType = type;
    currentModalId = id;
    
    try {
        let endpoint = '';
        if (type === 'doctor') {
            endpoint = `${API_BASE}/doctors/${id}`;
        } else if (type === 'patient') {
            endpoint = `${API_BASE}/patients/${id}`;
        } else {
            endpoint = `${API_BASE}/${id}`;
        }

        const response = await fetch(endpoint, {
            headers: authHeader()
        });

        if (response.ok) {
            const data = await response.json();
            renderUserModal(data, type);
            document.getElementById('userDetailsModal').classList.add('active');
        } else {
            showToast('Failed to load user details', 'error');
        }
    } catch (error) {
        console.error('Error fetching user details:', error);
        showToast('Failed to load user details', 'error');
    }
}

// Render user modal content
function renderUserModal(user, type) {
    const modalBody = document.getElementById('modalBody');
    const modalFooter = document.getElementById('modalFooter');
    const modalTitle = document.getElementById('modalTitle');

    // Set modal title
    if (type === 'doctor') {
        modalTitle.textContent = 'Doctor Details';
    } else if (type === 'patient') {
        modalTitle.textContent = 'Patient Details';
    } else {
        modalTitle.textContent = 'Admin Details';
    }

    // Build modal content based on user type
    let content = '';
    let actions = '';

    if (type === 'doctor') {
        content = `
            <div class="modal-profile-header">
                <div class="profile-avatar">
                    <i class="fas fa-user-md"></i>
                </div>
                <div class="profile-info">
                    <h3>${user.fullName || 'N/A'}</h3>
                    <p class="profile-specialty">${user.specialization || 'N/A'}</p>
                </div>
            </div>
            <div class="modal-details-grid">
                <div class="detail-item">
                    <span class="detail-label">Email</span>
                    <span class="detail-value">${user.user?.email || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Phone</span>
                    <span class="detail-value">${user.user?.phone || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">License Number</span>
                    <span class="detail-value">${user.licenseNumber || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Qualifications</span>
                    <span class="detail-value">${user.qualifications || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Experience</span>
                    <span class="detail-value">${user.experienceYears || 0} years</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Consultation Fee</span>
                    <span class="detail-value">${user.consultationFee ? '$' + user.consultationFee : 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Languages</span>
                    <span class="detail-value">${user.languagesSpoken || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Rating</span>
                    <span class="detail-value">${user.averageRating ? user.averageRating + ' / 5.00' : 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">About</span>
                    <span class="detail-value">${user.about || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Status</span>
                    <span class="detail-value badge ${user.isVerified ? 'badge-success' : 'badge-warning'}">
                        ${user.isVerified ? 'Verified' : 'Pending'}
                    </span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Available</span>
                    <span class="detail-value badge ${user.isAvailable ? 'badge-success' : 'badge-danger'}">
                        ${user.isAvailable ? 'Yes' : 'No'}
                    </span>
                </div>
            </div>
        `;

        // Doctor action buttons
        if (!user.isVerified) {
            actions = `
                <button class="action-btn approve" onclick="confirmDoctorAction(${user.id}, 'approve')">
                    <i class="fas fa-check-circle"></i> Approve
                </button>
                <button class="action-btn reject" onclick="confirmDoctorAction(${user.id}, 'reject')">
                    <i class="fas fa-times-circle"></i> Reject
                </button>
            `;
        }
        actions += `
            <button class="action-btn toggle" onclick="toggleUserStatus(${user.user?.id}, 'doctor')">
                <i class="fas fa-toggle-${user.user?.isActive ? 'on' : 'off'}"></i> ${user.user?.isActive ? 'Suspend' : 'Activate'}
            </button>
        `;

    } else if (type === 'patient') {
        content = `
            <div class="modal-profile-header">
                <div class="profile-avatar">
                    <i class="fas fa-user-injured"></i>
                </div>
                <div class="profile-info">
                    <h3>${user.fullName || 'N/A'}</h3>
                    <p class="profile-specialty">Patient</p>
                </div>
            </div>
            <div class="modal-details-grid">
                <div class="detail-item">
                    <span class="detail-label">Email</span>
                    <span class="detail-value">${user.user?.email || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Phone</span>
                    <span class="detail-value">${user.user?.phone || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Date of Birth</span>
                    <span class="detail-value">${user.dob ? formatDate(user.dob) : 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Gender</span>
                    <span class="detail-value">${user.gender || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Blood Group</span>
                    <span class="detail-value">${user.bloodGroup || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Address</span>
                    <span class="detail-value">${user.address || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">City</span>
                    <span class="detail-value">${user.city || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">State</span>
                    <span class="detail-value">${user.state || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Pincode</span>
                    <span class="detail-value">${user.pincode || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Emergency Contact</span>
                    <span class="detail-value">${user.emergencyContact || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Allergies</span>
                    <span class="detail-value">${user.allergies || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Chronic Conditions</span>
                    <span class="detail-value">${user.chronicConditions || 'N/A'}</span>
                </div>
            </div>
        `;

        // Patient action buttons
        actions = `
            <button class="action-btn toggle" onclick="toggleUserStatus(${user.user?.id}, 'patient')">
                <i class="fas fa-toggle-${user.user?.isActive ? 'on' : 'off'}"></i> ${user.user?.isActive ? 'Suspend' : 'Activate'}
            </button>
        `;

    } else {
        // Admin
        content = `
            <div class="modal-profile-header">
                <div class="profile-avatar">
                    <i class="fas fa-user-shield"></i>
                </div>
                <div class="profile-info">
                    <h3>${user.firstName || ''} ${user.lastName || ''}</h3>
                    <p class="profile-specialty">Admin</p>
                </div>
            </div>
            <div class="modal-details-grid">
                <div class="detail-item">
                    <span class="detail-label">Email</span>
                    <span class="detail-value">${user.email || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Phone</span>
                    <span class="detail-value">${user.phone || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Role</span>
                    <span class="detail-value badge badge-primary">${user.role || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Status</span>
                    <span class="detail-value badge ${user.active ? 'badge-success' : 'badge-danger'}">
                        ${user.active ? 'Active' : 'Inactive'}
                    </span>
                </div>
            </div>
        `;

        // Admin action buttons
        actions = `
            <button class="action-btn toggle" onclick="toggleUserStatus(${user.id}, 'admin')">
                <i class="fas fa-toggle-${user.active ? 'on' : 'off'}"></i> ${user.active ? 'Suspend' : 'Activate'}
            </button>
        `;
    }

    modalBody.innerHTML = content;
    modalFooter.innerHTML = actions;
}

// Confirm doctor action (approve/reject)
function confirmDoctorAction(doctorId, action) {
    const actionTitle = action === 'approve' ? 'Approve Doctor' : 'Reject Doctor';
    const actionMessage = action === 'approve' 
        ? 'Are you sure you want to approve this doctor? This will make them available to patients.'
        : 'Are you sure you want to reject this doctor? This will set their status to unverified.';
    
    document.getElementById('actionModalTitle').textContent = actionTitle;
    document.getElementById('actionModalMessage').textContent = actionMessage;
    document.getElementById('actionUserId').value = doctorId;
    document.getElementById('actionType').value = action;
    
    document.getElementById('actionModal').classList.add('active');
}

// Close modals
function closeModal() {
    document.getElementById('userDetailsModal').classList.remove('active');
    currentModalType = null;
    currentModalId = null;
}

function closeActionModal() {
    document.getElementById('actionModal').classList.remove('active');
}

// Handle action modal confirm
document.getElementById('confirmActionBtn').addEventListener('click', async function() {
    const doctorId = document.getElementById('actionUserId').value;
    const action = document.getElementById('actionType').value;
    
    if (action === 'approve') {
        await approveDoctor(doctorId);
    } else if (action === 'reject') {
        await rejectDoctor(doctorId);
    }
    
    closeModal();
    closeActionModal();
    loadUsers();
});

// Close modal when clicking outside
document.getElementById('userDetailsModal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeModal();
    }
});

document.getElementById('actionModal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeActionModal();
    }
});

// Format date
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

// Pagination
function updatePagination(tab, currentPage, totalPages) {
    let pageIndicator, prevBtn, nextBtn;

    if (tab === 'patientsTab') {
        pageIndicator = document.getElementById('patientsPageIndicator');
        prevBtn = document.getElementById('prevPatientsPage');
        nextBtn = document.getElementById('nextPatientsPage');
    } else if (tab === 'doctorsTab') {
        pageIndicator = document.getElementById('doctorsPageIndicator');
        prevBtn = document.getElementById('prevDoctorsPage');
        nextBtn = document.getElementById('nextDoctorsPage');
    } else {
        pageIndicator = document.getElementById('adminsPageIndicator');
        prevBtn = document.getElementById('prevAdminsPage');
        nextBtn = document.getElementById('nextAdminsPage');
    }

    if (pageIndicator) {
        pageIndicator.textContent = `Page ${currentPage + 1} of ${totalPages}`;
    }
    if (prevBtn) {
        prevBtn.disabled = currentPage === 0;
    }
    if (nextBtn) {
        nextBtn.disabled = currentPage >= totalPages - 1;
    }
}

function goToPage(tab, direction) {
    if (direction === 'prev' && currentPage > 0) {
        currentPage--;
        loadUsers();
    } else if (direction === 'next' && currentPage < 10) {
        currentPage++;
        loadUsers();
    }
}

// Setup search functionality
function setupSearch() {
    document.querySelectorAll('.search-input').forEach(input => {
        input.addEventListener('input', (e) => {
            const searchTerm = e.target.value.toLowerCase();
            const tbody = e.target.closest('.table-card').querySelector('tbody');
            const rows = tbody.querySelectorAll('tr');
            
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(searchTerm) ? '' : 'none';
            });
        });
    });
}

// Export CSV
function exportToCSV(tab) {
    showToast('Export functionality coming soon', 'info');
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;

    setupTabs();
    setupSearch();
    loadUsers();

    // Setup pagination buttons for each tab
    document.getElementById('prevPatientsPage').addEventListener('click', () => goToPage('patients', 'prev'));
    document.getElementById('nextPatientsPage').addEventListener('click', () => goToPage('patients', 'next'));
    document.getElementById('prevDoctorsPage').addEventListener('click', () => goToPage('doctors', 'prev'));
    document.getElementById('nextDoctorsPage').addEventListener('click', () => goToPage('doctors', 'next'));
    document.getElementById('prevAdminsPage').addEventListener('click', () => goToPage('admins', 'prev'));
    document.getElementById('nextAdminsPage').addEventListener('click', () => goToPage('admins', 'next'));

    // Setup export buttons
    document.getElementById('exportPatientsBtn').addEventListener('click', () => exportToCSV('patients'));
    document.getElementById('exportDoctorsBtn').addEventListener('click', () => exportToCSV('doctors'));
    document.getElementById('exportAdminsBtn').addEventListener('click', () => exportToCSV('admins'));
});
