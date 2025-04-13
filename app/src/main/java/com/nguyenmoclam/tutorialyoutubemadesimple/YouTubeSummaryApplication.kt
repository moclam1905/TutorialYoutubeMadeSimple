package com.nguyenmoclam.tutorialyoutubemadesimple

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory // Import HiltWorkerFactory
import androidx.work.Configuration // Import Configuration
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkStateListener
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class YouTubeSummaryApplication : Application(), Configuration.Provider { // Implement Configuration.Provider

    @Inject
    lateinit var workerFactory: HiltWorkerFactory // Inject HiltWorkerFactory

    @Inject
    lateinit var offlineSyncManager: OfflineSyncManager
    
    @Inject
    lateinit var networkUtils: NetworkUtils
    
    @Inject
    lateinit var networkStateListener: NetworkStateListener
    
    override fun onCreate() {
        super.onCreate()

        // Initialize OfflineSyncManager and start observing network state changes
        offlineSyncManager.startObservingNetworkChanges()
    }

    // Provide the HiltWorkerFactory to WorkManager
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
