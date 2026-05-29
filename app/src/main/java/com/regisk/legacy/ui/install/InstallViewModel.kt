package com.regisk.legacy.ui.install

import android.app.Activity
import android.net.Uri
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.regisk.legacy.BR
import com.regisk.legacy.BuildConfig
import com.regisk.legacy.R
import com.regisk.legacy.arch.BaseViewModel
import com.regisk.legacy.core.Info
import com.regisk.legacy.di.AppContext
import com.regisk.legacy.events.RegiskInstallFileEvent
import com.regisk.legacy.events.dialog.SecondSlotWarningDialog
import com.regisk.legacy.ui.flash.FlashFragment
import com.regisk.legacy.utils.set
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException

class InstallViewModel() : BaseViewModel() {

    val isRooted = Shell.rootAccess()
    val skipOptions = Info.isEmulator || (Info.ramdisk && !Info.isFDE && Info.isSAR)
    val noSecondSlot = !isRooted || Info.isPixel || Info.isVirtualAB || !Info.isAB || Info.isEmulator

    @get:Bindable
    var step = if (skipOptions) 1 else 0
        set(value) = set(value, field, { field = it }, BR.step)

    var _method = -1

    @get:Bindable
    var method
        get() = _method
        set(value) = set(value, _method, { _method = it }, BR.method) {
            when (it) {
                R.id.method_patch -> {
                    RegiskInstallFileEvent { code, intent ->
                        if (code == Activity.RESULT_OK)
                            data = intent?.data
                    }.publish()
                }
                R.id.method_inactive_slot -> {
                    SecondSlotWarningDialog().publish()
                }
            }
        }

    @get:Bindable
    var data: Uri? = null
        set(value) = set(value, field, { field = it }, BR.data)

    @get:Bindable
    var notes = ""
        set(value) = set(value, field, { field = it }, BR.notes)

    init {
        notes = ""
    }

    fun step(nextStep: Int) {
        step = nextStep
    }

    fun install() {
        when (method) {
            R.id.method_patch -> FlashFragment.patch(data!!).navigate()
            R.id.method_direct -> FlashFragment.flash(false).navigate()
            R.id.method_inactive_slot -> FlashFragment.flash(true).navigate()
            else -> error("Unknown value")
        }
        state = State.LOADING
    }
}
