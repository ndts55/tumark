package me.ndts.tumark

import android.content.Context
import androidx.room.*

@Dao
interface ExamEntryDao {
    @Query("select * from exam_entries")
    fun getEntries(): List<ExamEntry>

    @Update
    fun update(vararg entries: ExamEntry)

    @Insert
    fun insert(vararg entries: ExamEntry)

    @Delete
    fun delete(entry: ExamEntry)
}

@Database(entities = [ExamEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun examEntryDao(): ExamEntryDao
}

fun Context.database(): AppDatabase = Room.databaseBuilder(
    this,
    AppDatabase::class.java, "tumark-database"
).build()
