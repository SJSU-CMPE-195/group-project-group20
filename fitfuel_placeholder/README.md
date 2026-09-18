# FitFuel (placeholder name)

# Kit
- AGP 8.9.2
- Kotlin 2.1.20
- Jetpack Compose
- Gradle wrapper 8.11.1
- Alpha (app-only)

# Usage
- Import, exclude folders when prompted by Android Studio
- Let Gradle sync
- If this is your first time using Android Studio, set up an emulator (recommend using Medium Phone API 36)
- Build automatically, then run
- Cold Booting the emulator is safe, but do not wipe emulator data or uninstall the app if you want Room data to persist between runs.

# Immediate future plans (3-20-26, version 1, frame)
- Start charting out dynamic BMR calculation based on user history. Should take a pretty long while.
- Add field for suggested macro intake, protein intake
- Create calendar screen
- Comment more code for future proof


# 195B Weeks 1-2 Persistence
- Room stores each saved `DayEntry` in the on-device SQLite database.
- `FitFuelViewModel` exposes Room-backed state to every Compose screen.
- `DayEntryRepository` separates UI state from database implementation details.
- Saving a recommended meal updates the selected day through the repository.
