package com.example.insulia.data.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.insulia.data.repository.PatientRepository
import kotlinx.coroutines.CancellationException

/**
 * Reserves the signed-in patient's permanent Patient ID when it could not be
 * assigned while the app was open (offline profile save, process death, etc.).
 *
 * Firestore transactions need a connection, so the WorkManager request carries
 * a network constraint. The ID allocation itself is idempotent and preserves
 * an ID that a previous attempt already assigned.
 */
class EnsurePatientIdWorker(
    context: Context,
    params: WorkerParameters,
    private val repositoryProvider: () -> PatientRepository = { PatientRepository() }
) : CoroutineWorker(context, params) {

    // WorkManager uses reflection to create workers with this exact signature.
    constructor(context: Context, params: WorkerParameters) : this(
        context,
        params,
        { PatientRepository() }
    )

    override suspend fun doWork(): Result {
        // Resolve Firebase lazily; Application.onCreate runs before doWork().
        val repository = repositoryProvider()
        if (!repository.isPatientIdAvailable) return Result.failure()
        val expectedUid = inputData.getString(InsuliaWorkScheduler.KEY_UID)
            ?: return Result.failure()

        return try {
            // Never sign someone in from a background job. After an explicit
            // sign-out there is no account whose ID should be reserved.
            val signedInUid = repository.accountUid ?: return Result.success()
            // The patient signed in to a different account before this ran.
            if (signedInUid != expectedUid) return Result.success()
            val patientId = repository.ensurePatientId()
            if (patientId.isNullOrBlank()) retryOrFail() else Result.success()
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            Log.w(TAG, "Could not ensure Patient ID", t)
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
        const val MAX_ATTEMPTS = 8
    }
}
