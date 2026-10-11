package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CalendarDay(
    val dayOfMonth: Int,
    val month: Int, // 0-based
    val year: Int,
    val timeInMillis: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)

object DateUtils {
    private val localeId = Locale("id", "ID")

    fun formatDateIndo(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMMM yyyy", localeId)
        return sdf.format(timestamp)
    }

    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM", localeId)
        return sdf.format(timestamp)
    }

    fun formatDateTimeIndo(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy, HH:mm", localeId)
        return sdf.format(timestamp)
    }

    fun formatMonthYear(calendar: Calendar): String {
        val sdf = SimpleDateFormat("MMMM yyyy", localeId)
        return sdf.format(calendar.time)
    }

    fun isSameDay(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun isToday(timestamp: Long): Boolean {
        return isSameDay(timestamp, System.currentTimeMillis())
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", localeId)
        return sdf.format(timestamp)
    }

    fun isDueOrPast(timestamp: Long): Boolean {
        return System.currentTimeMillis() >= timestamp
    }

    fun isDueExact(timestamp: Long): Boolean {
        return System.currentTimeMillis() >= timestamp
    }

    fun getMonthDays(year: Int, month: Int): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
        // Convert to Monday as 0, Sunday as 6
        val dayOffset = (firstDayOfWeek + 5) % 7

        // Previous month padding
        val prevMonthCal = (cal.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        val maxDaysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in (maxDaysInPrevMonth - dayOffset + 1)..maxDaysInPrevMonth) {
            prevMonthCal.set(Calendar.DAY_OF_MONTH, i)
            days.add(
                CalendarDay(
                    dayOfMonth = i,
                    month = prevMonthCal.get(Calendar.MONTH),
                    year = prevMonthCal.get(Calendar.YEAR),
                    timeInMillis = prevMonthCal.timeInMillis,
                    isCurrentMonth = false,
                    isToday = isSameDay(prevMonthCal.timeInMillis, System.currentTimeMillis())
                )
            )
        }

        // Current month days
        val maxDaysInCurrentMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDaysInCurrentMonth) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            days.add(
                CalendarDay(
                    dayOfMonth = i,
                    month = month,
                    year = year,
                    timeInMillis = cal.timeInMillis,
                    isCurrentMonth = true,
                    isToday = isSameDay(cal.timeInMillis, System.currentTimeMillis())
                )
            )
        }

        // Next month padding to fill grid to multiple of 7
        val remaining = (7 - (days.size % 7)) % 7
        val nextMonthCal = (cal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        for (i in 1..remaining) {
            nextMonthCal.set(Calendar.DAY_OF_MONTH, i)
            days.add(
                CalendarDay(
                    dayOfMonth = i,
                    month = nextMonthCal.get(Calendar.MONTH),
                    year = nextMonthCal.get(Calendar.YEAR),
                    timeInMillis = nextMonthCal.timeInMillis,
                    isCurrentMonth = false,
                    isToday = isSameDay(nextMonthCal.timeInMillis, System.currentTimeMillis())
                )
            )
        }

        return days
    }
}
