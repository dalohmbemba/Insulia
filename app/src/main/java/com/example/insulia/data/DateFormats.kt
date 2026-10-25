package com.example.insulia.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Date helpers.
 *
 * `java.time` is avoided on purpose: the app supports API 23 and the app has
 * no core library desugaring enabled, so the legacy date API is used instead.
 */
object DateFormats {

    /** Short weekday name, for example "Mon". */
    fun dayLabel(millis: Long): String =
        SimpleDateFormat("EEE", Locale.getDefault()).format(Date(millis))

    /** Time of day in 24 hour format, for example "13:05". */
    fun timeLabel(millis: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

    /** Full date, for example "12 June 2002". */
    fun fullDate(millis: Long): String =
        SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(millis))

    /** Short date, for example "12 Jun 2002". */
    fun shortDate(millis: Long): String =
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))

    /**
     * Full date for a value that is stored in UTC, such as the millis returned
     * by the Material date picker.
     */
    fun utcFullDate(millis: Long): String =
        SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(Date(millis))
}
