package com.regisk.legacy.ui.flash

import android.view.MenuItem
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.regisk.legacy.BR
import com.regisk.legacy.R
import com.regisk.legacy.arch.BaseViewModel
import com.regisk.legacy.arch.diffListOf
import com.regisk.legacy.arch.itemBindingOf
import com.regisk.legacy.core.Const
import com.regisk.legacy.core.Info
import com.regisk.legacy.core.tasks.FlashZip
import com.regisk.legacy.core.tasks.RegiskInstaller
import com.regisk.legacy.core.utils.MediaStoreUtils
import com.regisk.legacy.core.utils.MediaStoreUtils.outputStream
import com.regisk.legacy.databinding.RvBindingAdapter
import com.regisk.legacy.events.SnackbarEvent
import com.regisk.legacy.ktx.*
import com.regisk.legacy.utils.set
import com.regisk.legacy.view.Notifications
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FlashViewModel : BaseViewModel() {

    @get:Bindable
    var showReboot = Shell.rootAccess()
        set(value) = set(value, field, { field = it }, BR.showReboot)

    private val _subtitle = MutableLiveData(R.string.flashing)
    val subtitle get() = _subtitle as LiveData<Int>

    val adapter = RvBindingAdapter<ConsoleItem>()
    val items = diffListOf<ConsoleItem>()
    val itemBinding = itemBindingOf<ConsoleItem>()
    lateinit var args: FlashFragmentArgs

    private val logItems = mutableListOf<String>().synchronized()
    private val outItems = object : CallbackList<String>() {
        override fun onAddElement(e: String?) {
            e ?: return
            items.add(ConsoleItem(e))
            logItems.add(e)
        }
    }

    fun startFlashing() {
        val (action, uri, id) = args
        if (id != -1)
            Notifications.mgr.cancel(id)

        viewModelScope.launch {
            val result = when (action) {
                Const.Value.FLASH_ZIP -> {
                    FlashZip(uri!!, outItems, logItems).exec()
                }
                Const.Value.UNINSTALL -> {
                    showReboot = false
                    RegiskInstaller.Uninstall(outItems, logItems).exec()
                }
                Const.Value.FLASH_REGISK -> {
                    if (Info.isEmulator)
                        RegiskInstaller.Emulator(outItems, logItems).exec()
                    else
                        RegiskInstaller.Direct(outItems, logItems).exec()
                }
                Const.Value.FLASH_INACTIVE_SLOT -> {
                    RegiskInstaller.SecondSlot(outItems, logItems).exec()
                }
                Const.Value.PATCH_FILE -> {
                    uri ?: return@launch
                    showReboot = false
                    RegiskInstaller.Patch(uri, outItems, logItems).exec()
                }
                else -> {
                    back()
                    return@launch
                }
            }
            onResult(result)
        }
    }

    private fun onResult(success: Boolean) {
        state = if (success) State.LOADED else State.LOADING_FAILED
        when {
            success -> _subtitle.postValue(R.string.done)
            else -> _subtitle.postValue(R.string.failure)
        }
    }

    fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> savePressed()
        }
        return true
    }

    private fun savePressed() = withExternalRW {
        viewModelScope.launch(Dispatchers.IO) {
            val name = "regisk_install_log_%s.log".format(now.toTime(timeFormatStandard))
            val file = MediaStoreUtils.getFile(name, true)
            file.uri.outputStream().bufferedWriter().use { writer ->
                logItems.forEach {
                    writer.write(it)
                    writer.newLine()
                }
            }
            SnackbarEvent(file.toString()).publish()
        }
    }

    fun restartPressed() = reboot()
}
