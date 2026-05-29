package com.regisk.legacy.events

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.regisk.legacy.arch.ActivityExecutor
import com.regisk.legacy.arch.BaseUIActivity
import com.regisk.legacy.arch.ViewEvent
import com.regisk.legacy.utils.TextHolder
import com.regisk.legacy.utils.asText

class SnackbarEvent constructor(
    private val msg: TextHolder,
    private val length: Int = Snackbar.LENGTH_SHORT,
    private val builder: Snackbar.() -> Unit = {}
) : ViewEvent(), ActivityExecutor {

    constructor(
        @StringRes res: Int,
        length: Int = Snackbar.LENGTH_SHORT,
        builder: Snackbar.() -> Unit = {}
    ) : this(res.asText(), length, builder)

    constructor(
        msg: String,
        length: Int = Snackbar.LENGTH_SHORT,
        builder: Snackbar.() -> Unit = {}
    ) : this(msg.asText(), length, builder)


    private fun snackbar(
        view: View,
        message: String,
        length: Int,
        builder: Snackbar.() -> Unit
    ) = Snackbar.make(view, message, length).apply(builder).show()

    override fun invoke(activity: BaseUIActivity<*, *>) {
        snackbar(activity.snackbarView,
            msg.getText(activity.resources).toString(),
            length, builder)
    }
}
