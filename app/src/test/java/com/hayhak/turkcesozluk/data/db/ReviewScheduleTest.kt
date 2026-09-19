package com.hayhak.turkcesozluk.data.db

import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewScheduleTest {
    @Test fun forgottenCardResetsAndReturnsInTenMinutes() {
        assertEquals(0 to 601_000L, ReviewSchedule.next(5, false, 1_000L))
    }
    @Test fun knownCardsProgressFromOneDayToSixtyDays() {
        assertEquals(1 to 86_400_000L, ReviewSchedule.next(0, true, 0))
        assertEquals(2 to 259_200_000L, ReviewSchedule.next(1, true, 0))
        assertEquals(3 to 604_800_000L, ReviewSchedule.next(2, true, 0))
        assertEquals(4 to 1_209_600_000L, ReviewSchedule.next(3, true, 0))
        assertEquals(5 to 2_592_000_000L, ReviewSchedule.next(4, true, 0))
        assertEquals(6 to 5_184_000_000L, ReviewSchedule.next(5, true, 0))
        assertEquals(6 to 5_184_000_000L, ReviewSchedule.next(6, true, 0))
    }
}
