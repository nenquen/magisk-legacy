package com.regisk.legacy.events.dialog

import com.regisk.legacy.arch.ActivityExecutor
import com.regisk.legacy.arch.BaseUIActivity
import com.regisk.legacy.arch.ViewEvent
import com.regisk.legacy.view.RegiskDialog

abstract class DialogEvent : ViewEvent(), ActivityExecutor {

    protected lateinit var dialog: RegiskDialog

    override fun invoke(activity: BaseUIActivity<*, *>) {
        dialog = RegiskDialog(activity)
            .apply { setOwnerActivity(activity) }
            .apply(this::build).reveal()
    }

    abstract fun build(dialog: RegiskDialog)

}

typealias GenericDialogListener = () -> Unit
