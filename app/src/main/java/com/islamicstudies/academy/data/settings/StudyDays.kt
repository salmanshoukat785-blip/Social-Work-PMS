package com.islamicstudies.academy.data.settings

import java.util.TimeZone

/** Number of days since 1970-01-01 in the phone's local time zone. */
fun localEpochDay(now: Long = System.currentTimeMillis()): Long {
    val offset = TimeZone.getDefault().getOffset(now)
    return Math.floorDiv(now + offset, 86_400_000L)
}

/** Consecutive study days ending today (or yesterday, if today has no study yet). */
fun currentStreak(days: Set<Long>, today: Long): Int {
    var day = if (days.contains(today)) today else today - 1
    var streak = 0
    while (days.contains(day)) {
        streak += 1
        day -= 1
    }
    return streak
}
