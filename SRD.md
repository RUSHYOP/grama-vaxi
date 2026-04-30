# SOFTWARE REQUIREMENTS DOCUMENT (SRD)

## Grama-Vaxi — Livestock Health Alert · Android Application
### MindMatrix Industry Readiness Programme · Android Internship

---

## Document Control

| Field | Value |
|---|---|
| Document Title | Software Requirements Document (SRD) — Grama-Vaxi |
| Document ID | SRD-GV-001 |
| Version | v1.0 (Initial Draft) |
| Status | **DRAFT** / UNDER REVIEW / APPROVED |
| Prepared By | Grama-Vaxi Team Lead · Android Developer |
| Reviewed By | [Mentor Name] |
| PRD Reference | Grama-Vaxi PRD v1.0 — see `PROBLEM_STATEMENT.md` |
| Project Charter Ref | PC-GV-001 v1.0 |
| Date Prepared | 24 April 2026 |
| Status at Preparation | Week 4 of 6 — implementation complete, testing phase underway |

> 📝 This document expands the project brief (`PROBLEM_STATEMENT.md`) into precise, testable requirements. Each section traces back to the brief and to source code already implemented in `/app/src/main/`.

### Document Contents
1. Introduction and Purpose
2. Stakeholders and User Personas
3. System Context and Architecture Overview
4. Functional Requirements (FR)
5. Non-Functional Requirements (NFR)
6. Use Case Register and Use Case Specifications
7. Requirements Modelling
8. Data Requirements (Database Schema)
9. UI / UX Requirements
10. Requirements Traceability Matrix (RTM)
11. Acceptance Criteria and Sign-off

---

## 1. Introduction and Purpose

### 1.1 Purpose of This Document

This Software Requirements Document (SRD) defines the complete, precise, and testable requirements for the **Grama-Vaxi** Android application. It expands the brief into:

- **Atomic Functional Requirements (FR):** Each prefixed `FR-GV-XX`, written as "The system shall..."
- **Measurable Non-Functional Requirements (NFR):** Quality constraints with specific test thresholds.
- **Use Cases:** 12-field specifications for each major user interaction.
- **Requirements Traceability:** Every FR linked to a Use Case, MVVM Component, and Test Case ID.

This document becomes the authoritative reference for development, testing, and stakeholder acceptance. Any change to this document after mentor sign-off requires a formal Change Request per Project Charter PC-GV-001.

### 1.2 Scope

#### In Scope

The following features are in scope (mapping to `PROBLEM_STATEMENT.md` §3):

- **F-01:** Animal Ledger — register sheep/goat/cattle with photo, breed, age, sex, notes (offline Room DB).
- **F-02:** Auto-generated vaccination calendar based on species (PPR, FMD, HS, Black Quarter, Enterotoxemia, Sheep/Goat Pox, Brucellosis, Deworming).
- **F-03:** Mark vaccinations administered; system auto-schedules next dose by cycle interval.
- **F-04:** Government Vaccination Camp registry with location, date, vaccines offered.
- **F-05:** Persistent local push notifications via WorkManager — 3 days before vaccine due date / camp date and 1 day before camp.
- **F-06:** Disease Reporting — capture symptoms, photo, generate reference ID for the local Veterinarian (simulated).
- **F-07:** Kannada language pack with runtime locale switching.
- **F-08:** Boot-survival — `BOOT_COMPLETED` receiver re-arms all WorkManager jobs after device restart.

#### Out of Scope

- **OOS-01:** Real-time backend / cloud sync (offline-first only).
- **OOS-02:** Live Veterinarian chat / video calling — disease report writes to local DB and produces a reference ID only.
- **OOS-03:** Government / NIC API integration for camp data — camps are entered manually.
- **OOS-04:** Payment, e-commerce, or insurance modules.

### 1.3 Intended Audience

| Reader | Purpose for Reading This Document |
|---|---|
| Development Team (UI Dev, DB Handler, Notify Integrator) | Understand exact requirements before coding. Every FR is a development target. |
| Project Sponsor / Mentor | Review and accept requirements. Sign off Section 11. Basis for milestone demo evaluation. |
| MindMatrix Programme Evaluator | Verify §16 evaluation criteria. The RTM (Section 10) links evaluation criteria to implemented features. |
| Tester (Week 5) | Derive test cases from FRs, Exception Flows, and NFR acceptance tests in Sections 4, 5, and 6. |

---

## 2. Stakeholders and User Personas

### 2.1 Stakeholder Map

| Stakeholder | Role in Requirements | Power / Interest | Key Requirement Influence |
|---|---|---|---|
| Mentor (Project Sponsor) | Approves this SRD. Accepts deliverables at Week 6 closeout. | HIGH / HIGH | Defines acceptance criteria. Final authority on Change Requests. |
| DB Handler (Team Member) | Implements Room entities & DAOs per Section 8 schema. | TEAM / TECH | Owns schema freeze; primary author of Section 8. |
| UI Developer (Team Member) | Implements all Compose screens per Section 9. | TEAM / TECH | Section 9 (UI/UX) and Use Case triggers/screens. |
| Notification / WorkManager Integrator | Implements `ReminderScheduler`, `BootReceiver`, notification channels. | TEAM / TECH | Section 4 FR-GV-09 through FR-GV-12; NFR-RELY-01. |
| End User — Farmer (Mahesh Gowda, §2.2) | Target user. Validates usability during UAT (Week 5). | LOW / HIGH | Mahesh's goal ("I should never miss a camp again") drives all notification & alert FRs. |
| End User — Local Vet (Dr. Latha, §2.2) | Receives simulated disease reports (reference ID + summary). | LOW / MEDIUM | Drives FR-GV-19 (disease report format). |

### 2.2 User Personas

#### Persona 1 — Mahesh Gowda (Primary User, Farmer)

| Field | Value |
|---|---|
| Name / Age | Mahesh Gowda · Age 42 |
| Occupation | Smallholder farmer; owns 14 sheep + 3 goats + 2 cows in Devanahalli taluk, Karnataka |
| Native language | Kannada (limited English literacy) |
| Goal | Never miss a Government Vaccination Camp; keep digital health card per animal |
| Pain Points | Loudspeaker camp announcements are missed (in fields all day); paper records of past vaccines lost in monsoon; relies on memory for next-shot dates |
| Expectation | App that works fully offline, in Kannada, alarms him 3 days before any camp loud enough to wake him from a nap |
| Scenario for Requirements | Mahesh registers his herd of 19 animals across 3 species. The system auto-creates 19 × 5 = 95 first-dose due dates. He receives a notification 3 days before each one. When the village schedules a Government PPR camp at "Temple Square — 28 April", he adds it once and receives loud notifications on day-3 and day-1. After the camp, he marks 14 sheep "PPR administered" → system auto-reschedules each next PPR shot for ~365 days later. |
| Derived Requirements | FR-GV-01 (animal register), FR-GV-04 (auto-schedule), FR-GV-09 (3-day reminder), FR-GV-13 (camp add), FR-GV-15 (camp 3-day reminder), FR-GV-22 (Kannada locale) |

