package com.lucaslima.musicsearch.feature.library.data.fakes

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

internal class FakeClock(var now: Instant = Instant.fromEpochMilliseconds(1_000_000)) : Clock {
    override fun now(): Instant = now

    fun advanceBy(duration: Duration) {
        now += duration
    }
}
