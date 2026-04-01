package com.nrs.nsnik.notes.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class CheckListObjectTest {

    @Test
    fun roundTrip_defaultValues() {
        val original = CheckListObject()
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<CheckListObject>(json)
        assertEquals(original.text, restored.text)
        assertEquals(original.done, restored.done)
    }

    @Test
    fun roundTrip_customValues() {
        val original = CheckListObject().apply {
            text = "Buy groceries"
            done = true
        }
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<CheckListObject>(json)
        assertEquals("Buy groceries", restored.text)
        assertTrue(restored.done)
    }

    @Test
    fun roundTrip_listOfCheckListObjects() {
        val list = listOf(
            CheckListObject().apply { text = "Item 1"; done = false },
            CheckListObject().apply { text = "Item 2"; done = true }
        )
        val json = Json.encodeToString(list)
        val restored = Json.decodeFromString<List<CheckListObject>>(json)
        assertEquals(2, restored.size)
        assertEquals("Item 1", restored[0].text)
        assertFalse(restored[0].done)
        assertEquals("Item 2", restored[1].text)
        assertTrue(restored[1].done)
    }

    @Test
    fun roundTrip_emptyText() {
        val original = CheckListObject().apply { text = ""; done = false }
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<CheckListObject>(json)
        assertEquals("", restored.text)
    }

    @Test
    fun id_isUniquePerInstance() {
        val a = CheckListObject()
        val b = CheckListObject()
        assertNotEquals(a.id, b.id)
    }

    @Test
    fun id_isNotSerialized() {
        val original = CheckListObject().apply { text = "test" }
        val json = Json.encodeToString(original)
        assertFalse("id should not appear in JSON", json.contains("\"id\""))
    }

    @Test
    fun id_isNewAfterDeserialization() {
        val original = CheckListObject().apply { text = "test" }
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<CheckListObject>(json)
        assertNotEquals(original.id, restored.id)
    }
}
