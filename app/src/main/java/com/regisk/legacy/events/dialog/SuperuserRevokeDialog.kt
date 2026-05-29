package com.regisk.legacy.events.dialog

import com.regisk.legacy.R
import com.regisk.legacy.view.RegiskDialog

class SuperuserRevokeDialog(
    builder: Builder.() -> Unit
) : DialogEvent() {

    private val callbacks = Builder().apply(builder)

    override fun build(dialog: RegiskDialog) {
        dialog.applyTitle(R.string.su_revoke_title)
            .applyMessage(R.string.su_revoke_msg, callbacks.appName)
            .applyButton(RegiskDialog.ButtonType.POSITIVE) {
                titleRes = android.R.string.ok
                onClick { callbacks.listenerOnSuccess() }
            }
            .applyButton(RegiskDialog.ButtonType.NEGATIVE) {
                titleRes = android.R.string.cancel
            }
    }

    inner class Builder internal constructor() {
        var appName: String = ""

        internal var listenerOnSuccess: GenericDialogListener = {}

        fun onSuccess(listener: GenericDialogListener) {
            listenerOnSuccess = listener
        }
    }
}
