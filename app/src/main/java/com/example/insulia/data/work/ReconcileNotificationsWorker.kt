package com.example.insulia.data.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.insulia.data.repository.PatientRepository
import kotlinx.coroutines.CancellationException

/**
 * Rebuilds the derived AlertsScreen documents from the Firestore source of
 * truth. This runs after record changes and at app start so a killed process
 * cannot leave the alerts list stale.
 */
class ReconcileNotificationsWorker(
    context: Context,
    params: WorkerParameters,
    private val repositoryProvider: () -> PatientRepository = { PatientRepository() }
) : CoroutineWorker(context, params) {

    constructor(context: Context, params: WorkerParameters) : this(
        context,
        params,
        { PatientRepository() }
    )

    override suspend fun doWork(): Result {
        val repository = repositoryProvider()
        if (!repository.isAvailable) return Result.failure()
        val expectedUid = inputData.getString(InsuliaWorkScheduler.KEY_UID)
            ?: return Result.failure()

        return try {
            // A queued job must not create a fresh anonymous account after the
            // patient has signed out; that would orphan the previous records.
            val signedInUid = repository.accountUid ?: return Result.success()
            if (signedInUid != expectedUid) return Result.success()
            repository.reconcileNotifications()
            Result.success()
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            Log.w(TAG, "Could not reconcile notifications", t)
            retryOrFail()
        }
    }

    private fun retryOrFail(): Result = if (runAttemptCount >= MAX_ATTEMPTS - 1) {
        Result.failure()
    } else {
        Result.retry()
    }

    private companion object {
        const val TAG = "InsuliaWork"
        const val MAX_ATTEMPTS = 6
    }
}
