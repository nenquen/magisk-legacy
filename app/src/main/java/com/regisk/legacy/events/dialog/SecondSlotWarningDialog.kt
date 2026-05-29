package com.regisk.legacy.events.dialog

import com.regisk.legacy.R
import com.regisk.legacy.view.RegiskDialog

class SecondSlotWarningDialog : DialogEvent() {

    override fun build(dialog: RegiskDialog) {
        dialog.applyTitle(android.R.string.dialog_alert_title)
            .applyMessage(R.string.install_inactive_slot_msg)
            .applyButton(RegiskDialog.ButtonType.POSITIVE) {
                titleRes = android.R.string.ok
            }
            .cancellable(true)
    }
}
