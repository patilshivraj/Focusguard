package com.focusguard.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object TimeFormatting {
    private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    fun formatInstant(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String {
        return instant.atZone(zoneId).format(timeFormatter)
    }

    fun formatMinutesFromMidnight(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return String.format("%02d:%02d", h, m)
    }
}
