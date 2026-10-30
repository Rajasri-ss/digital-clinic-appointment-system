let calendar;
let allAppointments = [];
let selectedDateForSchedule = null;
let selectedLocationId = null;

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

    initFilters();
    initModal();
    initScheduleEditModal();
    fetchAndRenderSchedule(token);
});

async function fetchAndRenderSchedule(token) {
    document.getElementById('calendar-loader').style.display = 'block';
    if (calendar) {
        document.getElementById('calendar').innerHTML = ''; // Clear existing
    }

    try {
        const response = await fetch('/api/doctors/appointments?size=100', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("Failed to load schedule");

        const data = await response.json();
        allAppointments = data.content || data; // handle pagination structure

        renderCalendar();
    } catch (e) {
        console.error(e);
        alert('Could not load appointments.');
    } finally {
        document.getElementById('calendar-loader').style.display = 'none';
    }
}

function initFilters() {
    const defaultFilters = ['PENDING', 'CONFIRMED', 'COMPLETED'];

    const allCheckbox = document.getElementById('filter-all');
    const statusCheckboxes = document.querySelectorAll('.status-filter');
    const emCheckbox = document.getElementById('filter-emergency');

    allCheckbox.addEventListener('change', (e) => {
        if (e.target.checked) {
            statusCheckboxes.forEach(cb => cb.checked = false);
            updateCalendarEvents();
        }
    });

    statusCheckboxes.forEach(cb => {
        cb.addEventListener('change', () => {
            if (cb.checked) allCheckbox.checked = false;

            // If all are unchecked, check 'All'
            const anyChecked = Array.from(statusCheckboxes).some(c => c.checked);
            if (!anyChecked) allCheckbox.checked = true;

            updateCalendarEvents();
        });
    });

    emCheckbox.addEventListener('change', () => updateCalendarEvents());
}

function updateCalendarEvents() {
    if (!calendar) return;
    calendar.removeAllEvents();
    calendar.addEventSource(getFilteredEvents());
}

function getFilteredEvents() {
    const allChecked = document.getElementById('filter-all').checked;
    const statusCheckboxes = document.querySelectorAll('.status-filter');
    const activeStatuses = Array.from(statusCheckboxes)
        .filter(cb => cb.checked)
        .map(cb => cb.value);

    const emChecked = document.getElementById('filter-emergency').checked;

    const events = [];

    allAppointments.forEach(app => {
        if (!app.appointmentDate) return;

        // Apply filters
        if (!allChecked && !activeStatuses.includes(app.status)) return;

        const isEmergency = app.isEmergency === true || (app.priority && app.priority.toLowerCase() === 'high');
        if (emChecked && !isEmergency) return;

        // Map to FullCalendar Event Format
        const dateStr = app.appointmentDate.split('T')[0];
        const timeStr = app.appointmentTime || "00:00:00";
        const startDateTime = `${dateStr}T${timeStr}`;

        let color = '#4F46E5'; // primary (Completed/Default)
        if (app.status === 'PENDING') color = '#F59E0B'; // warning
        if (app.status === 'CONFIRMED') color = '#10B981'; // success
        if (isEmergency && app.status !== 'COMPLETED') color = '#EF4444'; // danger overrides

        events.push({
            id: app.appointmentId || app.id,
            title: `${app.patientName || app.patient?.name || 'Patient'} ${isEmergency ? '(URGENT)' : ''}`,
            start: startDateTime,
            backgroundColor: color,
            borderColor: color,
            extendedProps: { ...app, isEmergency }
        });
    });

    return events;
}

function renderCalendar() {
    const calendarEl = document.getElementById('calendar');
    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'timeGridWeek',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        slotMinTime: '08:00:00',
        slotMaxTime: '20:00:00',
        allDaySlot: false,
        events: getFilteredEvents(),
        dateClick: function(info) {
            handleDateClick(info);
        },
        eventClick: function (info) {
            console.log(info.event);
            
            showAppointmentDetails(info.event.extendedProps, info.event.id);
        }
    });
    calendar.render();
}

