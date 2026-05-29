package com.regisk.legacy.core.base

import android.app.Service
import android.content.Context
import com.regisk.legacy.core.wrap

abstract class BaseService : Service() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.wrap())
    }
}
