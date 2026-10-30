package com.example.insulia

import android.app.Application

class InsuliaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val firebaseAppClass = Class.forName("com.google.firebase.FirebaseApp")
            val contextClass = Class.forName("android.content.Context")
            val getApps = firebaseAppClass.getMethod("getApps", contextClass)
            val apps = getApps.invoke(null, this) as? List<*>
            if (apps.isNullOrEmpty()) {
                val initializeApp = firebaseAppClass.getMethod("initializeApp", contextClass)
                initializeApp.invoke(null, this)
            }
        } catch (_: Throwable) {
            // Ignore Firebase initialization failures (e.g. missing GMS on some devices)
        }
    }
}