function handleDateClick(info) {
    // Extract date part from the ISO datetime string
    // info.dateStr can be in format "2026-03-17T09:00:00+05:30" or "2026-03-17"
    let dateStr = info.dateStr;
    if (dateStr.includes('T')) {
        dateStr = dateStr.split('T')[0];
    }
    const dayOfWeek = getDayOfWeek(dateStr);
    
    // Get the current location from the calendar view
    // For now, we'll use a default location or prompt the user
    // In a real app, you might want to show a location selector
    selectedLocationId = 1; // Default location - in production, get from user selection
    
    selectedDateForSchedule = {
        date: dateStr,
        dayOfWeek: dayOfWeek,
        locationId: selectedLocationId
    };
    
    // Open the schedule edit modal
    openScheduleEditModal(dateStr, dayOfWeek, selectedLocationId);
}

function getDayOfWeek(dateStr) {
    const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
    const date = new Date(dateStr);
    return days[date.getDay()];
}

function showAppointmentDetails(app, id) {
    const card = document.getElementById('appointment-details-card');
    const content = document.getElementById('details-content');
    const actions = document.getElementById('details-actions');

    const statusBadgeColors = {
        'PENDING': 'var(--warning)',
        'CONFIRMED': 'var(--secondary)',
        'COMPLETED': 'var(--primary)',
        'CANCELLED': 'var(--danger)',
        'SCHEDULED': 'var(--warning)',
        'IN_PROGRESS': '#F59E0B',
        'NO_SHOW': 'gray'
    };
    const c = statusBadgeColors[app.status] || 'gray';

    content.innerHTML = `
        <div class="details-item">
            <label>Patient</label>
            <div>${app.patientName || app.patient?.name || 'Unknown'} ${app.isEmergency ? '<i class="fas fa-exclamation-circle text-danger"></i>' : ''}</div>
        </div>
        <div class="details-item">
            <label>Time</label>
            <div>${app.appointmentDate?.split('T')[0]} @ ${app.appointmentTime || '--:--'}</div>
        </div>
        <div class="details-item">
            <label>Status</label>
            <div style="color:${c}; font-weight:bold;">${app.status}</div>
        </div>
        <div class="details-item">
            <label>Reason</label>
            <div>${app.reasonForVisit || app.reason || app.chiefComplaint || 'Not provided'}</div>
        </div>
    `;

    // Use appointmentId if id is undefined
    const appointmentId = id || app.appointmentId || app.id;

    // actions
    let btnHtml = `<button class="btn btn-primary w-100 mb-2" onclick="window.location.href='app_updation.html?id=${appointmentId}'"><i class="fas fa-stethoscope"></i> Process Appointment</button>`;
    if (app.status === 'PENDING' || app.status === 'SCHEDULED') {
        btnHtml += `<button class="btn btn-success" onclick="openActionModal(${appointmentId}, 'CONFIRM')">Confirm</button>`;
        btnHtml += `<button class="btn btn-outline-danger" onclick="openActionModal(${appointmentId}, 'CANCEL')">Cancel</button>`;
    } else if (app.status === 'CONFIRMED' || app.status === 'IN_PROGRESS') {
        btnHtml += `<button class="btn btn-primary" onclick="openActionModal(${appointmentId}, 'COMPLETE')">Mark Completed</button>`;
        btnHtml += `<button class="btn btn-outline-danger" onclick="openActionModal(${appointmentId}, 'CANCEL')">Cancel</button>`;
    } else {
        btnHtml += `<p class="text-muted text-sm">No other actions available.</p>`;
    }
    actions.innerHTML = btnHtml;

    card.style.display = 'block';
}

function refreshSchedule() {
    fetchAndRenderSchedule(localStorage.getItem('token'));
    document.getElementById('appointment-details-card').style.display = 'none';
}

// Modal Logic
const modal = document.getElementById('actionModal');
const closeBtns = document.querySelectorAll('.close, .close-btn');

function initModal() {
    closeBtns.forEach(b => b.onclick = () => modal.style.display = 'none');
    window.onclick = e => { if (e.target == modal) modal.style.display = 'none'; }
    document.getElementById('confirmActionBtn').onclick = executeAction;
}

