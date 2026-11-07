# musicsearch

A music search app built for the Moises Android code challenge. It searches the iTunes catalog,
plays 30-second previews, and keeps working offline. On top of the required phone app, the same
code runs on **iOS**, in **Android Auto**, and as a standalone **Wear OS** app.

Built with Kotlin Multiplatform, Compose Multiplatform, MVI, Room, Paging 3, Ktor, Koin and Media3.

## Features

**Required by the challenge**

- **Splash** with the app logo (SplashScreen API on Android, LaunchScreen on iOS).
- **Songs (Home):** search with debounce and infinite scrolling, pull to refresh, and the
  recently played list when the search is empty.
- **Player:** artwork, title, artist, a seekable timeline, play/pause, previous/next and repeat
  (off, one, all). Playback continues in the background with the system media notification.
- **More options:** a bottom sheet from any song row or from the player, with "View album".
- **Album:** cover, title, artist and the full track list in disc order. Tapping a track plays the album from there.
- **Offline-first:** results come from a local database. Earlier searches, recently played songs and
  visited albums still open with no network, and an offline banner offers a retry.

**Extras**

- **Shared-element transition:** the song cover grows from the list row into the player and back.
- **Accessibility:** headings, state descriptions ("Now playing", "No preview"), labelled
  controls, a timeline that reads as "0:12 of 0:30", and live announcements for empty, error and
  offline states.
- **Android Auto:** "Recently played" and "Last search" tabs, voice search, and the queue. Tabs refresh as you play and search.
- **Wear OS:** a standalone watch app with search (keyboard or voice), a player with a progress
  ring, recently played albums and their tracks.
- **iOS:** the same screens and logic, with AVPlayer and the iOS Now Playing controls.

## Demo

Screen recordings of the phone app, Android Auto and Wear OS are attached with the submission.

## Architecture

```
androidApp ─┐                 ┌─ feature:library:presentation  (MVI ViewModels + Compose screens)
iosApp ─────┼─ shared ────────┼─ feature:library:data          (Room, Ktor, Paging RemoteMediator)
            │  (DI, navigation)└─ feature:library:domain        (models, repository interfaces)
wearApp ────┘   reuses the feature modules with its own Wear UI
core:  domain · data · presentation · designsystem · playback · testing
```

- **MVI** in the presentation layer: each screen has one immutable `State`, a sealed `Action`
  as the only input, and a `Root`/`Screen` split so screens render without a ViewModel.
- **Offline-first data:** Room is the single source of truth. A Paging `RemoteMediator` fills it
  from the iTunes API. A search done less than an hour ago opens from the cache with no request.
- **One player:** a Media3 `MediaLibraryService` owns the ExoPlayer. The app, the notification and
  Android Auto all connect to it, so they always agree on what is playing. Shared code only sees
  an `AudioPlayer` port, which iOS implements with AVPlayer.
- **Rules checked by the build:** module dependencies (Gradle), architecture (Konsist tests),
  and formatting (ktlint via Spotless).

More detail:

- [`ARCHITECTURE.md`](ARCHITECTURE.md): modules, layers, error handling, DI, navigation and testing (in Portuguese).
- [`docs/decisions/`](docs/decisions/index.md): the ADRs, the reasoning behind the main choices.
  1. MVI.
  2. KMP and Compose Multiplatform.
  3. The offline-first cache.
  4. The single player service.

## Trade-offs and limitations

- **Previews only:** iTunes gives 30-second previews, and they stream. Offline mode covers
  browsing, not playback.
- **Cache growth:** every distinct search is stored. There is no cleanup of old searches yet.
- **Same UI on iOS:** iOS shares the Compose UI instead of using native SwiftUI. This trades a
  native look for one codebase.
- **No phone–watch sync:** the Wear app is standalone. It talks to iTunes directly and does not
  share playback with the phone.
- **Android Auto details:** section titles are English only, and a queue started from search has no title.
- **iOS tests:** there are no iOS UI tests. Shared logic runs on the iOS simulator in tests, but
  screens are tested on Android only.
- **Notification permission:** the app does not ask for the notification permission. On Android 13+, media
  session notifications are exempt and show even when it is denied.

## Requirements

- JDK 21. Gradle downloads it if missing (`gradle/gradle-daemon-jvm.properties`).
- Android SDK with platform 36, set in `local.properties` (`sdk.dir=...`).
- For iOS: macOS with Xcode.

## Running

### Phone (Android)

```bash
./gradlew :androidApp:installDebug
```

Or run the `androidApp` configuration in Android Studio. Requires Android 8.0 (API 26) or newer.

### Wear OS

Create a Wear OS emulator (API 30+) in Android Studio's Device Manager, start it, then run:

```bash
./gradlew :wearApp:installDebug
```

Or run the `wearApp` configuration. The app shows up as "Music Search" on the watch.

### Android Auto

Android Auto needs a phone with the Android Auto app. The emulator's Android Auto is a stub.

1. Install the phone app on the device: `./gradlew :androidApp:installDebug`.
2. On the phone, open **Android Auto settings**, tap **Version** ten times to turn on developer
   mode, then in the ⋮ menu turn on **Unknown sources** and choose **Start head unit server**.
3. Install the Desktop Head Unit from the SDK Manager (SDK Tools → Android Auto Desktop Head Unit
   Emulator), then forward the port and start it:

   ```bash
   adb forward tcp:5277 tcp:5277
   ```

   ```bash
   ~/Library/Android/sdk/extras/google/auto/desktop-head-unit
   ```

4. Open the media apps in the car screen and pick **Music Search**. Play something on the phone
   first to fill "Recently played", or use the search button.

To turn developer mode off afterwards, use ⋮ → **Quit developer mode** in Android Auto settings.

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme on a simulator. To run on
a device, set your `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`.

## Tests and checks

| What | Command |
|---|---|
| Format, then check | `./gradlew spotlessApply` then `./gradlew spotlessCheck` |
| Architecture rules (Konsist) | `./gradlew :konsist:test` |
| Unit tests (JVM) | `./gradlew testAndroidHostTest` |
| Shared tests on the iOS simulator | `./gradlew iosSimulatorArm64Test` |
| Phone UI and playback service tests (emulator running) | `./gradlew connectedAndroidDeviceTest` |
| Wear UI tests (watch emulator running) | `./gradlew :wearApp:connectedDebugAndroidTest` |

What is tested:

- **ViewModels and repositories:** unit tests with hand-written fakes, Turbine, and a fake clock.
- **Screens:** device tests with the real ViewModel.
- **Media service:** a device test that connects a real `MediaBrowser` to `PlaybackService`.

CI (`.github/workflows/ci.yml`) runs:

- On every push: formatting, Konsist, unit tests, and both Android builds.
- On pull requests: also device tests on an emulator and the iOS tests.
