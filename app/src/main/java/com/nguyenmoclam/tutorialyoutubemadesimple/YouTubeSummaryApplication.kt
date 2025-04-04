package com.nguyenmoclam.tutorialyoutubemadesimple

import android.app.Application
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkStateListener
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class YouTubeSummaryApplication : Application() {
    // Application-level initialization can be added here if needed

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
}