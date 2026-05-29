package com.regisk.legacy.ui.hide

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.os.Process
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.regisk.legacy.BR
import com.regisk.legacy.arch.BaseViewModel
import com.regisk.legacy.arch.Queryable
import com.regisk.legacy.arch.filterableListOf
import com.regisk.legacy.arch.itemBindingOf
import com.regisk.legacy.core.Config
import com.regisk.legacy.di.AppContext
import com.regisk.legacy.utils.Utils
import com.regisk.legacy.utils.set
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HideViewModel : BaseViewModel(), Queryable {

    override val queryDelay = 1000L

    @get:Bindable
    var isShowSystem = Config.showSystemApp
        set(value) = set(value, field, { field = it }, BR.showSystem) {
            Config.showSystemApp = it
            submitQuery()
        }

    @get:Bindable
    var isShowOS = false
        set(value) = set(value, field, { field = it }, BR.showOS) {
            submitQuery()
        }

    @get:Bindable
    var query = ""
        set(value) = set(value, field, { field = it }, BR.query) {
            submitQuery()
        }

    val items = filterableListOf<HideRvItem>()
    val itemBinding = itemBindingOf<HideRvItem> {
        it.bindExtra(BR.viewModel, this)
    }
    val itemInternalBinding = itemBindingOf<HideProcessRvItem> {
        it.bindExtra(BR.viewModel, this)
    }

    @SuppressLint("InlinedApi")
    override fun refresh() = viewModelScope.launch {
        if (!Utils.showSuperUser()) {
            state = State.LOADING_FAILED
            return@launch
        }
        state = State.LOADING
        val (apps, diff) = withContext(Dispatchers.Default) {
            val pm = AppContext.packageManager
            val hideList = Shell.su("magiskhide ls").exec().out.map { CmdlineHiddenItem(it) }
            val apps = pm.getInstalledApplications(MATCH_UNINSTALLED_PACKAGES)
                .asSequence()
                .filterNot { blacklist.contains(it.packageName) }
                .map { HideAppInfo(it, pm, hideList) }
                .filter { it.processes.isNotEmpty() }
                .filter { info -> info.enabled || info.processes.any { it.isHidden } }
                .map { HideRvItem(it) }
                .toList()
                .sorted()
            apps to items.calculateDiff(apps)
        }
        items.update(apps, diff)
        submitQuery()
    }

    // ---

    override fun query() {
        items.filter {
            fun showHidden() = it.itemsChecked != 0

            fun filterSystem() = isShowSystem || it.info.flags and ApplicationInfo.FLAG_SYSTEM == 0

            fun isApp(uid: Int) = run {
                val appId: Int = uid % 100000
                appId >= Process.FIRST_APPLICATION_UID && appId <= Process.LAST_APPLICATION_UID
            }

            fun filterOS() = (isShowSystem && isShowOS) || isApp(it.info.uid)

            fun filterQuery(): Boolean {
                fun inName() = it.info.label.contains(query, true)
                fun inPackage() = it.info.packageName.contains(query, true)
                fun inProcesses() = it.processes.any { p -> p.process.name.contains(query, true) }
                return inName() || inPackage() || inProcesses()
            }

            showHidden() || (filterSystem() && filterOS() && filterQuery())
        }
        state = State.LOADED
    }

    // ---

    fun resetQuery() {
        query = ""
    }

    companion object {
        private val blacklist by lazy { listOf(
            AppContext.packageName,
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.chrome.canary",
            "com.android.webview",
            "com.google.android.webview"
        ) }
    }
}