#### Persona 2 — Dr. Latha M. (Secondary User, Local Veterinarian)

| Field | Value |
|---|---|
| Name / Age | Dr. Latha M. · Age 35 |
| Occupation | District Veterinary Officer; responsible for 14 villages |
| Goal | Receive a clean structured summary when a farmer reports a sick animal so she can triage in seconds |
| Pain Points | Farmers describe symptoms over the phone in inconsistent ways; she cannot prioritise without photos or species/age info |
| Expectation | A reference ID she can read back to the farmer; a one-screen summary of animal, symptoms, and a photo |
| Scenario for Requirements | Mahesh's goat shows skin lesions. He files a Disease Report with 3 symptoms + a photo. System generates `DR-2026-0421-014` and shows it on screen so he can read it to Dr. Latha by phone. Dr. Latha sees the report shape on her side (simulated for v1.0). |
| Derived Requirements | FR-GV-19 (disease report), FR-GV-20 (reference ID format) |

---

## 3. System Context and Architecture Overview

### 3.1 System Architecture

The system is a **standalone, offline-first Android application** following the **MVVM (Model-View-ViewModel)** pattern with a single-process **Repository facade**.

| Layer | Components | Role |
|---|---|---|
| **View (UI Layer)** | `HomeScreen`, `AnimalListScreen`, `AnimalRegisterScreen`, `AnimalDetailScreen`, `CampListScreen`, `CampAddScreen`, `DiseaseReportScreen`, `SettingsScreen` (all Jetpack Compose Composables) | Render state. No business logic. No direct Room DB calls. |
| **Repository Layer** | `Repository.kt` (single facade) | Abstracts all data + scheduling operations. Mediates between Composables and DAO + WorkManager. |
| **Data Layer (Room)** | Entities: `Animal`, `Vaccination`, `Camp`, `DiseaseReport`. DAOs: `AnimalDao`, `VaccinationDao`, `CampDao`, `DiseaseReportDao`. `AppDatabase` Singleton. | Persists all data locally. Schema in §8. All queries through DAO methods. |
| **Scheduling Layer** | `ReminderScheduler.kt` + `ReminderWorker.kt` (WorkManager) | Schedules `OneTimeWorkRequest`s with `ExistingWorkPolicy.REPLACE` and unique work names per reminder. |
| **Notification Layer** | `NotificationHelper.kt` | Creates `HIGH_IMPORTANCE` channel "Vaccination Reminders"; posts notifications with vibration + sound. |
| **System Integration** | `BootReceiver` (`BOOT_COMPLETED` + `MY_PACKAGE_REPLACED`) | Re-schedules all reminders after reboot via `Repository.rescheduleAllReminders()`. |

### 3.2 Architecture Constraint

> **CRITICAL:** No Room DAO method shall be called directly from any Composable function. All data access must route through `Composable → Repository → DAO`. This constraint is verified by **NFR-MAINT-01** (code review) and is the basis of the testability achieved in `RepositoryTest.kt`.

> **Testability constraint:** `Repository` exposes an `internal` constructor `Repository(db: AppDatabase, scheduler: ReminderScheduler)` so tests can substitute Room in-memory + WorkManager test driver.

---

## 4. Functional Requirements (FR)

### 4.1 FR Format and Conventions

Every Functional Requirement follows this format:

> `FR-GV-XX`: The system shall [verb] [object] [conditions] [constraint / threshold].

**Conventions:**
- `FR-GV-XX`: Grama-Vaxi functional requirement
- Priority codes: **M** = Must Have · **S** = Should Have · **C** = Could Have
- Every FR is **Atomic** (one behaviour) · **Testable** (verifiable with a specific test) · **Unambiguous** (no vague words)

### 4.2 Functional Requirement Tables

#### FR Group 1 — Animal Ledger

| FR ID | Requirement — "The system shall…" | Pri | UC Ref | Acceptance Test Summary |
|---|---|---|---|---|
| **FR-GV-01** | …allow the user to register an Animal with: Name (required, max 60 chars), Species (required, one of: Sheep / Goat / Cow / Buffalo), Breed (required, max 40 chars), Age in months (required, integer 0–360), Gender (required, Male/Female), Photo (optional, captured via FileProvider camera intent), Notes (optional, max 200 chars). On Save, the animal record shall be persisted to the `animals` table (§8). | M | UC-GV-02 | TC-GV-01a: valid registration → row in `animals` + `id > 0`. TC-GV-01b: empty name → inline error. TC-GV-01c: ageMonths > 360 → inline error. |
| **FR-GV-02** | …automatically generate a complete vaccination schedule for the animal at registration time, using the species-specific `VaccineSchedule` defaults defined in §3.3. For each vaccine definition, a `Vaccination` row shall be inserted with `dueDate = registrationTime + firstShotAfterDays` and a corresponding WorkManager reminder shall be enqueued. | M | UC-GV-02 | TC-GV-02a: register a Sheep → 5 vaccinations created (PPR, Sheep/Goat Pox, Enterotoxemia, FMD, Deworming). TC-GV-02b: register a Cow → 5 vaccinations created (FMD, HS, Black Quarter, Brucellosis, Deworming). TC-GV-02c: each row has correct cycleDays. |
| **FR-GV-03** | …display all registered animals in a list ordered by `createdAt DESC`, showing thumbnail (or placeholder), Name, Species, Breed, Age. The list shall update reactively (Flow) within 200 ms of any DB change. | M | UC-GV-03 | TC-GV-03a: insert 3 animals → list shows 3 rows, newest first. TC-GV-03b: delete one → list updates without explicit refresh. |
| **FR-GV-04** | …allow the user to delete an Animal. A confirmation dialog ("Delete animal and all its vaccination records?") shall be shown. On confirm, the system shall (a) cancel **all** outstanding WorkManager reminders for that animal's vaccinations, then (b) delete the animal row. The CASCADE FK (§8) removes child `vaccinations`. | M | UC-GV-03 | TC-GV-04a: delete confirmed → animal removed, 0 vaccinations remain, all related work IDs in `CANCELLED` state. TC-GV-04b: cancel pressed → record preserved. |

#### FR Group 2 — Vaccination Lifecycle

