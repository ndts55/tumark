package me.ndts.tumark

import android.content.Context
import android.util.Log


suspend fun tumarkRun(context: Context) {
    val tuId = context.readTuId()
    val password = context.readPassword()
    val db = context.database()
    val dao = db.examEntryDao()
    tumarkRun(
        tuId,
        password,
        getRemoteEntries = { u: String, p: String -> retrieveGrades(u, p) },
        getLocalEntries = { dao.getEntries().toSet() },
        deleteEntry = dao::delete,
        updateEntry = dao::update,
        insertEntry = dao::insert,
        notify = { diff ->
            diff.updated.forEach {
                NotificationHelper.notify(
                    context,
                    "${it.identifier}: ${it.grade}",
                    "${it.name} (${it.credits} cp) updated to ${it.status}."
                )
            }
        },
        log = {
            Log.d("tumark", it)
        }
    )
    db.close()
}

fun tumarkRun(
    tuId: String, password: String,
    getRemoteEntries: (u: String, p: String) -> Set<ExamEntry>,
    getLocalEntries: () -> Set<ExamEntry>,
    deleteEntry: (e: ExamEntry) -> Unit,
    updateEntry: (e: ExamEntry) -> Unit,
    insertEntry: (e: ExamEntry) -> Unit,
    notify: (diff: Diff) -> Unit,
    log: (s: String) -> Unit = {}
) {
    // Retrieve login information
    if (tuId.isEmpty() || password.isEmpty()) {
        log("Invalid login information")
        return
    }

    // Retrieve remote data
    log("Retrieving remote data")
    val remoteEntries: Set<ExamEntry>
    try {
        remoteEntries = getRemoteEntries(tuId, password)
    } catch (e: Exception) {
        log("Error retrieving remote data")
        log(e.toString())
        return
    }
    log("Retrieved ${remoteEntries.joinToString()} from remote")

    // Retrieve local data
    log("Retrieving local data")
    val localEntries = getLocalEntries()
    log("Retrieved ${localEntries.joinToString()} from local")

    // Create diff from local to remote data
    log("Comparing datasets")
    val diff = localEntries diffTo remoteEntries

    // Apply changes to local data
    log("Deleting ${diff.deleted.joinToString()}")
    diff.deleted.forEach(deleteEntry)
    log("Inserting ${diff.inserted.joinToString()}")
    diff.inserted.forEach(insertEntry)
    log("Updating ${diff.updated.joinToString()}")
    diff.updated.forEach(updateEntry)

    // Notify user
    log("Notifying users")
    notify(diff)
}