function openActionModal(id, type) {
    document.getElementById('actionApptId').value = id;
    document.getElementById('actionType').value = type;

    let title = 'Update Appointment';
    let text = 'Proceed with updating this appointment?';
    let btnClass = 'btn-primary';

    if (type === 'CONFIRM') { title = 'Confirm Appointment'; text = 'Confirm this appointment and notify the patient?'; btnClass = 'btn-success'; }
    if (type === 'CANCEL') { title = 'Cancel Appointment'; text = 'Are you sure you want to cancel? This cannot be undone.'; btnClass = 'btn-danger'; }
    if (type === 'COMPLETE') { title = 'Complete Appointment'; text = 'Mark this appointment as completed?'; btnClass = 'btn-primary'; }

    document.getElementById('actionModalTitle').textContent = title;
    document.getElementById('actionModalText').textContent = text;

    const confirmBtn = document.getElementById('confirmActionBtn');
    confirmBtn.className = `btn ${btnClass}`;
    confirmBtn.textContent = 'Yes, Proceed';

    modal.style.display = 'flex';
}

async function executeAction() {
    const id = document.getElementById('actionApptId').value;
    const type = document.getElementById('actionType').value;
    const token = localStorage.getItem('token');

    // Map action types to status values
    const statusMap = {
        'CONFIRM': 'CONFIRMED',
        'CANCEL': 'CANCELLED',
        'COMPLETE': 'COMPLETED'
    };

    const status = statusMap[type] || type;
    const endpoint = `/api/appointments/${id}/status?status=${status}`;

    try {
        const response = await fetch(endpoint, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Action failed");
        }

        modal.style.display = 'none';
        refreshSchedule();
    } catch (e) {
        alert(e.message);
        console.error('Error executing action:', e);
    }
}

// Schedule Edit Modal Logic
const scheduleEditModal = document.getElementById('scheduleEditModal');
const scheduleEditCloseBtns = document.querySelectorAll('.close, .close-btn');

function initScheduleEditModal() {
    scheduleEditCloseBtns.forEach(b => b.onclick = () => scheduleEditModal.style.display = 'none');
    window.onclick = e => { if (e.target == scheduleEditModal) scheduleEditModal.style.display = 'none'; }
    document.getElementById('saveScheduleBtn').onclick = saveSchedule;
}

function openScheduleEditModal(date, dayOfWeek, locationId) {
    document.getElementById('scheduleEditDateDisplay').textContent = `Editing schedule for: ${formatDate(date)} (${dayOfWeek})`;
    document.getElementById('editLocationId').value = locationId;
    document.getElementById('editDayOfWeek').value = dayOfWeek;
    document.getElementById('editAppointmentDate').value = date;
    
    // Set default times (you could fetch existing schedule here)
    document.getElementById('editStartTime').value = '09:00';
    document.getElementById('editEndTime').value = '17:00';
    
    scheduleEditModal.style.display = 'flex';
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}

async function saveSchedule() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('Please log in again');
        window.location.href = 'login.html';
        return;
    }

    const startTime = document.getElementById('editStartTime').value;
    const endTime = document.getElementById('editEndTime').value;
    const locationId = document.getElementById('editLocationId').value;
    const dayOfWeek = document.getElementById('editDayOfWeek').value;
    const appointmentDate = document.getElementById('editAppointmentDate').value;

    // Validate times
    if (!startTime || !endTime) {
        alert('Please select both start and end times');
        return;
    }

    if (startTime >= endTime) {
        alert('Start time must be before end time');
        return;
    }

    try {
        const response = await fetch('/api/doctors/schedules/update', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                locationId: parseInt(locationId),
                dayOfWeek: dayOfWeek,
                appointmentDate: appointmentDate,
                startTime: startTime,
                endTime: endTime
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Failed to update schedule");
        }

        const result = await response.json();
        alert(result.message || 'Schedule updated successfully');
        
        scheduleEditModal.style.display = 'none';
        refreshSchedule();
    } catch (e) {
        alert(e.message);
        console.error('Error saving schedule:', e);
    }
}
