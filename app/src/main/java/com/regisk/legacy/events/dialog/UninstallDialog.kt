@file:Suppress("DEPRECATION")
package com.regisk.legacy.events.dialog

import android.app.ProgressDialog
import android.widget.Toast
import com.regisk.legacy.R
import com.regisk.legacy.arch.BaseUIActivity
import com.regisk.legacy.ui.flash.FlashFragment
import com.regisk.legacy.utils.Utils
import com.regisk.legacy.view.RegiskDialog
import com.topjohnwu.superuser.Shell

class UninstallDialog : DialogEvent() {

    override fun build(dialog: RegiskDialog) {
        dialog.applyTitle(R.string.uninstall_regisk_title)
            .applyMessage(R.string.uninstall_regisk_msg)
            .applyButton(RegiskDialog.ButtonType.POSITIVE) {
                titleRes = R.string.restore_img
                onClick { restore() }
            }
            .applyButton(RegiskDialog.ButtonType.NEGATIVE) {
                titleRes = R.string.complete_uninstall
                onClick { completeUninstall() }
            }
    }

    @Suppress("DEPRECATION")
    private fun restore() {
        val dialog = ProgressDialog(dialog.context).apply {
            setMessage(dialog.context.getString(R.string.restore_img_msg))
            show()
        }

        Shell.su("restore_imgs").submit { result ->
            dialog.dismiss()
            if (result.isSuccess) {
                Utils.toast(R.string.restore_done, Toast.LENGTH_SHORT)
            } else {
                Utils.toast(R.string.restore_fail, Toast.LENGTH_LONG)
            }
        }
    }

    private fun completeUninstall() {
        (dialog.ownerActivity as? BaseUIActivity<*, *>)
                ?.navigation?.navigate(FlashFragment.uninstall())
    }

}
