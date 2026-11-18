package co.edu.uniquindio.truekeapp

import android.app.Application
import android.util.Log
import co.edu.uniquindio.truekeapp.data.seed.SeedDataInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TruekeApplication : Application() {

    @Inject lateinit var seedDataInitializer: SeedDataInitializer

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        // La primera vez carga en Firebase los datos de prueba (si ya están, no hace nada)
        appScope.launch {
            runCatching { seedDataInitializer.seedIfNeeded() }
                .onFailure { Log.e("TruekeApplication", "No se pudieron cargar los datos de prueba", it) }
        }
    }
}
