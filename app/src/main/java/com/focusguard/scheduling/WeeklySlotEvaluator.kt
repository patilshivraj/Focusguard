package com.focusguard.scheduling

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object WeeklySlotEvaluator {
    fun isWithinAnySlot(
        slots: List<WeeklyTimeSlot>,
        now: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        if (slots.isEmpty()) return false
        val zoned = now.atZone(zoneId)
        val dayBit = dayBitFor(zoned.toLocalDate())
        val minutes = zoned.hour * 60 + zoned.minute
        return slots.any { slot ->
            if ((slot.daysOfWeekBitmask and dayBit) == 0) return@any false
            isMinuteInRange(minutes, slot.startMinutes, slot.endMinutes)
        }
    }

    fun nextSlotEnd(
        slots: List<WeeklyTimeSlot>,
        now: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Instant? {
        if (!isWithinAnySlot(slots, now, zoneId)) return null
        val zoned = now.atZone(zoneId)
        val date = zoned.toLocalDate()
        val minutes = zoned.hour * 60 + zoned.minute
        val dayBit = dayBitFor(date)
        val active = slots.filter { (it.daysOfWeekBitmask and dayBit) != 0 && isMinuteInRange(minutes, it.startMinutes, it.endMinutes) }
        val endMinutes = active.minOfOrNull { endMinuteFor(it, minutes) } ?: return null
        val endDate = if (endMinutes <= minutes && active.any { it.startMinutes > it.endMinutes }) {
            date.plusDays(1)
        } else {
            date
        }
        val endTime = LocalTime.ofSecondOfDay((endMinutes % (24 * 60)) * 60L)
        return LocalDateTime.of(endDate, endTime).atZone(zoneId).toInstant()
    }

    private fun endMinuteFor(slot: WeeklyTimeSlot, nowMinutes: Int): Int {
        if (slot.startMinutes < slot.endMinutes) return slot.endMinutes
        return if (nowMinutes >= slot.startMinutes) slot.endMinutes + 24 * 60 else slot.endMinutes
    }

    private fun isMinuteInRange(minutes: Int, start: Int, end: Int): Boolean {
        if (start == end) return false
        if (start < end) return minutes in start until end
        return minutes >= start || minutes < end
    }

    fun dayBitFor(date: LocalDate): Int =
        when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> ScheduleRule.MONDAY
            java.time.DayOfWeek.TUESDAY -> ScheduleRule.TUESDAY
            java.time.DayOfWeek.WEDNESDAY -> ScheduleRule.WEDNESDAY
            java.time.DayOfWeek.THURSDAY -> ScheduleRule.THURSDAY
            java.time.DayOfWeek.FRIDAY -> ScheduleRule.FRIDAY
            java.time.DayOfWeek.SATURDAY -> ScheduleRule.SATURDAY
            java.time.DayOfWeek.SUNDAY -> ScheduleRule.SUNDAY
        }
}
