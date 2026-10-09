package com.focusguard.blocking

import java.time.Instant

data class BlockDecision(
    val shouldBlock: Boolean,
    val reason: String,
    val availableAgainAt: Instant? = null,
)
