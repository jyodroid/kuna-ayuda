package com.jyodroid.kunasismoayuda.ui.quakes

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Formats an epoch-millis instant as `yyyy-MM-dd HH:mm` in the device's timezone. */
fun formatQuakeTime(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val mm = dt.monthNumber.pad()
    val dd = dt.dayOfMonth.pad()
    val hh = dt.hour.pad()
    val min = dt.minute.pad()
    return "${dt.year}-$mm-$dd $hh:$min"
}

private fun Int.pad(): String = if (this < 10) "0$this" else "$this"
