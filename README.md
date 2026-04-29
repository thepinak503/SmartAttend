# SmartAttend

SmartAttend is a modern Android application designed for teachers to streamline student attendance management. Built with Jetpack Compose and local data persistence, it allows educators to mark attendance, generate reports, and communicate directly with parents.

## Features

- **Attendance Tracking**: Efficiently mark students as Present or Absent for specific sessions.
- **WhatsApp Integration**: Automatically initiate WhatsApp messages to parents, specifically useful for notifying them about absentees.
- **Excel Reports**: Export comprehensive attendance logs to Excel format (.xlsx) for record-keeping and sharing.
- **Student Management**: Add new students or view the existing roster including names, roll numbers, and parent contact details.
- **Teacher Dashboard**: At-a-glance view of total student strength and quick access to core features.
- **Data Persistence**: Uses Room database for reliable local storage, ensuring data is available offline.
- **Automated Seeding**: Includes built-in data seeding for the TECA class to quickly set up the environment.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room Persistence Library
- **Architecture**: MVVM (Model-View-ViewModel)
- **Asynchronous Processing**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose
- **Reporting**: Apache POI for Excel generation
- **Material Design**: Material 3

## Project Structure

- `ui/`: Contains Compose screens and the `AttendanceViewModel`.
- `data/`: Room entities (`Student`, `AttendanceLog`), DAO, and Database configuration.
- `util/`: Utility classes such as `ExcelExporter`.

## Requirements

- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 35
- **Android Studio**: Ladybug or newer recommended
- **Build System**: Gradle with Version Catalog (libs.versions.toml)

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run the app on an emulator or physical device.
5. Use the default login:
   - **Username**: pinak
   - **Password**: pinak@123
6. Tap **"Seed Student Data"** on the dashboard to populate the initial TECA class list.
