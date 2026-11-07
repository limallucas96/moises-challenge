---
status: proposed
date: 2026-11-07
deciders: Lucas Lima
---

# Use MVI for the presentation layer

## Context and Problem Statement

Every screen in the app (Songs, Player, Album, and the Wear screens) reacts to several inputs at
once: the search query, paged results, connectivity, the database, and the player state. How
should a screen hold that state and receive user input so it stays predictable and easy to test?

## Decision Drivers

- One place to read what a screen shows, so a bug can be reproduced from a single value.
- ViewModels testable with plain unit tests and fakes, with no Compose involved.
- Composables that render from a value, so previews and UI tests need no ViewModel.
- The same ViewModels must serve the phone and the Wear OS app.

## Considered Options

- MVI: one immutable `State`, a sealed `Action` as the only input, and an optional `Event` for one-off effects.
- Plain MVVM: several exposed `StateFlow`s and public functions per user action.

## Decision Outcome

Chosen option: "MVI", because a single `State` plus a single `onAction` entry point answers every
driver. A screen is a function of one value, and every user intent is a named type that a test can
send.

### Consequences

- Good, because each `<Name>Screen` takes `state` and `onAction`, so it has previews and device tests without a ViewModel.
- Good, because ViewModel tests send actions and assert on one `State`; there are no mocks.
- Good, because the Wear app reuses `SongsViewModel`, `NowPlayingViewModel` and `AlbumViewModel` without changes. Only the composables differ.
- Bad, because simple screens pay a little boilerplate: a contract file with `State` and `Action`.
- Bad, because a large `State` recomposes more unless the composables read only the fields they need.

### Confirmation

`ARCHITECTURE.md` §3 sets the convention. `PresentationRulesTest` in `konsist/` checks that every
ViewModel lives in a presentation module and exposes `state`, and that every `*Screen.kt` file has
both a `Root` and a `Screen`. The rest (`On<Thing>Click` names, no ViewModel inside a `Screen`) is
checked in code review.

## More Information

The paged search results are the one exception to "everything in `State`". `PagingData` is exposed
as its own `Flow` (`searchResults`) because Paging must be collected with
`collectAsLazyPagingItems()` to keep its caching and load states.
