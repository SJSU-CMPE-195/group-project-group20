
# **GPT-Powered Meal-Planning**

> A GPT-powered meal planning app that assists users in crafting day-to-day diet plans

## Team

| Name | GitHub | Email |
|------|--------|-------|
| Tanish Shah | [@stanish28](https://github.com/stanish28) | tanish.shah@sjsu.edu |
| Ishan Sikka  | [@ishansikka](https://github.com/username) | ishan.sikka@sjsu.edu |
| Name 3 | [@erttransjsu](https://github.com/erttransjsu) | eric.tran05@sjsu.edu |
| Zihao He | [@zihaohe2022](https://github.com/zihaohe2022) | zihao.he01@sjsu.edu |

**Advisor:** Jun Liu

---

## Problem Statement

The tradional way for diet control with restrictive dieting and calorie restriction is not pratical effective. As a result, it may lead to slower metabolisms or even an increased likelihood of binge eating. 

## Solution

We try to build a GPT-powered meal planning app that assists users in crafting day-to-day diet plans for users just beginning their fitness journey.

## Project Description

This project is a simple calendar-based meal-planning app that lets the user input their fitness goals and their consumed calories/protein for a selected day choosable via the calendar screen. The app will return the remaining calories/protein needed for consumption to fit the user's goals for the selected day. The user can go to the "User" screen to see their net calories/protein consumed and days allotted since the beginning of their fitness journey.

## Proof-of-Concept Scope

This proof of concept demonstrates the general flow of the app we have in mind. It provides the framework for the main "gimmick" of our app--that is, this app is what is built around the main GPT module. The proof of concept shows what can be done in-app and locally, and provides the majority of what the app will do for the user (including a stub for the Recommendation implementation that will demo how GPT will present meals to the user). We do NOT have persistent information storage quite yet (that will come with Room implementation) nor do we have GPT implementation/the tagging system, as this PoC is meant to demonstrate the app's "core" functionalities first.

## What's Next?

For our next implementation, we plan on adding our Python backend, Room implementation for persistent user info storage, actual OpenAI API calling, and basic GPT recommendation module. For our final implementation, we'll have a smart tagging system to learn what the user likes, and let GPT assign the user meals based off those tags and their nutrient/fitness goals as parameters. We will also implement auto-input of user info with our paired hardware.

### Key Features

- Health Tracking
- Meal Planning/Calorie and Protein Tracking Calendar

### Hardware integration

Fitness band connectivity—BLE transport from band to phone/PC (prototype status and implementation path) for Hardware and Software—is described in [docs/hardware-fitness-band-connectivity.md](docs/hardware-fitness-band-connectivity.md).

---

## Demo

**Live Demo:** https://drive.google.com/file/d/1-pBeTOaMbrHi1RQev1v0bt4YqWlTw0h_/view?usp=sharing

---

## Screenshots

| Feature | Screenshot |
|---------|------------|
| Health Tracking: https://drive.google.com/file/d/1ZfnkbrlzSUaJ7U2VvPX1gIpAkA2S9n_j/view?usp=sharing |
| Calendar: https://drive.google.com/file/d/13qHX_4FFrsgPrwX6ExgOlaICDS_ZCfcI/view?usp=sharing |

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Frontend | Kotlin |
| Backend | Python (planned) |
| Database | Room (planned), SQLite (planned) |
| Deployment | Android Studio |
| Build	| Gradle |

---

## Getting Started

### Prerequisites

- Android Studio (stable)
- SDK 35
- Gradle v8.11.1
- AGP v8.9.2
- AVD Emulator (provided by Andorid Studio)
- Compose enabled
- Desugaring enabled
- Dependencies synced

### Installation

```bash
# Clone the repository
git clone https://github.com/[org]/[repo].git
cd [repo]

# Open the project in Android Studio
# File > Open > select the project folder

# Let Gradle sync automatically
# If it does not, click:
# File > Sync Project with Gradle Files
```

### Running Locally

```bash
# Build the debug APK
./gradlew assembleDebug
```

### Running Tests

```bash
# Run local unit tests
./gradlew test

# Run instrumented Android tests on an emulator/device
./gradlew connectedAndroidTest
```

---

## API Reference

<details>
<summary>Click to expand API endpoints</summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/resource` | Get all resources |
| GET | `/api/resource/:id` | Get resource by ID |
| POST | `/api/resource` | Create new resource |
| PUT | `/api/resource/:id` | Update resource |
| DELETE | `/api/resource/:id` | Delete resource |

</details>

---

## Acknowledgments

- [Resource/Library/Person]
- [Resource/Library/Person]

---

## License

This project is licensed under the <FILL IN> License - see the [LICENSE](LICENSE) file for details.

---

*CMPE 195A/B - Senior Design Project | San Jose State University | Spring 2026*