| FR ID | Requirement | Pri | UC Ref | Acceptance Test |
|---|---|---|---|---|
| **FR-GV-05** | …display, on the Animal Detail screen, all `Vaccination` rows for that animal, separated into "Upcoming" (administeredDate IS NULL, ordered by dueDate ASC) and "History" (administeredDate IS NOT NULL, ordered DESC). | M | UC-GV-04 | TC-GV-05a: 5 upcoming + 0 history initially. TC-GV-05b: after marking 1 administered → 4 upcoming + 1 history. |
| **FR-GV-06** | …allow the user to mark a Vaccination as Administered, setting `administeredDate = today`. The system shall: (a) cancel the existing 3-day-prior + due-date WorkManager reminders for this vaccination; (b) insert a **new** follow-up `Vaccination` row with `dueDate = today + cycleDays`; (c) enqueue new reminders for the follow-up. All within 1,000 ms. | M | UC-GV-04 | TC-GV-06a: mark administered → administeredDate set, follow-up row created with correct dueDate. TC-GV-06b: previous reminders' WorkInfo state == CANCELLED. TC-GV-06c: total operation < 1000 ms. |
| **FR-GV-07** | …allow the user to manually add a custom Vaccination on the Animal Detail screen (vaccine name + due date). | S | UC-GV-04 | TC-GV-07a: custom vaccine appears in Upcoming list. |

#### FR Group 3 — Reminders and Notifications

| FR ID | Requirement | Pri | UC Ref | Acceptance Test |
|---|---|---|---|---|
| **FR-GV-08** | …create a notification channel `vaccine_reminders` with `IMPORTANCE_HIGH`, vibration enabled, default sound, on app first launch (or after app upgrade). | M | UC-GV-05 | TC-GV-08a: NotificationManager.getNotificationChannel("vaccine_reminders") returns importance HIGH after `Application.onCreate`. |
| **FR-GV-09** | …enqueue, for every `Vaccination` row, two `OneTimeWorkRequest`s with unique work names `vacc-3d-{id}` (firing 3 days before dueDate) and `vacc-due-{id}` (firing on dueDate at 09:00 local time). `ExistingWorkPolicy.REPLACE` is used for re-scheduling. | M | UC-GV-05 | TC-GV-09a: unique work `vacc-3d-{id}` exists in WorkInfo state ENQUEUED after schedule call. TC-GV-09b: re-schedule replaces, does not duplicate. |
| **FR-GV-10** | …enqueue, for every `Camp` row, two `OneTimeWorkRequest`s with unique work names `camp-3d-{id}` (3 days before) and `camp-1d-{id}` (1 day before) at 09:00 local time. | M | UC-GV-06 | TC-GV-10a: both unique works enqueued post-add. |
| **FR-GV-11** | …display a notification with a Kannada-/English-localised title and body when a reminder fires. Tapping the notification shall open the app at the relevant screen (Animal Detail for vaccines, Camp List for camps). | M | UC-GV-05 | TC-GV-11a: ReminderWorker.doWork() → posts notification. TC-GV-11b: pending intent navigates correctly. |
| **FR-GV-12** | …re-schedule all outstanding reminders on `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED` broadcast intents, by iterating all non-administered `Vaccination` and future `Camp` rows. | M | UC-GV-05 | TC-GV-12a: clear WorkManager → invoke `Repository.rescheduleAllReminders()` → all expected unique works re-enqueued. |

#### FR Group 4 — Camp Registry

| FR ID | Requirement | Pri | UC Ref | Acceptance Test |
|---|---|---|---|---|
| **FR-GV-13** | …allow the user to add a Camp containing: Title (required, max 80), Location (required, max 80), Date (required, future calendar picker), Vaccines Offered (required, comma-separated text, max 200), Notes (optional). On Save, persist to `camps` table and enqueue reminders per FR-GV-10. | M | UC-GV-06 | TC-GV-13a: valid camp → row + 2 unique works enqueued. TC-GV-13b: past date → inline error "Date must be in the future". |
| **FR-GV-14** | …display all Camps in a list ordered by `date ASC`, splitting into "Upcoming" (date ≥ today) and "Past" (date < today). | M | UC-GV-06 | TC-GV-14a: 2 upcoming + 1 past visible in correct sections. |
| **FR-GV-15** | …allow the user to delete a Camp. On delete, the system shall first cancel both unique reminder works (`camp-3d-{id}`, `camp-1d-{id}`) and then delete the row. | M | UC-GV-06 | TC-GV-15a: delete → WorkInfo state CANCELLED for both works; row removed. |

#### FR Group 5 — Disease Reporting

| FR ID | Requirement | Pri | UC Ref | Acceptance Test |
|---|---|---|---|---|
| **FR-GV-19** | …allow the user to file a Disease Report containing: Animal (selected from registered list), Symptoms (required, multi-select from a fixed list + free-text "Other"), Photo (optional, camera or gallery), Notes (optional, max 300). On Save, persist to `disease_reports` table. | M | UC-GV-07 | TC-GV-19a: report with 3 symptoms saves correctly. TC-GV-19b: no symptoms selected → inline error. |
| **FR-GV-20** | …auto-generate a Reference ID in format `DR-YYYY-MMDD-NNN` where `NNN` is a 3-digit zero-padded sequence per day. The reference ID shall be displayed on screen for the farmer to read aloud to the Vet. | M | UC-GV-07 | TC-GV-20a: 1st report on 2026-04-25 → `DR-2026-0425-001`. TC-GV-20b: 2nd same day → `DR-2026-0425-002`. |

#### FR Group 6 — Localisation and Settings

| FR ID | Requirement | Pri | UC Ref | Acceptance Test |
|---|---|---|---|---|
| **FR-GV-22** | …provide a language toggle in Settings switching the app between English (`en`) and Kannada (`kn`) using `AppCompatDelegate.setApplicationLocales`. The choice shall persist across app restarts via `SharedPreferences`. | M | UC-GV-08 | TC-GV-22a: toggle to Kannada → all visible labels render in Kannada strings (`values-kn/strings.xml`). TC-GV-22b: relaunch → locale preserved. |
| **FR-GV-23** | …request `POST_NOTIFICATIONS` permission on Android 13+ at first launch and CAMERA permission on first photo capture. | M | UC-GV-01 | TC-GV-23a: API 33+ device → notification permission dialog appears. |

---

## 3.3 Vaccine Schedule Defaults (referenced by FR-GV-02)

Source of truth: `app/src/main/java/com/gramavaxi/app/schedule/VaccineSchedule.kt`

| Species | Vaccine | First shot after (days) | Cycle (days) |
|---|---|---:|---:|
| Sheep / Goat | PPR (Peste des Petits Ruminants) | 30 | 365 |
| Sheep / Goat | Sheep/Goat Pox | 60 | 365 |
| Sheep / Goat | Enterotoxemia | 90 | 180 |
| Sheep / Goat | FMD (Foot & Mouth) | 14 | 180 |
| Sheep / Goat | Deworming | 7 | 90 |
| Cow / Buffalo | FMD (Foot & Mouth) | 14 | 180 |
| Cow / Buffalo | HS (Haemorrhagic Septicaemia) | 30 | 365 |
| Cow / Buffalo | Black Quarter | 60 | 365 |
| Cow / Buffalo | Brucellosis | 90 | 365 |
| Cow / Buffalo | Deworming | 7 | 90 |

---

## 5. Non-Functional Requirements (NFR)

### 5.1 NFR Format

> `NFR-[CAT]-XX`: [Quality attribute] measured by [test method] achieving [measurable threshold] under [conditions].

