package com.hayhak.turkcesozluk.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RatePromptStoreTest {
    private fun at(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance().apply { clear(); set(year, month, day, hour, 0, 0) }.timeInMillis

    private val shown = at(2026, Calendar.MARCH, 10, 20)

    @Test fun notDueBeforeThreeDays() {
        assertFalse(RatePromptStore.isDue(shown, at(2026, Calendar.MARCH, 12, 23), 3))
    }

    @Test fun dueAtMidnightOfThirdDay() {
        assertTrue(RatePromptStore.isDue(shown, at(2026, Calendar.MARCH, 13, 0), 3))
    }

    @Test fun weeklyAfterPlayVisit() {
        assertFalse(RatePromptStore.isDue(shown, at(2026, Calendar.MARCH, 16), 7))
        assertTrue(RatePromptStore.isDue(shown, at(2026, Calendar.MARCH, 17), 7))
    }

    @Test fun clockMovedBackwardsDoesNotSuppress() {
        assertTrue(RatePromptStore.isDue(at(2030, Calendar.JANUARY, 1), shown, 7))
    }

    @Test fun daylightSavingChangeDoesNotShiftTheDay() {
        // 2026-03-29 is the EU spring-forward date; three days from the 27th is still the 30th.
        val last = at(2026, Calendar.MARCH, 27, 18)
        assertFalse(RatePromptStore.isDue(last, at(2026, Calendar.MARCH, 29, 23), 3))
        assertTrue(RatePromptStore.isDue(last, at(2026, Calendar.MARCH, 30, 0), 3))
    }
}
