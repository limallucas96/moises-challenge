---
status: proposed
date: 2026-11-07
deciders: Lucas Lima
---

# Share the app across Android and iOS with Kotlin Multiplatform and Compose Multiplatform

## Context and Problem Statement

The challenge asks for an Android app. A music app usually also ships on iOS, and the logic
(search, cache, recently played, queue handling) does not depend on the platform. Should the
project be Android-only, or share code with iOS from the start?

## Decision Drivers

- Show that the architecture scales past one platform without rewriting the domain and data layers.
- Keep Android-first quality: Media3, Android Auto and Wear OS must not be compromised.
- Limit the cost: one language, and one UI toolkit where possible.

## Considered Options

- Android-only with Jetpack Compose.
- KMP for domain and data only, with native UI on each platform (Compose on Android, SwiftUI on iOS).
- KMP plus Compose Multiplatform: shared domain, data, presentation and UI.

## Decision Outcome

Chosen option: "KMP plus Compose Multiplatform", because the project skeleton already supports it,
and it puts the ViewModels and screens in `commonMain` once. Platform code stays behind small
ports: `AudioPlayer` (Media3 on Android, AVPlayer on iOS), and the database and HTTP engines.

### Consequences

- Good, because the screens, ViewModels, repositories, Room database and Ktor client are written once and run on both platforms.
- Good, because Android-only features (Media3 session, Android Auto, Wear) plug in through the same ports without touching shared code.
- Bad, because every dependency in `commonMain` must be multiplatform. For example Paging, Room, Coil and Navigation 3 had to be versions with iOS targets.
- Bad, because iOS builds need Xcode and `iosSimulatorArm64Test` adds CI time.
- Neutral, because iOS gets the same UI as Android rather than a native iOS look.

### Confirmation

`./gradlew iosSimulatorArm64Test` runs the shared tests on iOS. The module rules in
`build-logic/convention/src/main/kotlin/app.module.rules.gradle.kts` keep platform code out of
`commonMain`.

## More Information

The Wear OS app (`:wearApp`) is a plain Android module. It reuses the shared ViewModels and data
layer, but has its own Wear Compose UI, because Compose Multiplatform has no Wear components.
