package com.englishlearning.newconcept

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for New Concept English app
 */
@HiltAndroidApp
class NewConceptEnglishApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize app-wide components
    }
}