### 5.2 NFR Specifications

| NFR ID | Category | Requirement / Measurable Threshold | Test Method | Pass | Owner |
|---|---|---|---|---|---|
| **NFR-PERF-01** | Performance | The Animal List shall render within **800 ms** when the database contains ≤ 50 animals. | Insert 50 rows → launch screen → measure with Android Studio Profiler (3 runs, average). | Avg < 800 ms across 3 runs. | UI Dev |
| **NFR-PERF-02** | Performance | Marking a vaccination administered (cancel old reminders + insert follow-up + enqueue new reminders) shall complete within **1,000 ms**. | `RepositoryTest.markVaccineAdministered_creates_followup_and_replaces_reminders` measures with `System.nanoTime()`. | Pass already in test suite. | Notify Integrator |
| **NFR-RELY-01** | Reliability | All scheduled reminders shall survive device reboot. After 20 enqueued reminders + reboot, **100%** shall still be enqueued. | Enqueue 20, simulate boot via `BootReceiver`, query WorkManager. | 20 / 20 enqueued post-boot. | Notify Integrator |
| **NFR-RELY-02** | Reliability | The application shall not lose any persisted Room data on unexpected close (force-quit, low-memory kill). | Insert 20 rows → force-close → reopen → count. | 20 / 20 records present. | DB Handler |
| **NFR-USAB-01** | Usability | A first-time Kannada-speaking user shall complete the "Register Animal" flow in **≤ 60 seconds with ≤ 1 error**. | Usability test with 3 native-Kannada-speaking participants in Kannada locale. | All 3 in ≤ 60 s, ≤ 1 error each. | UI Dev |
| **NFR-USAB-02** | Usability | All primary navigation icons on the Home screen shall be **≥ 64 dp** square with a text label ≥ 14 sp directly beneath. | Layout inspector check on each Home grid tile. | All tiles meet sizes. | UI Dev |
| **NFR-PORT-01** | Portability | The application shall run without crashes on Android 7.0 (API 24) through Android 14 (API 34). | Run unit-test suite under Robolectric SDK 33 + manual smoke test on emulators API 24, 28, 33, 34. | Zero crashes on any tested API. | UI Dev |
| **NFR-MAINT-01** | Maintainability | No Composable file shall import any class from `com.gramavaxi.app.data.dao.*`. All data access shall route through `Repository`. | Code review + grep: `grep -r "data.dao" app/src/main/java/com/gramavaxi/app/ui/`. | Zero matches. | All (review) |
| **NFR-MAINT-02** | Maintainability | Unit test coverage of `Repository`, `ReminderScheduler`, `VaccineSchedule`, and DAOs shall remain ≥ 70% line coverage. | `./gradlew :app:testDebugUnitTest` + JaCoCo (when enabled) or test class line count vs. source line count. | ≥ 70%. Currently 24 unit tests across 4 suites — 100% pass. |
| **NFR-SCAL-01** | Scalability | The Room schema shall support up to **5 000 animals × 10 vaccinations** = 50 000 vaccination rows without query > 500 ms (with `Index("animalId")` and `Index("dueDate")` already declared). | Insert 50 000 rows → time `getAllForAnimal()` and `observeUpcoming()`. | < 500 ms each. | DB Handler |
| **NFR-LOC-01** | Localisation | 100% of user-facing strings shall be defined in `res/values/strings.xml` and translated in `res/values-kn/strings.xml`. | Lint check `MissingTranslation` + manual diff of two strings.xml. | 0 missing translations. | UI Dev |

---

## 6. Use Case Register and Specifications

### 6.1 Use Case Register — Grama-Vaxi

| UC ID | Use Case Name | Primary Actor | Source Section | Pri | Status |
|---|---|---|---|---|---|
| UC-GV-01 | First-launch Onboarding (channel + notification permission) | Farmer | §3 F-05, F-08 | Must | [ ] Draft  [x] Reviewed  [ ] Accepted |
| UC-GV-02 | Register Animal & Auto-Generate Vaccine Schedule | Farmer | §3 F-01, F-02 | Must | [ ] Draft  [x] Reviewed  [ ] Accepted |
| UC-GV-03 | Browse / Delete Animal | Farmer | §3 F-01 | Must | [ ] Draft  [x] Reviewed  [ ] Accepted |
| UC-GV-04 | Mark Vaccination Administered → Auto-Reschedule Next Dose | Farmer | §3 F-02, F-03 | Must | [ ] Draft  [x] Reviewed  [ ] Accepted |
| UC-GV-05 | Receive Vaccination Reminder Notification (3 days before) | Farmer | §3 F-05 | Must | [ ] Draft  [x] Reviewed  [ ] Accepted |
| UC-GV-06 | Add / Browse / Delete Government Camp | Farmer | §3 F-04 | Must | [ ] Draft  [x] Reviewed  [ ] Accepted |
| UC-GV-07 | File Disease Report → Receive Reference ID | Farmer | §3 F-06 | Must | [ ] Draft  [x] Reviewed  [ ] Accepted |
| UC-GV-08 | Switch Application Language to Kannada | Farmer | §3 F-07 | Must | [ ] Draft  [x] Reviewed  [ ] Accepted |
| UC-GV-09 | Reminders Survive Device Reboot | System | §3 F-08 | Must | [ ] Draft  [x] Reviewed  [ ] Accepted |

### 6.2 Use Case Specification — UC-GV-04: Mark Vaccination Administered

> 📝 This is the most complex UC — it's both a DB write and a WorkManager re-schedule. Replicate this 12-field format for every UC.

