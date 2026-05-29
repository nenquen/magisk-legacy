package com.regisk.legacy.ui.log

import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.regisk.legacy.BR
import com.regisk.legacy.BuildConfig
import com.regisk.legacy.R
import com.regisk.legacy.arch.BaseViewModel
import com.regisk.legacy.arch.diffListOf
import com.regisk.legacy.arch.itemBindingOf
import com.regisk.legacy.core.Info
import com.regisk.legacy.core.utils.MediaStoreUtils
import com.regisk.legacy.core.utils.MediaStoreUtils.outputStream
import com.regisk.legacy.data.repository.LogRepository
import com.regisk.legacy.events.SnackbarEvent
import com.regisk.legacy.ktx.now
import com.regisk.legacy.ktx.timeFormatStandard
import com.regisk.legacy.ktx.toTime
import com.regisk.legacy.utils.set
import com.regisk.legacy.view.TextItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewModel(
    private val repo: LogRepository
) : BaseViewModel() {

    // --- empty view

    val itemEmpty = TextItem(R.string.log_data_none)
    val itemRegiskEmpty = TextItem(R.string.log_data_regisk_none)

    // --- su log

    val items = diffListOf<LogRvItem>()
    val itemBinding = itemBindingOf<LogRvItem> {
        it.bindExtra(BR.viewModel, this)
    }

    // --- regisk log
    @get:Bindable
    var consoleText = " "
        set(value) = set(value, field, { field = it }, BR.consoleText)

    override fun refresh() = viewModelScope.launch {
        consoleText = repo.fetchRegiskLogs()
        val (suLogs, diff) = withContext(Dispatchers.Default) {
            val suLogs = repo.fetchSuLogs().map { LogRvItem(it) }
            suLogs to items.calculateDiff(suLogs)
        }
        items.firstOrNull()?.isTop = false
        items.lastOrNull()?.isBottom = false
        items.update(suLogs, diff)
        items.firstOrNull()?.isTop = true
        items.lastOrNull()?.isBottom = true
    }

    fun saveRegiskLog() = withExternalRW {
        viewModelScope.launch(Dispatchers.IO) {
            val filename = "regisk_log_%s.log".format(now.toTime(timeFormatStandard))
            val logFile = MediaStoreUtils.getFile(filename, true)
            logFile.uri.outputStream().bufferedWriter().use { file ->
                file.write("---System Properties---\n\n")

                ProcessBuilder("getprop").start()
                    .inputStream.reader().use { it.copyTo(file) }

                file.write("\n---Regisk Logs---\n")
                file.write("${Info.env.regiskVersionString} (${Info.env.regiskVersionCode})\n\n")
                file.write(consoleText)

                file.write("\n---Manager Logs---\n")
                file.write("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n")
                ProcessBuilder("logcat", "-d").start()
                    .inputStream.reader().use { it.copyTo(file) }
            }
            SnackbarEvent(logFile.toString()).publish()
        }
    }

    fun clearRegiskLog() = repo.clearRegiskLogs {
        SnackbarEvent(R.string.logs_cleared).publish()
        requestRefresh()
    }

    fun clearLog() = viewModelScope.launch {
        repo.clearLogs()
        SnackbarEvent(R.string.logs_cleared).publish()
        requestRefresh()
    }
}
