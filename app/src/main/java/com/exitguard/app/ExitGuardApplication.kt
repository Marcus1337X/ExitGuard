package com.exitguard.app

import android.app.Application
import com.exitguard.app.data.AppRepository
import com.exitguard.app.data.RuleRepository
import com.exitguard.app.data.SettingsRepository
import com.exitguard.app.network.ExitDetectionService

class ExitGuardApplication : Application() {

    lateinit var appRepository: AppRepository
        private set

    lateinit var ruleRepository: RuleRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var exitDetectionService: ExitDetectionService
        private set

    override fun onCreate() {
        super.onCreate()
        appRepository = AppRepository(this)
        ruleRepository = RuleRepository(this)
        settingsRepository = SettingsRepository(this)
        exitDetectionService = ExitDetectionService(settingsRepository = settingsRepository)
    }
}
