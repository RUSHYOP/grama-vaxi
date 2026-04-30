# Grama-Vaxi 🐐💉

> **Livestock Health Alert** — A digital vaccination card for village livestock (sheep, goats, cattle).

Built for the GenAI Healthcare brief: livestock often die from outbreaks because farmers miss "Vaccination Camp" dates announced via local loudspeakers. **Grama-Vaxi** tracks the vaccination cycle of every animal and sends loud notifications **3 days before** a camp arrives.

## Features

| Feature | Notes |
|---|---|
| 🐑 **Animal Ledger** | Register animals with photo (camera), species, breed, age, gender, notes |
| 📅 **Vaccine Calendar** | Auto-generated schedule per species (PPR, FMD, HS, Pox, Deworming, etc.) — *next shot* date is computed automatically and recomputed after each dose |
| 🔔 **Camp Alerts** | Add upcoming camps; loud notifications **3 days** and **1 day** before |
| 🏥 **Disease Alert** | Report a sick animal with multi-select symptoms → simulated dispatch to local Vet with reference id |
| 🌐 **Kannada UI** | Toggle between English and ಕನ್ನಡ in Settings |
| 📴 **Offline-first** | Room database; reminders survive reboot via `BOOT_COMPLETED` rescheduling |

## Tech Stack
- **Kotlin** + **Jetpack Compose** (Material 3)
- **Room** for persistence
- **WorkManager** for offline-capable reminder scheduling
- **Coil** for image loading
- **AppCompat** for runtime locale switching (`AppCompatDelegate.setApplicationLocales`)
- **min SDK 24**, target SDK 34

## Project structure
```
app/src/main/
 ├─ AndroidManifest.xml
 ├─ java/com/gramavaxi/app/
 │   ├─ GramaVaxiApp.kt           # Application + locale apply + channel init
 │   ├─ MainActivity.kt           # Compose nav host + notification permission
 │   ├─ data/                     # Room entities, DAOs, AppDatabase
 │   ├─ schedule/                 # Per-species default vaccine schedule
 │   ├─ repo/Repository.kt        # Single repository facade
 │   ├─ notify/                   # NotificationHelper, ReminderWorker, scheduler, BootReceiver
 │   └─ ui/                       # HomeScreen + AnimalList/Register/Detail + Camp + Disease + Settings
 └─ res/
     ├─ values/strings.xml        # English
     ├─ values-kn/strings.xml     # Kannada
     ├─ drawable/, mipmap/        # Icons (vector)
     └─ xml/file_paths.xml        # FileProvider paths for camera capture
```

## How reminders work
1. When you **register an animal**, the repository inserts a row in `vaccinations` for every vaccine in the species' schedule with a computed `dueDate`.
2. For each row, `ReminderScheduler` enqueues two **WorkManager** `OneTimeWorkRequest`s:
    - one **3 days before** `dueDate`
    - one **on** `dueDate`
3. When a vaccine is marked **administered**, the next cycle row is inserted and re-scheduled.
4. For **camps**, two reminders are scheduled (3-days-before and 1-day-before).
5. On device reboot, `BootReceiver` calls `Repository.rescheduleAllReminders()` so nothing is lost.

The `gramavaxi_alerts` notification channel is **HIGH importance** with default sound + vibration → loud, even from a quiet phone.

## Build & Run

### Open in Android Studio (recommended)
1. **Android Studio Hedgehog** (or newer) → *File → Open* → select the `Grama-Vaxi/` folder.
2. Let Gradle sync. Studio will download the Gradle wrapper distribution if missing.
3. Connect a device / start an emulator (API 24+).
4. *Run ▶︎* the **app** configuration.

### Command line
```bash
# from the project root
./gradlew assembleDebug          # build debug APK -> app/build/outputs/apk/debug/
./gradlew installDebug           # install on connected device
```
> If `gradlew` script is missing on first checkout, generate the wrapper from any local Gradle install:
> ```bash
> gradle wrapper --gradle-version 8.7
> ```

## Permissions
- `POST_NOTIFICATIONS` (Android 13+) — requested at first launch
- `RECEIVE_BOOT_COMPLETED` — re-schedule reminders after reboot
- `CAMERA` — animal photo capture
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — for accurate WorkManager firing

## Success criteria mapping
| Criterion | Where |
|---|---|
| Reminders trigger even if app not opened for days | `ReminderWorker` + `BootReceiver` |
| Register form simple & visual | `AnimalRegisterScreen` — big chips, large camera tile, minimal text |
| UI supports Kannada | `values-kn/strings.xml` + `SettingsScreen` toggle |

## Files of interest
- [PROBLEM_STATEMENT.md](PROBLEM_STATEMENT.md) — original brief
- [REQUIREMENTS.md](REQUIREMENTS.md) — feature/task breakdown
