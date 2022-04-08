package me.ndts.tumark

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit


const val GradeWorkName = "Tumark Worker"


class GradeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        tumarkRun(super.getApplicationContext())
        return Result.success()
    }
}

fun Context.enqueueGradeWorker(): Unit {
    val constraints: Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val workRequest =
        PeriodicWorkRequestBuilder<GradeWorker>(1, TimeUnit.HOURS).setConstraints(constraints)
            .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        GradeWorkName,
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}
