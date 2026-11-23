package com.example.insulia.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Central place for enqueuing deferred Insulia background work.
 *
 * Work names include the Firebase UID so switching accounts never mixes jobs.
 * All jobs are idempotent: a duplicate Patient ID request is harmless, and the
 * notification worker always reconciles from the current Firestore state.
 */
object InsuliaWorkScheduler {
    const val KEY_UID = "uid"

    private const val PATIENT_ID_WORK_PREFIX = "insulia_ensure_patient_id_"
    private const val NOTIFICATIONS_WORK_PREFIX = "insulia_reconcile_notifications_"
    private const val TAG_PATIENT_ID = "insulia_patient_id"
    private const val TAG_NOTIFICATIONS = "insulia_notifications"

    @Volatile
    private var appContext: Context? = null

    val isInitialized: Boolean get() = appContext != null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun enqueueEnsurePatientId(uid: String) {
        if (uid.isBlank()) return
        val manager = appContext?.let(WorkManager::getInstance) ?: return
        val request = OneTimeWorkRequestBuilder<EnsurePatientIdWorker>()
            .setConstraints(connectedConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .setInputData(workDataOf(KEY_UID to uid))
            .addTag(TAG_PATIENT_ID)
            .build()
        manager.enqueueUniqueWork(
            "$PATIENT_ID_WORK_PREFIX$uid",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun enqueueReconcileNotifications(uid: String) {
        if (uid.isBlank()) return
        val manager = appContext?.let(WorkManager::getInstance) ?: return
        val request = OneTimeWorkRequestBuilder<ReconcileNotificationsWorker>()
            .setConstraints(connectedConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .setInputData(workDataOf(KEY_UID to uid))
            .addTag(TAG_NOTIFICATIONS)
            .build()
        manager.enqueueUniqueWork(
            "$NOTIFICATIONS_WORK_PREFIX$uid",
            // A reconcile that is already running may have read the old state.
            // Append another pass so the latest medication/appointment/read
            // changes are never dropped.
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    private fun connectedConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}
