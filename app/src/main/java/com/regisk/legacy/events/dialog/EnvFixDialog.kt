package com.regisk.legacy.events.dialog

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.regisk.legacy.R
import com.regisk.legacy.core.base.BaseActivity
import com.regisk.legacy.core.tasks.RegiskInstaller
import com.regisk.legacy.view.RegiskDialog

class EnvFixDialog : DialogEvent() {

    override fun build(dialog: RegiskDialog) = dialog
        .applyTitle(R.string.env_fix_title)
        .applyMessage(R.string.env_fix_msg)
        .applyButton(RegiskDialog.ButtonType.POSITIVE) {
            titleRes = android.R.string.ok
            preventDismiss = true
            onClick {
                dialog.applyTitle(R.string.setup_title)
                    .applyMessage(R.string.setup_msg)
                    .resetButtons()
                    .cancellable(false)
                (dialog.ownerActivity as BaseActivity).lifecycleScope.launch {
                    RegiskInstaller.FixEnv {
                        dialog.dismiss()
                    }.exec()
                }
            }
        }
        .applyButton(RegiskDialog.ButtonType.NEGATIVE) {
            titleRes = android.R.string.cancel
        }
        .let { }

    companion object {
        const val DISMISS = "com.regisk.legacy.ENV_DONE"
    }
}
