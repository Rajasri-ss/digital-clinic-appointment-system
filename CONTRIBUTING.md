# Contributing to HealthConnect

Thank you for your interest in contributing to the Digital Clinic Appointment & Booking System!

## Code of Conduct
Please maintain a respectful, constructive, and professional environment for all contributors.

## How to Contribute

1. **Fork the Repository**
   Create a personal fork of the repository on GitHub.

2. **Clone your Fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/digital-clinic-appointment-system.git
   cd digital-clinic-appointment-system
   ```

3. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

4. **Environment Setup**
   - Copy `.env.example` to `.env` and configure your local test database.
   - Never commit `.env` or personal credentials to Git.

5. **Make Changes & Test**
   - Ensure your code compiles cleanly:
     ```bash
     ./mvnw test-compile -DskipTests
     ```
   - Follow standard Spring Boot and Java coding standards.

6. **Commit & Push**
   ```bash
   git add .
   git commit -m "feat: description of your changes"
   git push origin feature/your-feature-name
   ```

7. **Submit a Pull Request**
   Open a Pull Request describing your changes and link any related issues.
