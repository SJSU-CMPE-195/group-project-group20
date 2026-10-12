package com.fitfuel.app

import android.app.Application
import com.fitfuel.app.data.local.FitFuelDatabase
import com.fitfuel.app.data.remote.FitFuelNetwork
import com.fitfuel.app.data.repository.DayEntryRepository
import com.fitfuel.app.data.repository.OfflineDayEntryRepository
import com.fitfuel.app.data.repository.OfflineTagPreferenceRepository
import com.fitfuel.app.data.repository.RecommendationRepository
import com.fitfuel.app.data.repository.TagPreferenceRepository
import com.fitfuel.app.data.repository.RemoteRecommendationRepository

// 195B Weeks 1-2: Lightweight application container supplies persistent dependencies.
class FitFuelApplication : Application() {
    private val database by lazy { FitFuelDatabase.getInstance(this) }

    val dayEntryRepository: DayEntryRepository by lazy {
        OfflineDayEntryRepository(database.dayEntryDao())
    }

    // 195B Weeks 5-6: Shared preference repository persists tag-learning signals for every screen.
    val tagPreferenceRepository: TagPreferenceRepository by lazy {
        OfflineTagPreferenceRepository(database.tagPreferenceDao())
    }

    // 195B Weeks 3-4: Backend repository is created once and shared by the app ViewModel.
    val recommendationRepository: RecommendationRepository by lazy {
        RemoteRecommendationRepository(FitFuelNetwork.createRecommendationApi())
    }
}
