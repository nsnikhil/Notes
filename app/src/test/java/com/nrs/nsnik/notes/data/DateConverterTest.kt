package com.nrs.nsnik.notes.data

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateConverterTest {

    private val converter = DateConverter()

    @Test
    fun dateToLong_andBack_roundTrip() {
        val original = Date()
        val asLong = converter.dateToLong(original)
        val restored = converter.longToDate(asLong)
        assertEquals(original.time, restored!!.time)
    }

    @Test
    fun dateToLong_nullInput_returnsNull() {
        assertNull(converter.dateToLong(null))
    }

    @Test
    fun longToDate_nullInput_returnsNull() {
        assertNull(converter.longToDate(null))
    }

    @Test
    fun longToDate_epoch_returnsEpochDate() {
        val date = converter.longToDate(0L)
        assertEquals(0L, date!!.time)
    }

    @Test
    fun roundTrip_specificKnownDate() {
        val millis = 1700000000000L
        val date = converter.longToDate(millis)
        val backToLong = converter.dateToLong(date)
        assertEquals(millis, backToLong)
    }
}
