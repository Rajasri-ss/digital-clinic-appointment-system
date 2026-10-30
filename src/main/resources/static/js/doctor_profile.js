document.addEventListener('DOMContentLoaded', async () => {
            const urlParams = new URLSearchParams(window.location.search);
            const docId = urlParams.get('id');

            if (!docId) {
                alert("Doctor ID missing");
                window.location.href = "doctor_search.html";
                return;
            }

            try {
                const response = await fetch(`/api/doctors/${docId}`);
                if (response.ok) {
                    const doc = await response.json();
                    document.getElementById('docName').innerText = doc.fullName;
                    document.getElementById('docSpecialty').innerText = doc.specialization;
                    document.getElementById('docRating').innerText = doc.averageRating || 'New';
                    document.getElementById('docExp').innerText = doc.experienceYears || '0';
                    document.getElementById('docLang').innerText = doc.languagesSpoken || 'English';
                    document.getElementById('docAbout').innerText = doc.about || 'No detailed bio available for this doctor yet.';
                    document.getElementById('docFee').innerText = doc.consultationFee || '50.00';
                    document.getElementById('docImage').src = doc.profilePhoto || 'https://ui-avatars.com/api/?name=' + doc.fullName + '&background=random';
                }
            } catch (e) {
                console.error("Failed to load doctor", e);
            }
        });

        // Simple interactive mock for time slots
        document.querySelectorAll('.slot:not(.booked)').forEach(slot => {
            slot.addEventListener('click', function () {
                document.querySelectorAll('.slot').forEach(s => s.classList.remove('active'));
                this.classList.add('active');
            });
        });

        document.querySelectorAll('.date-box').forEach(box => {
            box.addEventListener('click', function () {
                document.querySelectorAll('.date-box').forEach(b => b.classList.remove('active'));
                this.classList.add('active');
            });
        });

        function bookAppointment() {
            // Placeholder for Phase 3 Booking 
            if (localStorage.getItem('userRole') !== 'PATIENT') {
                alert("Please login as a patient to book appointments.");
                window.location.href = "login.html";
                return;
            }
            window.location.href = "appointment_booking.html";
        }
