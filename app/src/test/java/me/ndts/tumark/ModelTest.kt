package me.ndts.tumark

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelTest {
    private val thesis =
        ExamEntry("20-AM-4000", "Bachelorarbeit Informatik", "noch nicht gesetzt", "12,0", "")
    private val sdms = ExamEntry(
        "20-00-1017",
        "Skalierbare Datenmanagement Systeme",
        "noch nicht gesetzt",
        "6,0",
        ""
    )
    private val testSet: Set<ExamEntry> = setOf(thesis, sdms)

    @Test
    fun `diffTo returns empty diff when passed equal sets`() {
        val set0 = testSet
        val diff = set0 diffTo set0
        assert(diff.deleted.isEmpty())
        assert(diff.inserted.isEmpty())
        assert(diff.updated.isEmpty())
    }

    @Test
    fun `diffTo returns non-empty deleted when passed set and subset`() {
        val diff = testSet diffTo setOf(sdms)
        assert(diff.deleted.isNotEmpty())
        assertEquals(diff.deleted, setOf(thesis))
        assert(diff.inserted.isEmpty())
        assert(diff.updated.isEmpty())
    }

    @Test
    fun `diffTo returns non-empty inserted when passed subset and set`() {
        val diff = setOf(sdms) diffTo testSet
        assert(diff.deleted.isEmpty())
        assert(diff.inserted.isNotEmpty())
        assertEquals(diff.inserted, setOf(thesis))
        assert(diff.updated.isEmpty())
    }

    @Test
    fun `diffTo returns non-empty updated when passed a set with updated entries`() {
        val updatedEntry = ExamEntry(sdms.identifier, sdms.name, "1,0", sdms.credits, sdms.status)
        val updatedTestSet = setOf(updatedEntry, thesis)
        val diff = testSet diffTo updatedTestSet
        assert(diff.deleted.isEmpty())
        assert(diff.inserted.isEmpty())
        assert(diff.updated.isNotEmpty())
        assertEquals(diff.updated, setOf(updatedEntry))
    }
}