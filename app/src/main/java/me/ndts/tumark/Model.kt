package me.ndts.tumark

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_entries")
data class ExamEntry(
    @PrimaryKey val identifier: String,
    val name: String,
    val grade: String,
    val credits: String,
    val status: String
)

data class Diff(
    val deleted: Set<ExamEntry>,
    val updated: Set<ExamEntry>,
    val inserted: Set<ExamEntry>,
)

infix fun Set<ExamEntry>.diffTo(other: Set<ExamEntry>): Diff {
    if (this == other) {
        return Diff(emptySet(), emptySet(), emptySet())
    }

    val thisIdentifierToEntry = this.associateBy { it.identifier }
    val otherIdentifierToEntry = other.associateBy { it.identifier }

    // Deleted
    val deletedIdentifiers = thisIdentifierToEntry.keys subtract otherIdentifierToEntry.keys
    // We know that the identifier HAS to be in the map because we only work with those identifiers.
    val deleted = deletedIdentifiers.map { thisIdentifierToEntry[it]!! }.toSet()

    // Inserted
    val insertedIdentifiers = otherIdentifierToEntry.keys subtract thisIdentifierToEntry.keys
    // We know that the identifier HAS to be in the map because we only work with those identifiers.
    val inserted = insertedIdentifiers.map { otherIdentifierToEntry[it]!! }.toSet()

    // Updated
    val updatedIdentifiers =
        (other subtract this).map { it.identifier } subtract insertedIdentifiers
    // We know that the identifier HAS to be in the map because we only work with those identifiers.
    val updated = updatedIdentifiers.map { otherIdentifierToEntry[it]!! }.toSet()

    return Diff(deleted, updated, inserted)
}

