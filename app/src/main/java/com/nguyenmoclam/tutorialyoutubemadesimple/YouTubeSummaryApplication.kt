package com.nguyenmoclam.tutorialyoutubemadesimple

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.KeyMigrationManager
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkStateListener
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineSyncManager
import com.nguyenmoclam.tutorialyoutubemadesimple.worker.ModelDataRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class YouTubeSummaryApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory // Inject HiltWorkerFactory

    @Inject
    lateinit var offlineSyncManager: OfflineSyncManager

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var networkStateListener: NetworkStateListener
    
    @Inject
    lateinit var keyMigrationManager: KeyMigrationManager

    override fun onCreate() {
        super.onCreate()

        // Initialize OfflineSyncManager and start observing network state changes
        offlineSyncManager.startObservingNetworkChanges()
        
        // Migrate API keys from BuildConfig to secure storage
        keyMigrationManager.migrateKeysIfNeeded()
        
        // Schedule background refresh of model data
        ModelDataRefreshWorker.schedule(this)
    }

    // Provide the HiltWorkerFactory to WorkManager
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