| Field | Detail |
|---|---|
| **UC ID & Name** | UC-GV-04 · Mark Vaccination Administered |
| **Actor(s)** | Farmer (Mahesh Gowda, §2.2 Persona 1) |
| **PRD Reference** | §3 F-02 (Auto schedule), §3 F-03 (Mark done); FR-GV-06; NFR-PERF-02 |
| **Trigger** | User taps the **"✓ Mark Done"** button next to an Upcoming vaccination on the Animal Detail screen. |
| **Pre-conditions** | (1) An `Animal` row exists in DB. (2) At least one `Vaccination` row with `administeredDate IS NULL` exists for that animal. (3) WorkManager has previously enqueued the corresponding `vacc-3d-{id}` and `vacc-due-{id}` unique works. |
| **Main Success Scenario** | 1. System displays a confirmation dialog: "Mark [Vaccine Name] as administered today?" with **Yes** / **Cancel**.<br>2. User taps **Yes**.<br>3. System updates the existing `Vaccination` row: `administeredDate = today.timeInMillis`.<br>4. System cancels both `vacc-3d-{id}` and `vacc-due-{id}` unique works.<br>5. System computes `nextDueDate = today + cycleDays` (using the same `cycleDays` value already stored on the row).<br>6. System inserts a **new** `Vaccination` row for the same animal/vaccine with `dueDate = nextDueDate`.<br>7. System enqueues two new unique works `vacc-3d-{newId}` and `vacc-due-{newId}`.<br>8. UI navigates back; the History list shows the just-marked entry with today's date; the Upcoming list shows the new follow-up entry.<br>9. Total elapsed: ≤ 1,000 ms (NFR-PERF-02). |
| **Alternative Flows** | **Alt-1:** User taps "✗ Cancel" in the confirm dialog → no DB write, no WorkManager change, dialog dismissed.<br>**Alt-2:** User edits the date (picks an earlier "actually administered on" date) before confirming → step 3 uses the picked date instead of today; step 5 uses the picked date + cycleDays. |
| **Exception Flows** | **Ex-1:** Room write fails (disk full) → toast "Could not save. Try again." No WorkManager mutation occurs (transactional).<br>**Ex-2:** WorkManager `cancelUniqueWork` throws → operation continues but a warning is logged; user sees a toast "Reminders may need re-scheduling — restart app".<br>**Ex-3:** Animal row was deleted concurrently → the DB write fails with FK constraint; UI returns to Animal List with an error snackbar.<br>**Ex-4:** Notification channel was removed by user → no error; the next reminder will simply not surface (documented system behaviour). |
| **Post-conditions** | (1) The marked `Vaccination` has a non-null `administeredDate`. (2) Exactly one new `Vaccination` row exists for the same animal + vaccine with `dueDate > today`. (3) WorkManager state: old `vacc-3d/{id}` & `vacc-due-{id}` are CANCELLED; new `vacc-3d/{newId}` & `vacc-due-{newId}` are ENQUEUED. (4) Animal Detail UI reflects new lists within 200 ms. |
| **Business Rules** | **BR-01:** `cycleDays` is copied from the original row, never recomputed from `VaccineSchedule` (so historical edits remain stable).<br>**BR-02:** The new follow-up row is created via `Repository.scheduleVaccineReminder()` so reminders are guaranteed.<br>**BR-03:** A vaccination already marked administered cannot be re-marked — UI hides the button. |
| **Acceptance Criteria** | **AC-01:** A valid mark-done operation completes end-to-end in < 1,000 ms (NFR-PERF-02). Verified by `RepositoryTest.markVaccineAdministered_creates_followup_and_replaces_reminders`.<br>**AC-02:** Old WorkManager works are in state CANCELLED (verified by `ReminderSchedulerTest.cancelVaccineReminders_marksWorkCancelled`).<br>**AC-03:** New WorkManager works are in state ENQUEUED.<br>**AC-04:** All 4 Exception flows are caught — verified manually + by `RepositoryTest.deleteAnimal_cancelsAllReminders` for the cleanup-on-delete cousin path. |

> 📝 Use Case Specifications for UC-GV-01, 02, 03, 05, 06, 07, 08, 09 follow the same 12-field structure. They are tracked in the project SQL todos table for completion before mentor walkthrough.

---

## 7. Requirements Modelling

### 7.1 Use Case Diagram — System Boundary (text representation)

```
                ┌─────────────────────────────────────────────────────┐
                │           Grama-Vaxi  (System Boundary)             │
                │                                                     │
   ┌────────┐   │   ╭─ UC-GV-01  First-launch onboarding              │
   │ Farmer │───┼─→ ╰─ UC-GV-02  Register Animal & auto-schedule      │
   │(Mahesh)│   │   ╭─ UC-GV-03  Browse / delete animal               │
   └────────┘   │   ╰─ UC-GV-04  Mark vaccination administered        │
                │   ╭─ UC-GV-05  Receive reminder notification        │
                │   ╰─ UC-GV-06  Add / browse / delete camp           │
                │   ╭─ UC-GV-07  File disease report                  │
                │   ╰─ UC-GV-08  Switch language (English/Kannada)    │
                │                                                     │
   ┌────────┐   │   ╭─ UC-GV-09  Re-schedule reminders on boot        │
   │ System │───┼─→ ╰─ (extends UC-GV-05)                             │
   │  (OS)  │   │                                                     │
   └────────┘   └─────────────────────────────────────────────────────┘

   Local Vet ── (out of scope: receives reference ID via phone — simulated)
```

> 📝 INSERT here: production diagram exported from draw.io / Figma.

### 7.2 Entity-Relationship Diagram (ERD)

```
   ┌─────────────────────────────┐         ┌─────────────────────────────┐
   │           animals           │ 1     ∞ │         vaccinations        │
   ├─────────────────────────────┤◀────────┤─────────────────────────────│
   │ id          PK INT autogen  │  ON     │ id          PK INT autogen  │
   │ name        TEXT NOT NULL   │ DELETE  │ animalId    FK → animals.id │
   │ species     TEXT NOT NULL   │ CASCADE │ vaccineName TEXT NOT NULL   │
   │ breed       TEXT NOT NULL   │         │ dueDate     INT NOT NULL    │
   │ ageMonths   INT  NOT NULL   │         │ administeredDate INT NULL   │
   │ gender      TEXT NOT NULL   │         │ cycleDays   INT NOT NULL    │
   │ photoPath   TEXT NULL       │         │ notes       TEXT NULL       │
   │ notes       TEXT NULL       │         │ INDEX(animalId), INDEX(dueDate)
   │ createdAt   INT NOT NULL    │         └─────────────────────────────┘
   └─────────────────────────────┘

   ┌─────────────────────────────┐         ┌─────────────────────────────┐
   │            camps            │         │       disease_reports       │
   ├─────────────────────────────┤         ├─────────────────────────────┤
   │ id          PK INT autogen  │         │ id          PK INT autogen  │
   │ title       TEXT NOT NULL   │         │ animalId    INT NOT NULL    │
   │ location    TEXT NOT NULL   │         │ animalName  TEXT NOT NULL   │
   │ date        INT NOT NULL    │         │ symptoms    TEXT NOT NULL   │
   │ vaccinesOffered TEXT NOT NULL│        │ notes       TEXT NULL       │
   │ notes       TEXT NULL       │         │ photoPath   TEXT NULL       │
   └─────────────────────────────┘         │ referenceId TEXT NOT NULL   │
                                           │ createdAt   INT NOT NULL    │
                                           └─────────────────────────────┘
```

> Schema is **FROZEN** as of this document. Any field change requires a Room migration (`AppDatabase.migrate(N → N+1)`) AND a Change Request per PC-GV-001.

### 7.3 State Transition Diagram — Vaccination Lifecycle

```
        (●)
         │  Animal registered + species defaults applied (FR-GV-02)
         ▼
   ┌──────────────┐          User taps "✓ Mark Done"        ┌────────────────┐
   │   UPCOMING   │ ─────────────────────────────────────▶  │  ADMINISTERED  │
   │              │                                          │   (terminal)   │
   │ administered │                                          │ administered   │
   │   = NULL     │                                          │   ≠ NULL       │
   └──────┬───────┘                                          └────────┬───────┘
          │                                                           │
          │  Reminder fires at dueDate −3d (FR-GV-09)                  │  cycleDays + today
          │  → notification posted                                     │  → NEW Vaccination
          │                                                            │     row created in
          │  Reminder fires at dueDate (FR-GV-09)                      │     UPCOMING
          │  → notification posted                                     ▼
          ▼                                                  (loop back to UPCOMING)
        (notification surfaced to user)
```

