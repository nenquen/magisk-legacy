package com.regisk.legacy.events.dialog

import com.regisk.legacy.R
import com.regisk.legacy.core.Info
import com.regisk.legacy.core.download.Action
import com.regisk.legacy.core.download.DownloadService
import com.regisk.legacy.core.download.Subject
import com.regisk.legacy.core.model.module.OnlineModule
import com.regisk.legacy.view.RegiskDialog

class ModuleInstallDialog(private val item: OnlineModule) : DialogEvent() {

    override fun build(dialog: RegiskDialog) {
        with(dialog) {

            fun download(install: Boolean) {
                val config = if (install) Action.Flash else Action.Download
                val subject = Subject.Module(item, config)
                DownloadService.start(context, subject)
            }

            applyTitle(context.getString(R.string.repo_install_title, item.name))
                .applyMessage(context.getString(R.string.repo_install_msg, item.downloadFilename))
                .cancellable(true)
                .applyButton(RegiskDialog.ButtonType.NEGATIVE) {
                    titleRes = R.string.download
                    icon = R.drawable.ic_download_md2
                    onClick { download(false) }
                }

            if (Info.env.isActive) {
                applyButton(RegiskDialog.ButtonType.POSITIVE) {
                    titleRes = R.string.install
                    icon = R.drawable.ic_install
                    onClick { download(true) }
                }
            }

            reveal()
        }
    }

}
