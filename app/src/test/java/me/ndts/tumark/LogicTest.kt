package me.ndts.tumark

import org.junit.Assert.assertEquals
import org.junit.Test

class LogicTest {
    private val extraCourse =
        ExamEntry("id", "name", "grade", "credits", "status")
    private val remoteThesis =
        ExamEntry("20-AM-4000", "Bachelorarbeit Informatik", "1,3", "12,0", "bestanden")
    private val remoteSdms = ExamEntry(
        "20-00-1017",
        "Skalierbare Datenmanagement Systeme",
        "1,7",
        "6,0",
        "bestanden"
    )
    private val localThesis =
        ExamEntry("20-AM-4000", "Bachelorarbeit Informatik", "noch nicht gesetzt", "12,0", "")
    private val localSdms = ExamEntry(
        "20-00-1017",
        "Skalierbare Datenmanagement Systeme",
        "noch nicht gesetzt",
        "6,0",
        ""
    )
    private val remoteEntries = setOf(remoteThesis, remoteSdms)
    private val localEntries = setOf(localThesis, localSdms)

    @Test
    fun `tumarkRun does nothing when login data is invalid`() {
        var gre = false
        var gle = false
        var del = false
        var upd = false
        var ins = false
        var nou = false
        tumarkRun("", "",
            { _, _ ->
                gre = true
                remoteEntries
            },
            {
                gle = true
                localEntries
            },
            { del = true },
            { upd = true },
            { ins = true },
            { nou = true }
        )
        assert(!gre)
        assert(!gle)
        assert(!del)
        assert(!upd)
        assert(!ins)
        assert(!nou)
    }

    @Test
    fun `tumarkRun updates entries`() {
        var gre = false
        var gle = false
        var del = false
        var upd = false
        var ins = false
        var nou = false
        val updatedEntries = mutableSetOf<ExamEntry>()
        tumarkRun("ab12cdef", "1324",
            { _, _ ->
                gre = true
                remoteEntries
            },
            {
                gle = true
                localEntries
            },
            { del = true },
            {
                updatedEntries.add(it)
                upd = true
            },
            { ins = true },
            { nou = true }
        )
        assert(gre)
        assert(gle)
        assert(!del)
        assert(upd)
        assertEquals(updatedEntries, remoteEntries)
        assert(!ins)
        assert(nou)
    }

    @Test
    fun `tumarkRun inserts new entries`() {
        var gre = false
        var gle = false
        var del = false
        var upd = false
        var ins = false
        var nou = false
        val insertedEntries = mutableSetOf<ExamEntry>()
        tumarkRun("ab12cdef", "1324",
            { _, _ ->
                gre = true
                remoteEntries
            },
            {
                gle = true
                setOf(remoteThesis)
            },
            { del = true },
            { upd = true },
            {
                insertedEntries.add(it)
                ins = true
            },
            { nou = true }
        )
        assert(gre)
        assert(gle)
        assert(!del)
        assert(!upd)
        assert(ins)
        assertEquals(insertedEntries, setOf(remoteSdms))
        assert(nou)
    }

    @Test
    fun `tumarkRun deletes old entries`() {
        var gre = false
        var gle = false
        var del = false
        var upd = false
        var ins = false
        var nou = false
        val deletedEntries = mutableSetOf<ExamEntry>()
        tumarkRun("ab12cdef", "1324",
            { _, _ ->
                gre = true
                setOf(remoteThesis)
            },
            {
                gle = true
                remoteEntries
            },
            {
                deletedEntries.add(it)
                del = true
            },
            { upd = true },
            { ins = true },
            { nou = true }
        )
        assert(gre)
        assert(gle)
        assert(del)
        assertEquals(deletedEntries, setOf(remoteSdms))
        assert(!upd)
        assert(!ins)
        assert(nou)
    }

    @Test
    fun `tumarkRun deletes, updates, inserts, and notifies`() {
        var gre = false
        var gle = false
        var del = false
        var upd = false
        var ins = false
        var nou = false
        val deletedEntries = mutableSetOf<ExamEntry>()
        val updatedEntries = mutableSetOf<ExamEntry>()
        val insertedEntries = mutableSetOf<ExamEntry>()
        tumarkRun("ab12cdef", "1324",
            { _, _ ->
                gre = true
                remoteEntries
            },
            {
                gle = true
                setOf(localSdms, extraCourse)
            },
            {
                deletedEntries.add(it)
                del = true
            },
            {
                updatedEntries.add(it)
                upd = true
            },
            {
                insertedEntries.add(it)
                ins = true
            },
            { nou = true }
        )
        assert(gre)
        assert(gle)
        assert(del)
        assertEquals(deletedEntries, setOf(extraCourse))
        assert(upd)
        assertEquals(updatedEntries, setOf(remoteSdms))
        assert(ins)
        assertEquals(insertedEntries, setOf(remoteThesis))
        assert(nou)
    }
}