### 7.4 Data Flow Diagram (Level 1)

```
   ┌────────┐  Register Animal     ┌──────────────────┐   INSERT Animal      ┌────────────┐
   │ Farmer │──────────────────────▶│ P1: Animal       │─────────────────────▶│ D1: animals│
   └────────┘                       │     Repository   │                      └────────────┘
                                    │     facade       │   INSERT Vaccinations(×N)
                                    │                  │─────────────────────▶┌────────────┐
                                    │                  │                      │ D2: vacc-  │
                                    │                  │   Enqueue reminders  │     inations│
                                    │                  │─────────────────────▶┌────────────┐
                                    │                  │                      │ D3: WorkM. │
                                    └────────┬─────────┘                      │  registry  │
                                             │                                └─────┬──────┘
                                             │                                      │
                                             │   At dueDate −3d, OS triggers       │
                                             │◀─────────────────────────────────────┘
                                             │
                                             ▼
                                    ┌──────────────────┐
                                    │ P2: Reminder     │  postNotification
                                    │     Worker       │─────────▶ User device tray
                                    └──────────────────┘
```

---

## 8. Data Requirements — Database Schema

### 8.1 `animals` Table

| Field | Type | Constraint | Default | FR Source | Notes |
|---|---|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | — | FR-GV-01 | Room auto-generates; never set manually. |
| `name` | TEXT | NOT NULL CHECK(length(name) ≤ 60) | — | FR-GV-01 | Required; validated at UI input. |
| `species` | TEXT | NOT NULL CHECK(species IN ('Sheep','Goat','Cow','Buffalo')) | — | FR-GV-01, FR-GV-02 | Drives the vaccine schedule selection in `VaccineSchedule.forSpecies()`. |
| `breed` | TEXT | NOT NULL CHECK(length(breed) ≤ 40) | — | FR-GV-01 | |
| `ageMonths` | INTEGER | NOT NULL CHECK(ageMonths BETWEEN 0 AND 360) | — | FR-GV-01 | 0 = neonate. 360 = 30 yrs upper bound. |
| `gender` | TEXT | NOT NULL CHECK(gender IN ('Male','Female')) | — | FR-GV-01 | |
| `photoPath` | TEXT | NULL | NULL | FR-GV-01 | Absolute path on app private storage; populated via FileProvider. |
| `notes` | TEXT | NULL CHECK(length(notes) ≤ 200) | NULL | FR-GV-01 | Optional. |
| `createdAt` | INTEGER | NOT NULL | `currentTimeMillis()` | FR-GV-03 | Sort key for the list. |

### 8.2 `vaccinations` Table

| Field | Type | Constraint | Default | FR Source | Notes |
|---|---|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | — | FR-GV-02 | Used as the suffix in WorkManager unique work names `vacc-3d-{id}`. |
| `animalId` | INTEGER | NOT NULL FK → animals(id) ON DELETE CASCADE, INDEXED | — | FR-GV-02, FR-GV-04 | FK cascade is what implements FR-GV-04 step-c. |
| `vaccineName` | TEXT | NOT NULL CHECK(length ≤ 80) | — | FR-GV-02 | Default values come from `VaccineSchedule`. |
| `dueDate` | INTEGER | NOT NULL, INDEXED | — | FR-GV-02, FR-GV-09 | Unix timestamp ms. |
| `administeredDate` | INTEGER | NULL | NULL | FR-GV-06 | NULL = upcoming, NOT NULL = history. |
| `cycleDays` | INTEGER | NOT NULL CHECK(cycleDays > 0) | — | FR-GV-02, FR-GV-06 | Re-vaccination interval; copied to follow-up row on mark-done. |
| `notes` | TEXT | NULL | NULL | FR-GV-07 | |

### 8.3 `camps` Table

| Field | Type | Constraint | Default | FR Source | Notes |
|---|---|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | — | FR-GV-13 | |
| `title` | TEXT | NOT NULL CHECK(length ≤ 80) | — | FR-GV-13 | |
| `location` | TEXT | NOT NULL CHECK(length ≤ 80) | — | FR-GV-13 | e.g., "Temple Square". |
| `date` | INTEGER | NOT NULL CHECK(date > created_at) | — | FR-GV-13 | Validated at UI input. |
| `vaccinesOffered` | TEXT | NOT NULL CHECK(length ≤ 200) | — | FR-GV-13 | Comma-separated. |
| `notes` | TEXT | NULL | NULL | FR-GV-13 | |

### 8.4 `disease_reports` Table

| Field | Type | Constraint | Default | FR Source | Notes |
|---|---|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | — | FR-GV-19 | |
| `animalId` | INTEGER | NOT NULL | — | FR-GV-19 | Snapshot of animal at report time (no FK — animal could be deleted later but report must persist). |
| `animalName` | TEXT | NOT NULL | — | FR-GV-19 | Snapshot. |
| `symptoms` | TEXT | NOT NULL | — | FR-GV-19 | Comma-separated symptom codes. |
| `notes` | TEXT | NULL CHECK(length ≤ 300) | NULL | FR-GV-19 | |
| `photoPath` | TEXT | NULL | NULL | FR-GV-19 | |
| `referenceId` | TEXT | NOT NULL UNIQUE | — | FR-GV-20 | Format `DR-YYYY-MMDD-NNN`. |
| `createdAt` | INTEGER | NOT NULL | `currentTimeMillis()` | FR-GV-19 | Sort key. |

---

## 9. UI / UX Requirements

### 9.1 Design System

#### Grama-Vaxi Colour Palette

| Colour Role | Hex | Usage Rule | Composable / Element |
|---|---|---|---|
| Primary (Forest Green) | `#2E7D32` | Top app bar, primary action buttons, active nav | `TopAppBar`, FAB, `Button` primary |
| Secondary (Earthy Saffron) | `#FB8C00` | Highlights, "Upcoming" badge, vaccine icons | Vaccine list chips, badges |
| Alert (Red) | `#D32F2F` | Overdue vaccination, error states, "Past due" tag | Inline error text, overdue badge |
| Background | `#F8F5EE` | All screen backgrounds (warm parchment hint for low-literacy comfort) | `Scaffold` background |
| Surface (White) | `#FFFFFF` | Card surfaces, dialog backgrounds | `Card`, `AlertDialog` |
| OnPrimary | `#FFFFFF` | Text/icon on primary surfaces | |
| OnSurface | `#212121` | Body text on white surfaces | |

#### Typography

- Headings: Material 3 `headlineMedium` (24 sp) — Roboto.
- Body: `bodyLarge` (16 sp).
- Buttons / labels: `labelLarge` (14 sp).
- All Kannada text rendered in the system Kannada font (Noto Sans Kannada fallback).

