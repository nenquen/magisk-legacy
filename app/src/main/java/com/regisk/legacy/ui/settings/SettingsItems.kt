package com.regisk.legacy.ui.settings

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.Bindable
import com.regisk.legacy.BR
import com.regisk.legacy.BuildConfig
import com.regisk.legacy.R
import com.regisk.legacy.core.Config
import com.regisk.legacy.core.Const
import com.regisk.legacy.core.Info
import com.regisk.legacy.core.UpdateCheckService
import com.regisk.legacy.core.tasks.HideAPK
import com.regisk.legacy.core.utils.BiometricHelper
import com.regisk.legacy.core.utils.MediaStoreUtils
import com.regisk.legacy.core.utils.availableLocales
import com.regisk.legacy.core.utils.currentLocale
import com.regisk.legacy.databinding.DialogSettingsAppNameBinding
import com.regisk.legacy.databinding.DialogSettingsDownloadPathBinding

import com.regisk.legacy.di.AppContext
import com.regisk.legacy.utils.Utils
import com.regisk.legacy.utils.asText
import com.regisk.legacy.utils.set
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// --- Customization

object Customization : BaseSettingsItem.Section() {
    override val title = R.string.settings_customization.asText()
}



object Theme : BaseSettingsItem.Blank() {
    override val icon = R.drawable.ic_paint
    override val title = R.string.section_theme.asText()
}

// --- App

object AppSettings : BaseSettingsItem.Section() {
    override val title = R.string.home_app_title.asText()
}

object ClearRepoCache : BaseSettingsItem.Blank() {
    override val title = R.string.settings_clear_cache_title.asText()
    override val description = R.string.settings_clear_cache_summary.asText()

    override fun refresh() {
        isEnabled = Info.env.isActive
    }
}

object Hide : BaseSettingsItem.Input() {
    override val title = R.string.settings_hide_app_title.asText()
    override val description = R.string.settings_hide_app_summary.asText()

    override var value = ""
        set(value) = setV(value, field, { field = it })

    override val inputResult
        get() = if (isError) null else result

    @get:Bindable
    var result = "Settings"
        set(value) = set(value, field, { field = it }, BR.result, BR.error)

    val maxLength
        get() = HideAPK.MAX_LABEL_LENGTH

    @get:Bindable
    val isError
        get() = result.length > maxLength || result.isBlank()

    override fun getView(context: Context) = DialogSettingsAppNameBinding
        .inflate(LayoutInflater.from(context)).also { it.data = this }.root

    override fun refresh() {
        isEnabled = Info.remote.stub.versionCode > 0
    }
}

object Restore : BaseSettingsItem.Blank() {
    override val title = R.string.settings_restore_app_title.asText()
    override val description = R.string.settings_restore_app_summary.asText()
}

object AddShortcut : BaseSettingsItem.Blank() {
    override val title = R.string.add_shortcut_title.asText()
    override val description = R.string.setting_add_shortcut_summary.asText()
}

object DownloadPath : BaseSettingsItem.Input() {
    override var value = Config.downloadDir
        set(value) = setV(value, field, { field = it }) { Config.downloadDir = it }

    override val title = R.string.settings_download_path_title.asText()
    override val description get() = path.asText()

    override val inputResult: String get() = result

    @get:Bindable
    var result = value
        set(value) = set(value, field, { field = it }, BR.result, BR.path)

    @get:Bindable
    val path
        get() = MediaStoreUtils.fullPath(result)

    override fun getView(context: Context) = DialogSettingsDownloadPathBinding
        .inflate(LayoutInflater.from(context)).also { it.data = this }.root
}







// check whether is module already installed beforehand?
object SystemlessHosts : BaseSettingsItem.Blank() {
    override val title = R.string.settings_hosts_title.asText()
    override val description = R.string.settings_hosts_summary.asText()
}

