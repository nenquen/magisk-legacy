package com.regisk.legacy.events.dialog

import com.regisk.legacy.R
import com.regisk.legacy.core.Info
import com.regisk.legacy.core.download.DownloadService
import com.regisk.legacy.core.download.Subject
import com.regisk.legacy.view.RegiskDialog

class ManagerInstallDialog : MarkDownDialog() {

    override suspend fun getMarkdownText(): String {
        return Info.remote.regisk.note
    }

    override fun build(dialog: RegiskDialog) {
        super.build(dialog)
        with(dialog) {
            setCancelable(true)
            applyButton(RegiskDialog.ButtonType.POSITIVE) {
                titleRes = R.string.install
                onClick { DownloadService.start(context, Subject.Manager()) }
            }
            applyButton(RegiskDialog.ButtonType.NEGATIVE) {
                titleRes = android.R.string.cancel
            }
        }
    }

}