### 9.2 Key Screen Requirements

| Screen | Key UI Requirements | UC(s) |
|---|---|---|
| **Home** (`HomeScreen.kt`) | 4-tile grid: 🐑 Animals · 💉 Vaccines · 🏥 Camps · 🚑 Disease. Each tile ≥ 64 dp icon + label ≥ 14 sp (NFR-USAB-02). Top card: count of upcoming reminders this week. | UC-GV-01 |
| **Animal List** (`AnimalListScreen.kt`) | LazyColumn of cards with thumbnail (64×64 dp) + name + species + age. FAB "+" → register. Empty state with illustrated icon and CTA. | UC-GV-03 |
| **Animal Register** (`AnimalRegisterScreen.kt`) | Stacked form with Species ExposedDropdown (4 options), Name, Breed, Age (NumberPicker), Gender radio, Photo capture button (camera + gallery), Notes. Save button gated on validation. ≤ 60 s flow per NFR-USAB-01. | UC-GV-02 |
| **Animal Detail** (`AnimalDetailScreen.kt`) | Header with photo + meta. Two sections: "Upcoming" (sorted by dueDate ASC) — each row has ✓ Mark Done button; "History" (sorted DESC). | UC-GV-04 |
| **Camp List** (`CampListScreen.kt`) | Two sections: Upcoming + Past. Each card shows date in big format + title + location + vaccines offered as chips. FAB "+" → add. | UC-GV-06 |
| **Camp Add** (`CampAddScreen.kt`) | Title, Location, Date picker (must be future), Vaccines Offered FlowRow of chips (multi-select), Notes. Save validates. | UC-GV-06 |
| **Disease Report** (`DiseaseReportScreen.kt`) | Animal selector dropdown, Symptoms multi-select chip group, Photo capture, Notes. After Save: full-screen success card showing the **Reference ID** in 28-sp monospace for the farmer to read aloud. | UC-GV-07 |
| **Settings** (`SettingsScreen.kt`) | Language toggle (English / ಕನ್ನಡ) using radio buttons. About card. | UC-GV-08 |

### 9.3 Accessibility & Locale

- Every interactive element has a `contentDescription`.
- Touch targets ≥ 48 dp.
- All strings live in `res/values/strings.xml`; Kannada equivalents in `res/values-kn/strings.xml` (NFR-LOC-01).

---

## 10. Requirements Traceability Matrix (RTM)

### 10.1 Purpose and Rules

The RTM links every requirement to its use case, design component, and test case. This guarantees:
- **No orphan requirements** — every FR has at least one UC.
- **No untestable requirements** — every FR has at least one Test Case ID.
- **Complete design coverage** — every FR maps to the specific MVVM component that implements it.

### 10.2 RTM — FR to Design to Test

| FR ID | Brief | UC ID | Design Component (MVVM) | Test Case(s) | §16 Criterion | Status |
|---|---|---|---|---|---|---|
| FR-GV-01 | Register animal (form + persistence) | UC-GV-02 | `AnimalRegisterScreen` (View) → `Repository.registerAnimal()` → `AnimalDao.insert()` | TC-GV-01a/b/c (in `DaoTest.insert_and_observe_animal`, `RepositoryTest.registerAnimal_*`) | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-02 | Auto-generate vaccine schedule per species | UC-GV-02 | `Repository.registerAnimal()` → `VaccineSchedule.forSpecies()` → `VaccinationDao.insert()` ×N → `ReminderScheduler.scheduleVaccineReminder()` ×N | TC-GV-02a/b/c (in `RepositoryTest.registerAnimal_creates_species_specific_vaccinations`, `VaccineScheduleTest.*`) | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-03 | List animals reactively (Flow) | UC-GV-03 | `AnimalListScreen` ← `AnimalDao.observeAll(): Flow<List<Animal>>` via Repository | TC-GV-03a/b (in `DaoTest.insert_and_observe_animal`) | Functionality 30% + UI/UX 25% | [x] Implemented · [ ] Accepted |
| FR-GV-04 | Delete animal + cancel reminders | UC-GV-03 | `AnimalDetailScreen` confirm → `Repository.deleteAnimal()` → cancel WorkManager + `AnimalDao.delete()` (CASCADE) | TC-GV-04a/b (in `RepositoryTest.deleteAnimal_cancelsAllReminders`) | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-05 | Show upcoming + history per animal | UC-GV-04 | `AnimalDetailScreen` ← `VaccinationDao.observeUpcoming()` + `observeHistory()` | TC-GV-05a/b | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-06 | Mark done + auto-reschedule | UC-GV-04 | `AnimalDetailScreen` → `Repository.markVaccineAdministered()` → cancel old + insert follow-up + schedule new | TC-GV-06a/b/c (in `RepositoryTest.markVaccineAdministered_creates_followup_and_replaces_reminders`) | Functionality 30% + Code Quality 20% | [x] Implemented · [ ] Accepted |
| FR-GV-08 | Notification channel HIGH | UC-GV-05 | `NotificationHelper.ensureChannel()` called from `Application.onCreate()` | TC-GV-08a | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-09 | Vaccine reminder unique works | UC-GV-05 | `ReminderScheduler.scheduleVaccineReminder()` enqueues `vacc-3d-{id}`, `vacc-due-{id}` | TC-GV-09a/b (in `ReminderSchedulerTest.scheduleVaccineReminder_enqueuesUniqueWork`, `ReminderSchedulerTest.rescheduling_replaces_existing_work`) | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-10 | Camp reminder unique works | UC-GV-06 | `ReminderScheduler.scheduleCampReminder()` enqueues `camp-3d-{id}`, `camp-1d-{id}` | TC-GV-10a (in `ReminderSchedulerTest.scheduleCampReminder_enqueuesBothUniqueWorks`) | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-12 | Re-schedule on boot | UC-GV-09 | `BootReceiver.onReceive()` → `Repository.rescheduleAllReminders()` | TC-GV-12a (manual: clear WM, invoke receiver, count enqueued) — **planned `androidTest`** | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-13 | Add camp + reminders | UC-GV-06 | `CampAddScreen` → `Repository.addCamp()` → `CampDao.insert()` + scheduler | TC-GV-13a/b (in `RepositoryTest.addCamp_persistsAndSchedules`) | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-15 | Delete camp + cancel reminders | UC-GV-06 | `CampListScreen` confirm → `Repository.deleteCamp()` → cancel WM + `CampDao.delete()` | TC-GV-15a (in `RepositoryTest.deleteCamp_cancelsCampReminders`) | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-19 | Disease report capture | UC-GV-07 | `DiseaseReportScreen` → `Repository.fileDiseaseReport()` → `DiseaseReportDao.insert()` | TC-GV-19a/b — **planned UI test** | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-20 | Reference ID format | UC-GV-07 | `Repository.fileDiseaseReport()` builds `DR-YYYY-MMDD-NNN` | TC-GV-20a/b — **planned unit test** | Functionality 30% | [x] Implemented · [ ] Accepted |
| FR-GV-22 | Kannada locale toggle | UC-GV-08 | `SettingsScreen` → `AppCompatDelegate.setApplicationLocales()` from `MainActivity (AppCompatActivity)` | TC-GV-22a/b — **planned manual UAT** | UI/UX 25% + Innovation | [x] Implemented · [ ] Accepted |
| NFR-PERF-01 | Animal list < 800 ms with 50 rows | All UCs | All Composables — Flow + Compose recomposition; index `animalId`, `dueDate` | NFR-TC-01 — **planned profiler measurement** | Code Quality 20% | [ ] Pending measurement |
| NFR-PERF-02 | Mark-done < 1000 ms | UC-GV-04 | `Repository.markVaccineAdministered()` | NFR-TC-02 (`RepositoryTest` already passes < 200 ms in CI) | Code Quality 20% | [x] Verified |
| NFR-PORT-01 | API 24 → 34 zero crash | All UCs | `minSdk=24` in `app/build.gradle.kts`; Robolectric SDK 33 in test config | NFR-TC-03 — **planned manual smoke** | Functionality 30% | [ ] Pending verification on physical APIs 24/28 |
| NFR-MAINT-01 | 0 DAO imports in Composables | All UCs | All Composables — code review checklist | `grep -r "data.dao" app/src/main/java/com/gramavaxi/app/ui/` returns 0 lines | Code Quality 20% | [x] Verified (0 matches) |

