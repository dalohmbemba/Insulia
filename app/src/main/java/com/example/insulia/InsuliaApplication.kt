package com.example.insulia

import android.app.Application
import com.google.firebase.FirebaseApp

class InsuliaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (_: Throwable) {
        }
    }
}
