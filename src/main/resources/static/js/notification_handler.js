/**
 * notification_handler.js
 * Global WebSocket Handler for Real-Time Toast Notifications
 */

let stompClient = null;

document.addEventListener('DOMContentLoaded', () => {

    // Inject Notification Container
    const container = document.createElement('div');
    container.id = 'notification-container';
    Object.assign(container.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        zIndex: '9999',
        display: 'flex',
        flexDirection: 'column',
        gap: '10px'
    });
    document.body.appendChild(container);

    // Initial connection to WebSocket logic
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    if (token && userId) {
        connectWebSocket(userId, token);
        // Also fetch initial unread count
        fetchUnreadCount(token);
    }
});

function connectWebSocket(userId, token) {
    // Check if SockJS is available
    if (typeof SockJS === 'undefined') {
        console.log('SockJS not loaded - skipping WebSocket connection');
        return;
    }
    
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // Disable debug logging for cleaner console
    stompClient.debug = null;

    const headers = {
        'Authorization': `Bearer ${token}`
    };

    stompClient.connect(headers, function (frame) {
        console.log('Connected to WebSocket for Notifications');

        // Subscribe to user-specific queue
        stompClient.subscribe(`/user/${userId}/queue/notifications`, function (message) {
            const notification = JSON.parse(message.body);
            showToast(notification);

            // Increment unread badge if it exists on page
            updateNotificationBadge(1);

            // If on dashboard, dynamically append to notification list if visible
            appendNotificationToList(notification);
        });

    }, function (error) {
        console.error('WebSocket connection error:', error);
        // Implement exponential backoff retry logic if needed
        setTimeout(() => connectWebSocket(userId, token), 5000);
    });
}

function showToast(notification) {
    const container = document.getElementById('notification-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${notification.type.toLowerCase()}`;

    // Toast Styling (Inline for transportability, though better in CSS)
    Object.assign(toast.style, {
        background: 'white',
        borderLeft: `4px solid ${getToastColor(notification.type)}`,
        borderRadius: '6px',
        boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.05)',
        padding: '16px',
        minWidth: '250px',
        transform: 'translateX(100%)',
        opacity: '0',
        transition: 'all 0.3s ease-out',
        display: 'flex',
        flexDirection: 'column',
        cursor: 'pointer'
    });

    const header = document.createElement('div');
    header.style.display = 'flex';
    header.style.justifyContent = 'space-between';
    header.style.alignItems = 'center';
    header.style.marginBottom = '4px';

    const title = document.createElement('strong');
    title.textContent = notification.title;
    title.style.color = '#1e293b';
    title.style.fontSize = '0.9rem';

    const time = document.createElement('small');
    time.textContent = 'Just now';
    time.style.color = '#64748b';
    time.style.fontSize = '0.75rem';

    const body = document.createElement('div');
    body.textContent = notification.message;
    body.style.color = '#475569';
    body.style.fontSize = '0.85rem';

    header.appendChild(title);
    header.appendChild(time);
    toast.appendChild(header);
    toast.appendChild(body);

    container.appendChild(toast);

    // Animate In
    requestAnimationFrame(() => {
        toast.style.transform = 'translateX(0)';
        toast.style.opacity = '1';
    });

    // Auto dismiss after 5 seconds
    setTimeout(() => {
        dismissToast(toast);
    }, 5000);

    // Click to dismiss
    toast.addEventListener('click', () => dismissToast(toast));
}

function dismissToast(toast) {
    toast.style.transform = 'translateX(100%)';
    toast.style.opacity = '0';
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 300);
}

function getToastColor(type) {
    switch (type) {
        case 'INFO': return '#3b82f6';
        case 'WARNING': return '#f59e0b';
        case 'SYSTEM': return '#64748b';
        case 'APPOINTMENT': return '#10b981';
        case 'PAYMENT': return '#8b5cf6';
        case 'REMINDER': return '#ec4899';
        default: return '#4f46e5'; // Primary
    }
}

async function fetchUnreadCount(token) {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) return;
        
        const response = await fetch(`/api/notifications/user/${userId}/unread-count`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            updateNotificationBadge(data.count, true);
        }
    } catch (e) {
        console.error("Error fetching unread count", e);
    }
}

function updateNotificationBadge(count, isAbsolute = false) {
    const badges = document.querySelectorAll('.notification-badge');
    badges.forEach(badge => {
        let current = parseInt(badge.textContent || '0');
        let newCount = isAbsolute ? count : current + count;

        badge.textContent = newCount;

        // Hide badge if 0
        if (newCount <= 0) {
            badge.style.display = 'none';
        } else {
            badge.style.display = 'inline-flex';
            // Simple bounce animation
            badge.style.transform = 'scale(1.2)';
            setTimeout(() => badge.style.transform = 'scale(1)', 200);
        }
    });
}

function appendNotificationToList(notification) {
    const list = document.getElementById('notificationListDropdown');
    if (!list) return;

    const emptyState = list.querySelector('.empty-state');
    if (emptyState) {
        emptyState.remove();
    }

    const item = document.createElement('div');
    item.className = 'notification-item new unread';
    // Style definition depends on dashboard CSS, but we inject basic styling

    // Assuming a structured item matches the dashboard code
    item.innerHTML = `
        <div class="notification-icon ${notification.type.toLowerCase()}">
            <i class="fa-solid fa-bell"></i>
        </div>
        <div class="notification-content">
            <h4>${notification.title}</h4>
            <p>${notification.message}</p>
            <span class="notification-time">Just now</span>
        </div>
    `;

    list.insertBefore(item, list.firstChild);
}
