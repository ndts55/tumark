package me.ndts.tumark

import android.content.Context
import android.util.Log

data class NotificationData(val title: String, val text: String)

suspend fun tumarkRun(context: Context) {
    val tuId = context.readTuId()
    val password = context.readPassword()
    val dao = context.database().examEntryDao()
    tumarkRun(
        tuId,
        password,
        getRemoteEntries = { u: String, p: String -> retrieveGrades(u, p) },
        getLocalEntries = { dao.getEntries().toSet() },
        deleteEntry = dao::delete,
        updateEntry = dao::update,
        insertEntry = dao::insert,
        notifyImportant = { ns ->
            ns.forEach { n ->
                NotificationHelper.notify(
                    context,
                    n.title,
                    n.text
                )
            }
        },
        log = {
            Log.d("tumark", it)
        }
    )
}

fun tumarkRun(
    tuId: String, password: String,
    getRemoteEntries: (u: String, p: String) -> Set<ExamEntry>,
    getLocalEntries: () -> Set<ExamEntry>,
    deleteEntry: (e: ExamEntry) -> Unit,
    updateEntry: (e: ExamEntry) -> Unit,
    insertEntry: (e: ExamEntry) -> Unit,
    notifyImportant: (ns: List<NotificationData>) -> Unit,
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

    // Create Notifications
    log("Creating notification data")
    val ns = diff.updated.map {
        NotificationData(
            "${it.identifier}: ${it.grade}",
            "${it.name} (${it.credits} cp) updated to ${it.status}."
        )
    }

    // Notify user
    log("Notifying users")
    notifyImportant(ns)
}
