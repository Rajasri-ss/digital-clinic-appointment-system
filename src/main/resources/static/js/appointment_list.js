const getToken = () => localStorage.getItem('token');
        const authHeader = () => ({ 'Authorization': 'Bearer ' + getToken(), 'Content-Type': 'application/json' });

        let allAppointments = [];
        let currentTab = 'UPCOMING';

        document.addEventListener('DOMContentLoaded', () => {
            if (!getToken()) {
                window.location.href = 'login.html';
                return;
            }
            fetchAppointments();
        });

        async function fetchAppointments() {
            document.getElementById('loader').style.display = 'block';
            document.getElementById('appointmentsList').innerHTML = '';

            try {
                // Fetching a large page for simplicity, pagination can be added
                const response = await fetch('/api/appointments/patient?page=0&size=50', { headers: authHeader() });
                if (response.ok) {
                    const data = await response.json();
                    allAppointments = data.content;
                    renderAppointments();
                } else {
                    document.getElementById('appointmentsList').innerHTML = '<p style="color:red; text-align:center;">Failed to load appointments.</p>';
                }
            } catch (err) {
                console.error(err);
            } finally {
                document.getElementById('loader').style.display = 'none';
            }
        }

        function switchTab(tab) {
            currentTab = tab;
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            event.target.classList.add('active');
            renderAppointments();
        }

        function renderAppointments() {
            const container = document.getElementById('appointmentsList');
            container.innerHTML = '';

            let filtered = allAppointments.filter(app => {
                if (currentTab === 'UPCOMING') return app.status === 'SCHEDULED' || app.status === 'IN_PROGRESS';
                if (currentTab === 'COMPLETED') return app.status === 'COMPLETED';
                if (currentTab === 'CANCELLED') return app.status === 'CANCELLED' || app.status === 'NO_SHOW';
            });

            if (filtered.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <i class="far fa-calendar-times"></i>
                        <h3>No ${currentTab.toLowerCase()} appointments</h3>
                        <p>You don't have any matching records at the moment.</p>
                        ${currentTab === 'UPCOMING' ? '<a href="doctor_search.html" class="btn-new">Book an Appointment</a>' : ''}
                    </div>`;
                return;
            }

            const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

            filtered.forEach(app => {
                const dateObj = new Date(app.appointmentDate);
                const month = months[dateObj.getMonth()];
                const day = dateObj.getDate();
                const time = app.appointmentTime.substring(0, 5); // HH:MM

                let statusClass = 'status-scheduled';
                if (app.status === 'COMPLETED') statusClass = 'status-completed';
                if (app.status === 'CANCELLED') statusClass = 'status-cancelled';

                let borderStyle = app.isEmergency ? 'border-left-color: var(--danger);' : '';

                const html = `
                    <div class="appointment-card" style="${borderStyle}">
                        <div class="date-box">
                            <div class="date-month">${month}</div>
                            <div class="date-day">${day}</div>
                            <div class="date-time">${time}</div>
                        </div>
                        
                        <div class="app-info">
                            <div class="doc-name">Dr. ${app.doctorName}</div>
                            <div class="doc-spec">${app.doctorSpecialization}</div>
                            <div class="clinic-loc">
                                <i class="fas fa-map-marker-alt" style="color:var(--primary)"></i> 
                                ${app.clinicName}
                            </div>
                            <div class="clinic-loc" style="margin-top: 0.3rem; font-family: monospace; font-size: 0.8rem;">
                                Booking Ref: #${app.bookingReference}
                            </div>
                            <div style="margin-top: 0.5rem; display: flex; gap: 0.5rem; flex-wrap: wrap;">
                                <span class="status-badge ${statusClass}">${app.status}</span>
                                ${app.isEmergency ? '<span class="status-badge status-emergency"><i class="fas fa-ambulance"></i> EMERGENCY</span>' : ''}
                                ${app.consultationType === 'VIDEO' ? '<span class="status-badge" style="background:#E0E7FF; color:#4F46E5"><i class="fas fa-video"></i> Video</span>' : ''}
                                ${app.consultationType === 'PHONE' ? '<span class="status-badge" style="background:#F3E8FF; color:#9333EA"><i class="fas fa-phone"></i> Phone</span>' : ''}
                            </div>
                        </div>
                        
                        <div class="action-cell">
                            <a href="appointment_details.html?id=${app.appointmentId}" class="btn-view">View Details</a>
                            ${currentTab === 'UPCOMING' && app.status !== 'CANCELLED' ? `<button onclick="window.location.href='appointment_booking.html?reschedule=true&id=${app.appointmentId}'" class="btn-reschedule">Reschedule</button>` : ''}
                            ${currentTab === 'UPCOMING' && app.status !== 'CANCELLED' ? `<button onclick="cancelAppointment(${app.appointmentId})" class="btn-cancel">Cancel</button>` : ''}
                        </div>
                    </div>
                `;
                container.innerHTML += html;
            });
        }

        async function cancelAppointment(id) {
            if (!confirm("Are you sure you want to cancel this appointment?")) return;

            try {
                const response = await fetch(`/api/appointments/${id}`, {
                    method: 'DELETE',
                    headers: authHeader()
                });

                if (response.ok) {
                    alert("Appointment cancelled successfully.");
                    fetchAppointments();
                } else {
                    alert("Failed to cancel appointment.");
                }
            } catch (err) {
                alert("Network error.");
            }
        }
