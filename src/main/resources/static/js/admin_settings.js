// Admin Settings - HealthConnect
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

// Load system settings
async function loadSettings() {
    try {
        const response = await fetch(`${API_BASE}/api/admin/settings`, {
            headers: authHeader()
        });
        
        if (response.ok) {
            const settings = await response.json();
            displaySettings(settings);
        }
    } catch (error) {
        console.error('Error loading settings:', error);
        showToast('Failed to load settings', 'error');
    }
}

// Display settings
function displaySettings(settings) {
    // Populate form fields with current settings
    if (settings.appointmentDuration) {
        const durationInput = document.getElementById('appointmentDuration');
        if (durationInput) durationInput.value = settings.appointmentDuration;
    }
    
    if (settings.maxAppointmentsPerDay) {
        const maxInput = document.getElementById('maxAppointmentsPerDay');
        if (maxInput) maxInput.value = settings.maxAppointmentsPerDay;
    }
    
    if (settings.emailNotifications !== undefined) {
        const emailCheckbox = document.getElementById('emailNotifications');
        if (emailCheckbox) emailCheckbox.checked = settings.emailNotifications;
    }
    
    if (settings.smsNotifications !== undefined) {
        const smsCheckbox = document.getElementById('smsNotifications');
        if (smsCheckbox) smsCheckbox.checked = settings.smsNotifications;
    }
}

// Save settings
async function saveSettings(event) {
    event.preventDefault();
    
    const settings = {
        appointmentDuration: document.getElementById('appointmentDuration')?.value,
        maxAppointmentsPerDay: document.getElementById('maxAppointmentsPerDay')?.value,
        emailNotifications: document.getElementById('emailNotifications')?.checked,
        smsNotifications: document.getElementById('smsNotifications')?.checked
    };
    
    try {
        const response = await fetch(`${API_BASE}/api/admin/settings`, {
            method: 'PUT',
            headers: authHeader(),
            body: JSON.stringify(settings)
        });
        
        if (response.ok) {
            showToast('Settings saved successfully');
        } else {
            showToast('Failed to save settings', 'error');
        }
    } catch (error) {
        console.error('Error saving settings:', error);
        showToast('Failed to save settings', 'error');
    }
}

// Logout function
function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}

document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    
    loadSettings();
    
    // Settings form
    const settingsForm = document.getElementById('settingsForm');
    if (settingsForm) {
        settingsForm.addEventListener('submit', saveSettings);
    }
    
    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
});
