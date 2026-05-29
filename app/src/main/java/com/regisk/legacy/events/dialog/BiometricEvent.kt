package com.regisk.legacy.events.dialog

import com.regisk.legacy.arch.ActivityExecutor
import com.regisk.legacy.arch.BaseUIActivity
import com.regisk.legacy.arch.ViewEvent
import com.regisk.legacy.core.utils.BiometricHelper

class BiometricEvent(
    builder: Builder.() -> Unit
) : ViewEvent(), ActivityExecutor {

    private var listenerOnFailure: GenericDialogListener = {}
    private var listenerOnSuccess: GenericDialogListener = {}

    init {
        builder(Builder())
    }

    override fun invoke(activity: BaseUIActivity<*, *>) {
        BiometricHelper.authenticate(
            activity,
            onError = listenerOnFailure,
            onSuccess = listenerOnSuccess
        )
    }

    inner class Builder internal constructor() {

        fun onFailure(listener: GenericDialogListener) {
            listenerOnFailure = listener
        }

        fun onSuccess(listener: GenericDialogListener) {
            listenerOnSuccess = listener
        }
    }

}
