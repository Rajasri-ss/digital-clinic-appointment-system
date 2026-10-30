// Tab functionality
        function openTab(evt, tabName) {
            evt.preventDefault();
            const tabcontent = document.getElementsByClassName("tab-content");
            for (let i = 0; i < tabcontent.length; i++) {
                tabcontent[i].classList.remove("active");
            }
            const tablinks = document.getElementsByClassName("tab-link");
            for (let i = 0; i < tablinks.length; i++) {
                tablinks[i].classList.remove("active");
            }
            document.getElementById(tabName).classList.add("active");
            evt.currentTarget.classList.add("active");
        }

        function showToast(message, isError = false) {
            const toast = document.getElementById("toastMessage");
            toast.innerText = message;
            toast.style.background = isError ? "var(--danger)" : "var(--secondary)";
            toast.style.display = "block";
            setTimeout(() => { toast.style.display = "none"; }, 3000);
        }

        const getToken = () => localStorage.getItem('token');
        const authHeader = () => ({ 'Authorization': 'Bearer ' + getToken(), 'Content-Type': 'application/json' });

        document.addEventListener('DOMContentLoaded', () => {
            if (!getToken()) window.location.href = 'login.html';
            loadProfileInfo();
        });

        async function loadProfileInfo() {
            try {
                const response = await fetch('/api/patients/profile', { headers: authHeader() });
                if (response.ok) {
                    const data = await response.json();
                    document.getElementById('fullName').value = data.fullName || '';
                    document.getElementById('email').value = data.email || '';
                    document.getElementById('phone').value = data.phone || '';
                    document.getElementById('dob').value = data.dateOfBirth || '';
                    document.getElementById('gender').value = data.gender || 'Other';
                    document.getElementById('bloodGroup').value = data.bloodGroup || 'O+';
                    document.getElementById('address').value = data.address || '';
                    document.getElementById('city').value = data.city || '';
                    document.getElementById('state').value = data.state || '';
                    document.getElementById('pincode').value = data.pincode || '';

                    document.getElementById('allergies').value = data.allergies || '';
                    document.getElementById('chronicConditions').value = data.chronicConditions || '';
                    document.getElementById('medicalHistory').value = data.medicalHistory || '';

                    document.getElementById('userProfilePic').src = data.profilePhoto ||
                        `https://ui-avatars.com/api/?name=${data.fullName}&background=4F46E5&color=fff`;
                } else {
                    showToast("Failed to load profile", true);
                }
            } catch (error) {
                console.error('Error fetching profile:', error);
            }
        }

        // Handle Profile Form Submit
        document.getElementById('profileForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                fullName: document.getElementById('fullName').value,
                phone: document.getElementById('phone').value,
                dateOfBirth: document.getElementById('dob').value,
                gender: document.getElementById('gender').value,
                bloodGroup: document.getElementById('bloodGroup').value,
                address: document.getElementById('address').value,
                city: document.getElementById('city').value,
                state: document.getElementById('state').value,
                pincode: document.getElementById('pincode').value
            };

            try {
                const response = await fetch('/api/patients/profile', {
                    method: 'PUT',
                    headers: authHeader(),
                    body: JSON.stringify(payload)
                });

                if (response.ok) showToast("Profile updated successfully!");
                else showToast("Failed to update profile", true);
            } catch (err) {
                showToast("An error occurred", true);
            }
        });

        // Handle Medical Form Submit
        document.getElementById('medicalForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                allergies: document.getElementById('allergies').value,
                chronicConditions: document.getElementById('chronicConditions').value,
                medicalHistory: document.getElementById('medicalHistory').value
            };

            try {
                const response = await fetch('/api/patients/medical-history', {
                    method: 'PUT',
                    headers: authHeader(),
                    body: JSON.stringify(payload)
                });

                if (response.ok) showToast("Medical records updated successfully!");
                else showToast("Failed to update medical records", true);
            } catch (err) {
                showToast("An error occurred", true);
            }
        });
