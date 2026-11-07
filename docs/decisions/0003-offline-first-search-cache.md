---
status: proposed
date: 2026-11-07
deciders: Lucas Lima
---

# Serve search results from a local cache first (offline-first)

## Context and Problem Statement

The Songs screen pages through the iTunes Search API. Mobile networks drop often, and the
challenge asks for the app to work offline and to show recently played songs. Where should the
screen read songs from: the network, or a local database that the network fills?

## Decision Drivers

- The app must open and show something useful with no network.
- Repeating a recent search should be instant and not spend data.
- Infinite scrolling must keep working, and errors must not erase what is already on screen.
- Recently played, albums, Android Auto and Wear read the same songs.

## Considered Options

- Network only, with an in-memory `PagingSource` over the API.
- Network first, falling back to a cache on error.
- Offline-first: Room is the single source of truth; a Paging `RemoteMediator` fills it page by page.

## Decision Outcome

Chosen option: "Offline-first with Room and a `RemoteMediator`", because the UI only ever reads
from the database. The network becomes a way to refresh it, so going offline changes nothing on
screen except a banner.

How it works (`SearchSongsRemoteMediator`):

- Each search is stored under a normalized key: trimmed, lowercased, single spaces.
- A search made less than one hour ago opens straight from the cache, without a request.
- An older search opens from the cache and refreshes on top of it.
- With no network, the cached pages stay, and the error arrives as a `LoadState.Error`, shown as an offline banner with a retry.

### Consequences

- Good, because the app works offline: the last searches, recently played songs and visited albums are always there.
- Good, because Android Auto and Wear read the same tables, so the "Last search" and "Recently played" sections need no extra storage.
- Good, because the mediator is a small class with a fake clock in tests; the cache rules are unit tested.
- Bad, because there is more code than a network-only `PagingSource`: entities, DAO and mediator.
- Bad, because the cache grows with every distinct search. There is no cleanup of old searches yet.
- Neutral, because the one-hour timeout is a guess; iTunes results change rarely.

### Confirmation

`testAndroidHostTest` runs the mediator and repository tests with an in-memory database and a fake
remote source.

## More Information

iTunes previews are streamed, not downloaded, so offline playback is out of scope. Offline mode
covers browsing; playing still needs network.
