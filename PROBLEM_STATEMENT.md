# Grama-Vaxi — Android App Development using GenAI (Healthcare)

## 1. The Problem Statement
Livestock in villages often die from outbreaks because farmers miss "Vaccination Camp" dates announced only via local loudspeakers.

## 2. Detailed Description (The Vision)
**Grama-Vaxi** is a *Livestock Health Alert* app. It tracks the vaccination cycle of every sheep/goat in the village. It acts as a **Digital Health Card** and sends loud notifications **3 days before** a government camp reaches the village.

## 3. App Usage & User Flow
- **Animal Ledger** — Register animals with photo, breed, and age.
- **Vaccine Calendar** — Automatically generates "Next Shot" dates.
- **Camp Alert** — Notification: *"Doctor arriving at Temple Square tomorrow."*
- **Disease Alert** — Report a sick animal to the local Vet (Simulated).

## 4. Technical Implementation & Hints
- **WorkManager** — Schedule notifications for months in advance.
- **Room DB** — Store offline animal medical history.
- **UI** — Use large *Animal Icons* for navigation.

## 5. Impact Goals
- **Livestock Wealth** — Preventing animal loss, which is the *Savings Account* of farmers.
- **Animal Welfare** — Ensuring timely medical care for the rural cattle population.
- **Health Digitization** — Creating a database of village animal health.

## 6. Success Criteria for Students
- Reminders must trigger **even if the app hasn't been opened for days**.
- The *Register Animal* form must be **simple and visual**.
- The UI must support **Kannada**.
