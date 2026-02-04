# 🤖 SoundWave AI Development Rules

This document defines the technical standards and architectural guidelines for the SoundWave Android application.

## 🛠 Tech Stack

*   **Language:** 100% Kotlin with a focus on functional programming patterns.
*   **UI Framework:** Jetpack Compose for all UI components and screens.
*   **Architecture:** Clean Architecture with distinct layers: `domain` (business logic), `data` (repositories/sources), and `ui` (ViewModels/Composables).
*   **Media Playback:** Jetpack Media3 (ExoPlayer) for high-performance audio streaming and local playback.
*   **Dependency Injection:** Hilt (Dagger) for managing component lifecycles and dependencies.
*   **Local Database:** Room Persistence Library for caching metadata, playlists, and user settings.
*   **Asynchronous Programming:** Kotlin Coroutines and StateFlow/SharedFlow for reactive data streams.
*   **Image Loading:** Coil for efficient, lifecycle-aware image loading and caching.
*   **Design System:** Material Design 3 (M3) with support for Dynamic Colors (Material You).
*   **Navigation:** Jetpack Compose Navigation with type-safe route definitions.

## 📏 Library Usage Rules

### 1. Media & Playback
*   **Rule:** Always use `androidx.media3` for anything related to audio playback, sessions, or notifications.
*   **Avoid:** Do not use the legacy `MediaPlayer` or `MediaSessionCompat` APIs.

### 2. UI & Styling
*   **Rule:** Use **Jetpack Compose** exclusively. No XML layouts for new features.
*   **Rule:** Use **Material 3** components. Custom components should extend M3 themes.
*   **Rule:** Primary UI language is **Arabic (RTL)**. Ensure all layouts are tested for RTL support.

### 3. Data & Persistence
*   **Rule:** Use **Room** for structured data (songs, playlists).
*   **Rule:** Use **Kotlinx Serialization** for JSON parsing and DataStore/Room type converters.
*   **Rule:** All database operations must be performed on a background dispatcher via Coroutines.

### 4. Dependency Injection
*   **Rule:** Use `@HiltViewModel` for all ViewModels.
*   **Rule:** Use constructor injection whenever possible. Avoid field injection unless required by the Android framework (e.g., in Services).

### 5. State Management
*   **Rule:** ViewModels must expose state via `StateFlow`.
*   **Rule:** Use `update { ... }` for atomic state updates to avoid race conditions.
*   **Rule:** Keep UI logic out of the ViewModel; it should only handle business logic and state transitions.

### 6. Icons & Assets
*   **Rule:** Use `androidx.compose.material:material-icons-extended` for a wide range of icons.
*   **Rule:** Prefer Vector Drawables over PNG/JPG for UI icons.

## 🏗 Project Structure
*   `data/`: Implementation of repositories, DAOs, and API clients.
*   `domain/`: Pure Kotlin models and repository interfaces.
*   `player/`: Media3 Service implementation and playback logic.
*   `ui/`: Compose screens, components, and themes.
*   `di/`: Hilt modules for dependency configuration.