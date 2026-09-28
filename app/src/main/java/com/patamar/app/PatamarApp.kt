package com.patamar.app

import android.app.Application
import com.patamar.app.data.repository.EventRepository
import com.patamar.app.data.repository.UserRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PatamarApp : Application() {

    @Inject lateinit var eventRepository: EventRepository
    @Inject lateinit var userRepository: UserRepository

    // BETA: popula o banco com mock data (eventos + usuário de teste) na primeira execução.
    // Fica no Application, e não na Splash, pra não ser cancelado quando a tela fecha —
    // um seed cancelado deixava o login de teste sem usuário.
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            eventRepository.seedMockEvents()
            userRepository.seedTestUserIfEmpty()
        }
    }
}
