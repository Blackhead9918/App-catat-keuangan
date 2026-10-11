package com.example

import com.example.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testIsDueExact_futureTimeReturnsFalse() {
        val futureTime = System.currentTimeMillis() + 60_000L // 1 minute in future
        assertFalse(DateUtils.isDueExact(futureTime))
    }

    @Test
    fun testIsDueExact_pastTimeReturnsTrue() {
        val pastTime = System.currentTimeMillis() - 60_000L // 1 minute in past
        assertTrue(DateUtils.isDueExact(pastTime))
    }

    @Test
    fun testFormatTime_returnsNonEmptyHourMinute() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 30)
        }
        val formatted = DateUtils.formatTime(cal.timeInMillis)
        assertEquals("08:30", formatted)
    }
}
