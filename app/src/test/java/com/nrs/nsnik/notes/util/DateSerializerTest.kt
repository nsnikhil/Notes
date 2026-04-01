package com.nrs.nsnik.notes.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateSerializerTest {

    @Serializable
    data class DateHolder(
        @Serializable(with = DateSerializer::class)
        val date: Date
    )

    @Test
    fun roundTrip_currentDate() {
        val original = DateHolder(Date())
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<DateHolder>(json)
        assertEquals(original.date.time, restored.date.time)
    }

    @Test
    fun roundTrip_epochDate() {
        val original = DateHolder(Date(0))
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<DateHolder>(json)
        assertEquals(0L, restored.date.time)
    }

    @Test
    fun roundTrip_specificDate() {
        val millis = 1700000000000L
        val original = DateHolder(Date(millis))
        val json = Json.encodeToString(original)
        assertTrue(json.contains(millis.toString()))
        val restored = Json.decodeFromString<DateHolder>(json)
        assertEquals(millis, restored.date.time)
    }

    @Test
    fun serializedFormat_isLong() {
        val millis = 1234567890L
        val holder = DateHolder(Date(millis))
        val json = Json.encodeToString(holder)
        assertEquals("""{"date":$millis}""", json)
    }
}
