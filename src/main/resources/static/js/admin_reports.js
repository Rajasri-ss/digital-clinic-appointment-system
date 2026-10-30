// Admin Reports & Analytics - HealthConnect
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

// Load analytics data
async function loadAnalytics() {
    try {
        const response = await fetch(`${API_BASE}/api/admin/analytics/reports`, {
            headers: authHeader()
        });
        
        if (response.ok) {
            const data = await response.json();
            displayAnalytics(data);
        }
    } catch (error) {
        console.error('Error loading analytics:', error);
        showToast('Failed to load analytics', 'error');
    }
}

// Display analytics data
function displayAnalytics(data) {
    // Update metrics
    if (data.appointmentStats) {
        document.getElementById('totalAppointments').textContent = data.appointmentStats.total || 0;
        document.getElementById('completedAppointments').textContent = data.appointmentStats.completed || 0;
        document.getElementById('cancelledAppointments').textContent = data.appointmentStats.cancelled || 0;
    }
    
    if (data.revenueStats) {
        document.getElementById('totalRevenue').textContent = `$${(data.revenueStats.total || 0).toFixed(2)}`;
        document.getElementById('monthlyRevenue').textContent = `$${(data.revenueStats.monthly || 0).toFixed(2)}`;
    }
}

// Generate report
async function generateReport(reportType) {
    try {
        const response = await fetch(`${API_BASE}/api/admin/analytics/generate-report?type=${reportType}`, {
            headers: authHeader()
        });
        
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `${reportType}_report_${new Date().toISOString().split('T')[0]}.pdf`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            showToast('Report generated successfully');
        } else {
            showToast('Failed to generate report', 'error');
        }
    } catch (error) {
        console.error('Error generating report:', error);
        showToast('Failed to generate report', 'error');
    }
}

// Logout function
function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}

document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    
    loadAnalytics();
    
    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
});