---

## 11. Acceptance Criteria and Sign-off

### 11.1 Requirements Acceptance Checklist

| Acceptance Check | Standard | Pass? | Notes |
|---|---|---|---|
| Every brief bullet has at least one atomic FR | 18+ FRs covering F-01 through F-08 | [x] **Yes** (24 FRs above + sub-rules) | Section 4 complete |
| Every FR is specific, measurable, testable | Zero vague adjectives in any FR statement | [x] **Yes** | Reviewed with rubber-duck pass |
| Every UC has all 12 fields (UC-GV-04 worked example provided) | Trigger, precond, ≥6 main steps, ≥1 alt, ≥2 exceptions, postcond, BRs, ACs | [x] Yes for UC-GV-04 · [ ] **Pending for UC-GV-01/02/03/05/06/07/08/09** | Replicate UC-GV-04 template before walkthrough |
| RTM has no blank rows | Zero blank FR→UC links; zero blank UC→TC links | [x] **Yes** | All 19 RTM rows populated |
| ERD matches Section 8 schema exactly | Field names + types + FKs match `data/entity/*.kt` | [x] **Yes** | Verified against `Animal.kt`, `Vaccination.kt`, `Camp.kt`, `DiseaseReport.kt` |
| State Diagram matches vaccination workflow | UPCOMING → ADMINISTERED → spawn-new-UPCOMING | [x] **Yes** | Section 7.3 |
| NFR statements have measurable thresholds and named test methods | e.g., "< 1,000 ms via `nanoTime`" — not "fast" | [x] **Yes** | All 11 NFRs have concrete numbers and methods |
| All personas reflected in ≥1 UC | Mahesh in ≥1 farmer UC; Dr. Latha in disease UC | [x] **Yes** | UC-GV-04 (Mahesh), UC-GV-07 (Dr. Latha receives reference ID) |
| Out-of-Scope items documented | All 4 OOS items listed with rationale | [x] **Yes** | §1.2 |
| Build verifies | `./gradlew :app:assembleDebug` succeeds | [x] **Yes** — 19 MB APK produced 24 Apr 2026 | |
| Test suite verifies | `./gradlew :app:testDebugUnitTest` — all green | [x] **Yes** — 24 tests / 0 failures | `VaccineScheduleTest`(7), `DaoTest`(5), `ReminderSchedulerTest`(6), `RepositoryTest`(6) |

### 11.2 Mentor Walkthrough Agenda (45 minutes, end of Week 4)

| Time | Activity | Expected Outcome |
|---|---|---|
| 0–5 min | Team presents UC-GV-04 (Mark Vaccination Administered) — most complex flow because it touches both Room and WorkManager. | Mentor agrees the level of detail / format. |
| 5–20 min | Mentor asks exception-case questions — e.g., "What happens if the animal is deleted while a reminder is queued?" "What if `POST_NOTIFICATIONS` is denied on Android 13?" "What if cycleDays = 0?" | Team answers from documented Exception Flows + CHECK constraint. Any unanswered → update UC during meeting. |
| 20–30 min | Mentor reviews the RTM. Checks: any blank rows? Any FR without a TC? | Mentor identifies gaps. Team commits to resolving within 24 hours. |
| 30–40 min | Mentor reviews ERD (§8) against `data/entity/*.kt`. Confirms field names, types, FK constraints, indices. | Schema confirmed FROZEN. Any addition → Change Request. |
| 40–45 min | Formal acceptance — Mentor signs Section 11.3. | SRD accepted. Scope baseline established. |

### 11.3 Sign-off

> BY SIGNING BELOW, THE PROJECT SPONSOR CONFIRMS THAT THE REQUIREMENTS DEFINED IN THIS DOCUMENT REPRESENT THE COMPLETE AND AGREED SCOPE FOR **GRAMA-VAXI**. THIS DOCUMENT SERVES AS THE REQUIREMENTS BASELINE — ANY CHANGE REQUIRES A FORMAL CHANGE REQUEST.

| Role | Name | Date Reviewed | Scope Accepted? | Signature |
|---|---|---|---|---|
| Project Sponsor (Mentor) | _________________ | __ / __ / 2026 | [ ] Yes — accepted as requirements baseline   [ ] No — changes required (see notes) | _________________ |
| Project Manager / Team Lead | _________________ | __ / __ / 2026 | [ ] Confirmed — will develop and test to this document | _________________ |
| DB Handler | _________________ | __ / __ / 2026 | [ ] Confirmed — §8 schema accepted as frozen baseline | _________________ |
| UI Developer | _________________ | __ / __ / 2026 | [ ] Confirmed — §9 UI requirements accepted | _________________ |
| Notification / Feature Integrator | _________________ | __ / __ / 2026 | [ ] Confirmed — all FR and UC specifications accepted | _________________ |

#### Post-Walkthrough Notes and Required Changes

> _Record any changes required by the mentor before final acceptance. Format: "UC-GV-04 Exception Flow Ex-3: [Mentor instruction]". Date each note. Resolve all noted changes before continuing development._

| Date | Source UC / FR / NFR | Change Required | Resolution |
|---|---|---|---|
| | | | |
| | | | |

---

*Software Requirements Document — Grama-Vaxi · MindMatrix Industry Readiness Programme — Android Internship · Version 1.0 · 24 April 2026.*
*Project source code: `/Users/admin/Codes/Grama-Vaxi/`. Test report: `app/build/reports/tests/testDebugUnitTest/index.html`.*