object Tapjack : BaseSettingsItem.Toggle() {
    override val title = R.string.settings_su_tapjack_title.asText()
    override var description = R.string.settings_su_tapjack_summary.asText()
    override var value = Config.suTapjack
        set(value) = setV(value, field, { field = it }) { Config.suTapjack = it }
}

object Biometrics : BaseSettingsItem.Toggle() {
    override val title = R.string.settings_su_biometric_title.asText()
    override var value = Config.suBiometric
        set(value) = setV(value, field, { field = it }) { Config.suBiometric = it }
    override var description = R.string.settings_su_biometric_summary.asText()

    override fun refresh() {
        isEnabled = BiometricHelper.isSupported
        if (!isEnabled) {
            value = false
            description = R.string.no_biometric.asText()
        }
    }
}

object Reauthenticate : BaseSettingsItem.Toggle() {
    override val title = R.string.settings_su_reauth_title.asText()
    override val description = R.string.settings_su_reauth_summary.asText()
    override var value = Config.suReAuth
        set(value) = setV(value, field, { field = it }) { Config.suReAuth = it }

    override fun refresh() {
        isEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.O && Utils.showSuperUser()
    }
}

// --- Regisk

object Regisk : BaseSettingsItem.Section() {
    override val title = R.string.regisk.asText()
}

object RegiskHide : BaseSettingsItem.Toggle() {
    override val title = R.string.regiskhide.asText()
    override val description = R.string.settings_regiskhide_summary.asText()
    override var value = Config.regiskHide
        set(value) = setV(value, field, { field = it }) {
            val cmd = if (it) "enable" else "disable"
            Shell.su("magiskhide $cmd").submit { cb ->
                if (cb.isSuccess) Config.regiskHide = it
                else field = !it
            }
        }
}

// --- Superuser

object Superuser : BaseSettingsItem.Section() {
    override val title = R.string.superuser.asText()
}

object AccessMode : BaseSettingsItem.Selector() {
    override val title = R.string.superuser_access.asText()
    override val entryRes = R.array.su_access

    override var value = Config.rootMode
        set(value) = setV(value, field, { field = it }) {
            Config.rootMode = it
        }
}

object MultiuserMode : BaseSettingsItem.Selector() {
    override val title = R.string.multiuser_mode.asText()
    override val entryRes = R.array.multiuser_mode
    override val descriptionRes = R.array.multiuser_summary

    override var value = Config.suMultiuserMode
        set(value) = setV(value, field, { field = it }) {
            Config.suMultiuserMode = it
        }

    override fun refresh() {
        isEnabled = Const.USER_ID == 0
    }
}

object MountNamespaceMode : BaseSettingsItem.Selector() {
    override val title = R.string.mount_namespace_mode.asText()
    override val entryRes = R.array.namespace
    override val descriptionRes = R.array.namespace_summary

    override var value = Config.suMntNamespaceMode
        set(value) = setV(value, field, { field = it }) {
            Config.suMntNamespaceMode = it
        }
}

object AutomaticResponse : BaseSettingsItem.Selector() {
    override val title = R.string.auto_response.asText()
    override val entryRes = R.array.auto_response

    override var value = Config.suAutoResponse
        set(value) = setV(value, field, { field = it }) {
            Config.suAutoResponse = it
        }
}

object RequestTimeout : BaseSettingsItem.Selector() {
    override val title = R.string.request_timeout.asText()
    override val entryRes = R.array.request_timeout

    private val entryValues = listOf(10, 15, 20, 30, 45, 60)
    override var value = selected
        set(value) = setV(value, field, { field = it }) {
            Config.suDefaultTimeout = entryValues[it]
        }

    private val selected: Int
        get() = entryValues.indexOfFirst { it == Config.suDefaultTimeout }
}

object SUNotification : BaseSettingsItem.Selector() {
    override val title = R.string.superuser_notification.asText()
    override val entryRes = R.array.su_notification

    override var value = Config.suNotification
        set(value) = setV(value, field, { field = it }) {
            Config.suNotification = it
        }
}
