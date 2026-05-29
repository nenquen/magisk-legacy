package com.regisk.legacy.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.room.Room
import com.regisk.legacy.core.regiskdb.PolicyDao
import com.regisk.legacy.data.database.RepoDatabase
import com.regisk.legacy.data.database.SuLogDatabase
import com.regisk.legacy.data.repository.LogRepository
import com.regisk.legacy.core.regiskdb.SettingsDao
import com.regisk.legacy.core.regiskdb.StringDao
import com.regisk.legacy.ktx.deviceProtectedContext
import com.regisk.legacy.ui.log.LogViewModel
import com.regisk.legacy.ui.module.ModuleViewModel
import com.regisk.legacy.ui.settings.SettingsViewModel
import com.regisk.legacy.ui.superuser.SuperuserViewModel
import com.regisk.legacy.ui.surequest.SuRequestViewModel

val AppContext: Context inline get() = ServiceLocator.context

@SuppressLint("StaticFieldLeak")
object ServiceLocator {

    lateinit var context: Context
    val deContext by lazy { context.deviceProtectedContext }
    val timeoutPrefs by lazy { deContext.getSharedPreferences("su_timeout", 0) }

    // Database
    val policyDB = PolicyDao()
    val settingsDB = SettingsDao()
    val stringDB = StringDao()
    val repoDB by lazy { createRepoDatabase(context).repoDao() }
    val sulogDB by lazy { createSuLogDatabase(deContext).suLogDao() }
    val logRepo by lazy { LogRepository(sulogDB) }

    object VMFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(clz: Class<T>): T {
            return when (clz) {
                LogViewModel::class.java -> LogViewModel(logRepo)
                ModuleViewModel::class.java -> ModuleViewModel(repoDB)
                SettingsViewModel::class.java -> SettingsViewModel(repoDB)
                SuperuserViewModel::class.java -> SuperuserViewModel(policyDB)
                SuRequestViewModel::class.java -> SuRequestViewModel(policyDB, timeoutPrefs)
                else -> @Suppress("DEPRECATION") clz.newInstance()
            } as T
        }
    }
}

inline fun <reified VM : ViewModel> ViewModelStoreOwner.viewModel() =
    lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, ServiceLocator.VMFactory).get(VM::class.java)
    }

private fun createRepoDatabase(context: Context) =
    Room.databaseBuilder(context, RepoDatabase::class.java, "repo.db")
        .fallbackToDestructiveMigration()
        .build()

private fun createSuLogDatabase(context: Context) =
    Room.databaseBuilder(context, SuLogDatabase::class.java, "sulogs.db")
        .fallbackToDestructiveMigration()
        .build()
