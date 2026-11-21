package com.example.insulia

import android.app.Application
import android.util.Log
import com.example.insulia.data.work.InsuliaWorkScheduler
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Application entry point.
 *
 * Firebase and the Firestore offline cache are configured here, before any
 * screen can touch Firestore. Records created without a connection are stored
 * in the local cache and uploaded automatically when the patient is online
 * again.
 */
class InsuliaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        InsuliaWorkScheduler.initialize(this)
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }

            // Settings can only be applied before Firestore is first used.
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
                .build()
            firestore.firestoreSettings = settings
        } catch (t: Throwable) {
            // A device without Google Play services can still open the app.
            // Failed reads and saves are reported to the patient instead of
            // crashing the app.
            Log.w("Insulia", "Firebase could not be initialised", t)
        }
    }
}
