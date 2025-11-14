package com.combinate.soferapp

import android.app.Application
import androidx.room.Room
import com.combinate.soferapp.data.local.SoferDatabase
import com.combinate.soferapp.data.remote.FakeBackendApi
import com.combinate.soferapp.data.repository.SoferRepository
import com.combinate.soferapp.domain.sync.SyncEngine

class SoferApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val database: SoferDatabase = Room.databaseBuilder(
        application,
        SoferDatabase::class.java,
        "sofer-db"
    ).fallbackToDestructiveMigration().build()

    private val backendApi = FakeBackendApi()

    val repository: SoferRepository = SoferRepository(
        backendApi = backendApi,
        database = database
    )

    val syncEngine: SyncEngine = SyncEngine(repository, backendApi)
}
