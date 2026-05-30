package com.regisk.legacy.ui.home

import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.regisk.legacy.BuildConfig
import com.regisk.legacy.R
import com.regisk.legacy.arch.*
import com.regisk.legacy.core.Config
import com.regisk.legacy.core.Info
import com.regisk.legacy.core.download.Subject
import com.regisk.legacy.core.download.Subject.Manager
import com.regisk.legacy.events.OpenInappLinkEvent
import com.regisk.legacy.events.SnackbarEvent
import com.regisk.legacy.events.dialog.EnvFixDialog
import com.regisk.legacy.events.dialog.ManagerInstallDialog
import com.regisk.legacy.events.dialog.UninstallDialog
import com.regisk.legacy.ktx.await
import com.regisk.legacy.ui.flash.FlashFragment
import com.regisk.legacy.utils.asText
import com.regisk.legacy.utils.set
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.BR
import kotlin.math.roundToInt

enum class RegiskState {
    NOT_INSTALLED, UP_TO_DATE, OBSOLETE, LOADING
}

class HomeViewModel() : BaseViewModel() {

    val regiskTitleBarrierIds =
        intArrayOf(R.id.home_regisk_icon, R.id.home_regisk_title, R.id.home_regisk_button)
    val regiskDetailBarrierIds =
        intArrayOf(R.id.home_regisk_installed_version, R.id.home_device_details_ramdisk)
    val appTitleBarrierIds =
        intArrayOf(R.id.home_manager_icon, R.id.home_manager_title, R.id.home_manager_button)



    val stateRegisk = when {
        !Info.env.isActive -> RegiskState.NOT_INSTALLED
        Info.env.regiskVersionCode < BuildConfig.VERSION_CODE -> RegiskState.OBSOLETE
        else -> RegiskState.UP_TO_DATE
    }

    @get:Bindable
    var stateManager = RegiskState.LOADING
        set(value) = set(value, field, { field = it }, BR.stateManager)

    val regiskInstalledVersion get() = Info.env.run {
        if (isActive)
            "$regiskVersionString ($regiskVersionCode)".asText()
        else
            R.string.not_available.asText()
    }

    @get:Bindable
    var managerRemoteVersion = R.string.loading.asText()
        set(value) = set(value, field, { field = it }, BR.managerRemoteVersion)

    val managerInstalledVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

    @get:Bindable
    var stateManagerProgress = 0
        set(value) = set(value, field, { field = it }, BR.stateManagerProgress)

    val itemBinding = itemBindingOf<IconLink> {
        it.bindExtra(BR.viewModel, this)
    }

    private var shownDialog = false

    override fun refresh() = viewModelScope.launch {
        state = State.LOADED
        managerRemoteVersion = managerInstalledVersion.asText()
        launch {
            ensureEnv()
        }
    }

    val showTest = false

    fun onTestPressed() = object : ViewEvent(), ActivityExecutor {
        override fun invoke(activity: BaseUIActivity<*, *>) {
            /* Entry point to trigger test events within the app */
        }
    }.publish()

    fun onProgressUpdate(progress: Float, subject: Subject) {
        if (subject is Manager)
            stateManagerProgress = progress.times(100f).roundToInt()
    }

    fun onLinkPressed(link: String) = OpenInappLinkEvent(link).publish()

    fun onDeletePressed() = UninstallDialog().publish()

    fun onManagerPressed() = when (state) {
        State.LOADED -> withExternalRW { ManagerInstallDialog().publish() }
        State.LOADING -> SnackbarEvent(R.string.loading).publish()
        else -> SnackbarEvent(R.string.no_connection).publish()
    }

    fun onRegiskPressed() = withExternalRW {
        if (Shell.rootAccess()) {
            FlashFragment.flash(false).navigate()
        } else {
            HomeFragmentDirections.actionHomeFragmentToInstallFragment().navigate()
        }
    }



    private suspend fun ensureEnv() {
        val invalidStates = listOf(
            RegiskState.NOT_INSTALLED,
            RegiskState.LOADING
        )
        if (invalidStates.any { it == stateRegisk } || shownDialog) return

        val result = Shell.su("env_check").await()
        if (!result.isSuccess) {
            shownDialog = true
            EnvFixDialog().publish()
        }
    }

}
