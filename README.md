# Eclinic

Eclinic is a comprehensive healthcare management mobile application built in Kotlin, designed to streamline communication and scheduling between patients, doctors, and administrators. The app leverages Firebase services including Firestore, Firebase Storage, and Firebase Cloud Messaging (FCM) to deliver a seamless and secure user experience.

## User Roles

The application supports three distinct types of user accounts, each with tailored features and permissions:

### 1. Administrator
- Manages doctor and patient accounts.
- Verifies doctor profiles to ensure authenticity.
- Edits and updates doctors’ availability schedules.
- Oversees overall platform data and user management.
### 2. Patient
- Books appointments with doctors easily through the app.
- Views and manages upcoming appointments.
- Accesses prescribed medications directly within the app or by scanning QR codes.
- Edits their personal profile information.
- Communicates with doctors via chat, including sending and receiving files.
### 3. Doctor
- Chats with patients to provide support and guidance, including file sharing.
- Issues prescriptions for patients digitally.
- Updates their availability and working hours.
- Views the list of patients scheduled for the day.
- Cancels appointments if necessary.
- Edits their profile details and the list of services offered, including setting prices.
## Features

- Real-time chat: Secure messaging between patients and doctors, supporting text and file attachments.
- Appointment booking and management: Patients can schedule and track visits; doctors can view and manage their daily schedules.
- Digital prescriptions: Doctors can send prescriptions electronically, accessible to patients via the app or QR codes.
- Admin controls: Admin users have the tools to verify doctors, manage accounts, and update schedules.
- Push notifications: Built-in support for Firebase Cloud Messaging to deliver timely alerts for appointments, messages, and updates.
- Cloud storage: Files and prescriptions are securely stored and accessed through Firebase Storage.
