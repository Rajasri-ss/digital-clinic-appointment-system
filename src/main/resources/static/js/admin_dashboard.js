// Admin Dashboard - HealthConnect
const API_BASE = 'http://localhost:8080';
const getToken = () => localStorage.getItem('token');
const authHeader = () => ({
    'Authorization': 'Bearer ' + getToken(),
    'Content-Type': 'application/json'
});

// Check authentication
function checkAuth() {
    if (!getToken()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// Show toast notification
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 2rem;
        background: ${type === 'success' ? '#10B981' : '#EF4444'};
        color: white;
        border-radius: 8px;
        z-index: 9999;
        font-weight: 600;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    `;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Load dashboard statistics
async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE}/api/admin/analytics/dashboard`, {
            headers: authHeader()
        });
        
        if (response.ok) {
            const data = await response.json();
            document.getElementById('totalPatients').textContent = data.totalPatients || 0;
            document.getElementById('totalDoctors').textContent = data.totalDoctors || 0;
            document.getElementById('todayAppointments').textContent = data.todayAppointments || 0;
            document.getElementById('totalRevenue').textContent = `$${(data.totalRevenue || 0).toFixed(2)}`;
        }
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
    }
}

// Load admin profile
async function loadAdminProfile() {
    try {
        const response = await fetch(`${API_BASE}/api/auth/profile`, {
            headers: authHeader()
        });
        
        if (response.ok) {
            const profile = await response.json();
            document.getElementById('adminName').textContent = profile.fullName || 'Admin';
        }
    } catch (error) {
        console.error('Error loading profile:', error);
    }
}

// Logout function
function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}

document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    
    loadDashboardStats();
    loadAdminProfile();
    
    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
});
