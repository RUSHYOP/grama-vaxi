# Grama-Vaxi — Requirements, Features & Tasks

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Persistence:** Room DB
- **Background work:** WorkManager (for offline-capable scheduled notifications)
- **Notifications:** NotificationManager + high-importance channel (loud sound)
- **Image storage:** Internal app storage (file path stored in Room)
- **Localization:** English + Kannada (`values/` + `values-kn/`)
- **Min SDK:** 24, **Target SDK:** 34

## Functional Requirements

### FR1 — Animal Ledger
- Register an animal with: photo, name/tag-id, species (Sheep/Goat/Cow), breed, age (months), gender, date of birth (optional), notes.
- List all animals with photo + key info.
- Tap to view full digital health card with vaccination history.
- Edit / delete an animal.

### FR2 — Vaccine Calendar
- Each species has a default vaccine schedule (e.g., FMD, PPR, Enterotoxemia, Sheep/Goat Pox).
- On registration, auto-generate "Next Shot" dates for each vaccine in the schedule.
- Mark a vaccine as "Administered" → moves to history, computes the next due date based on cycle.
- Show upcoming shots sorted by date.

### FR3 — Camp Alert
- Add upcoming Vaccination Camps (location, date, vaccines offered).
- Schedule a notification **3 days before** and **1 day before** each camp using WorkManager.
- Notification opens app to camp details.

### FR4 — Disease Alert
- Report a sick animal: choose animal, symptoms (multi-select), notes, optional photo.
- Show a "Reported to Local Vet (simulated)" confirmation with a generated reference id.
- Persist the report locally and show in a "Reports" history list.

### FR5 — Reminders
- WorkManager schedules per-vaccine reminders (3 days before due, and on due date).
- Survives device reboot via `BOOT_COMPLETED` re-scheduling.
- Loud, high-priority notification channel.

### FR6 — Localization
- Full UI strings extracted to `strings.xml` with Kannada translations in `values-kn/strings.xml`.
- Language toggle in Settings.

## Non-Functional Requirements
- Works fully offline.
- Large touch targets, big icons, minimal text — designed for low-literacy farmers.
- Clear iconography for navigation.

## Project Structure (Android)
```
app/
 ├─ build.gradle.kts
 ├─ src/main/
 │   ├─ AndroidManifest.xml
 │   ├─ java/com/gramavaxi/app/
 │   │   ├─ GramaVaxiApp.kt              (Application class)
 │   │   ├─ MainActivity.kt
 │   │   ├─ data/
 │   │   │   ├─ db/AppDatabase.kt
 │   │   │   ├─ entity/Animal.kt
 │   │   │   ├─ entity/Vaccination.kt
 │   │   │   ├─ entity/Camp.kt
 │   │   │   ├─ entity/DiseaseReport.kt
 │   │   │   ├─ dao/AnimalDao.kt
 │   │   │   ├─ dao/VaccinationDao.kt
 │   │   │   ├─ dao/CampDao.kt
 │   │   │   └─ dao/DiseaseReportDao.kt
 │   │   ├─ repo/Repository.kt
 │   │   ├─ schedule/VaccineSchedule.kt
 │   │   ├─ notify/
 │   │   │   ├─ NotificationHelper.kt
 │   │   │   ├─ ReminderWorker.kt
 │   │   │   ├─ ReminderScheduler.kt
 │   │   │   └─ BootReceiver.kt
 │   │   └─ ui/
 │   │       ├─ theme/...
 │   │       ├─ HomeScreen.kt
 │   │       ├─ AnimalListScreen.kt
 │   │       ├─ AnimalRegisterScreen.kt
 │   │       ├─ AnimalDetailScreen.kt
 │   │       ├─ CampListScreen.kt
 │   │       ├─ CampAddScreen.kt
 │   │       ├─ DiseaseReportScreen.kt
 │   │       └─ SettingsScreen.kt
 │   └─ res/
 │       ├─ values/strings.xml
 │       ├─ values-kn/strings.xml
 │       ├─ drawable/                     (icons)
 │       └─ mipmap-*/                     (launcher icon)
build.gradle.kts (root)
settings.gradle.kts
gradle.properties
```

## Tasks (tracked in SQL)
1. **proj-setup** — Create Gradle project skeleton (root + app, Compose + Kotlin)
2. **manifest** — AndroidManifest with permissions (camera, notifications, boot)
3. **db** — Room entities, DAOs, Database class
4. **schedule** — Default vaccine schedules per species
5. **repo** — Repository wrapping DAOs
6. **notify** — NotificationHelper, channel, ReminderWorker, scheduler, BootReceiver
7. **ui-theme** — Material3 theme + large-icon style
8. **ui-home** — Home screen with 4 big icon tiles
9. **ui-animal** — List + Register + Detail screens (photo capture)
10. **ui-camp** — Camp list + add screen
11. **ui-disease** — Disease report screen + history
12. **ui-settings** — Language toggle (English / Kannada)
13. **i18n** — strings.xml + values-kn/strings.xml
14. **app-class** — Application class, channel init, glue
15. **gradle-wrapper** — Gradle wrapper files + README build instructions
16. **readme** — Top-level README with build/run instructions
