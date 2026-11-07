---
status: proposed
date: 2026-11-07
deciders: Lucas Lima
---

# Use one Media3 player service for the app, the system controls and Android Auto

## Context and Problem Statement

Playback is controlled from several places: the app's Player screen, the media notification, the
lock screen, Bluetooth buttons, and Android Auto. If each one held its own player, they would
disagree about what is playing. Where should the player live, and how do the clients reach it?

## Decision Drivers

- One queue and one "now playing" for every client, at all times.
- Keep playing in the background, with the system's media notification.
- Android Auto support with browsing and voice search, without a second app.
- Keep the shared code free of Android types.

## Considered Options

- An ExoPlayer owned by the app process, driven by the ViewModels, plus a separate session for the system.
- One `MediaLibraryService` (`PlaybackService`) that owns the ExoPlayer; every client connects to it as a media controller.

## Decision Outcome

Chosen option: "One `MediaLibraryService`", because Media3 already makes the service the single
owner of the player. The app's `Media3AudioPlayer` is just another `MediaController`, the same as
the notification and Android Auto.

- Shared code only sees the `AudioPlayer` port. iOS implements it with AVPlayer.
- Android Auto browses through the `MediaCatalog` port. `LibraryMediaCatalog` serves "Recently played" and "Last search" from the database.
- The Wear OS app runs on the watch in its own process. It reuses the same modules and gets its own instance of this service.

### Consequences

- Good, because the app, the notification and Android Auto always show the same song and queue.
- Good, because background playback and the media notification come from Media3 with no custom notification code.
- Good, because the Auto browse tree is plain Kotlin over the `MediaCatalog` port, tested with a fake.
- Bad, because the controller connection is asynchronous, so `Media3AudioPlayer` has to queue calls until it connects.
- Bad, because the service is exported, which Media3 and Android Auto require.
- Neutral, because playback state is not shared between the phone and the watch.

### Confirmation

`PlaybackServiceDeviceTest` connects a real `MediaBrowser` to the service and checks the browse
tree, search and section-change notifications on a device.
