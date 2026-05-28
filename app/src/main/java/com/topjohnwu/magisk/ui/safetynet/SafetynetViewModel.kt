package com.topjohnwu.magisk.ui.safetynet

import android.os.Build
import androidx.databinding.Bindable
import com.topjohnwu.magisk.BR
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.arch.BaseViewModel
import com.topjohnwu.magisk.utils.set
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SafetyNetResult(
    val response: SafetyNetResponse? = null,
    val dismiss: Boolean = false
)

class SafetynetViewModel : BaseViewModel() {

    @get:Bindable
    var safetyNetTitle = R.string.empty
        set(value) = set(value, field, { field = it }, BR.safetyNetTitle)

    @get:Bindable
    var ctsState = false
        set(value) = set(value, field, { field = it }, BR.ctsState)

    @get:Bindable
    var basicIntegrityState = false
        set(value) = set(value, field, { field = it }, BR.basicIntegrityState)

    @get:Bindable
    var evalType = ""
        set(value) = set(value, field, { field = it }, BR.evalType)

    @get:Bindable
    var isChecking = false
        set(value) = set(value, field, { field = it }, BR.checking)

    @get:Bindable
    var isSuccess = false
        set(value) = set(value, field, { field = it }, BR.success, BR.textColorAttr)

    @get:Bindable
    var timestampString = ""
        set(value) = set(value, field, { field = it }, BR.timestampString)

    @get:Bindable
    var apkDigestString = ""
        set(value) = set(value, field, { field = it }, BR.apkDigestString)

    @get:Bindable
    var hasResult = false
        set(value) = set(value, field, { field = it }, BR.hasResult)

    val deviceModel: String = "${Build.MODEL} (${Build.DEVICE})"
    val androidVersion: String = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    val securityPatch: String = Build.VERSION.SECURITY_PATCH

    @get:Bindable
    val textColorAttr get() = if (isSuccess) R.attr.colorPrimary else R.attr.colorError

    init {
        cachedResult?.also {
            handleResult(SafetyNetResult(it))
        }
    }

    private fun attest() {
        isChecking = true
        CheckSafetyNetEvent(::handleResult).publish()
    }

    fun reset() = attest()

    private fun handleResult(result: SafetyNetResult) {
        isChecking = false

        if (result.dismiss) {
            back()
            return
        }

        result.response?.apply {
            cachedResult = this
            if (this === INVALID_RESPONSE) {
                isSuccess = false
                ctsState = false
                basicIntegrityState = false
                evalType = "N/A"
                safetyNetTitle = R.string.safetynet_res_invalid
                hasResult = true
                timestampString = ""
                apkDigestString = ""
            } else {
                val success = ctsProfileMatch && basicIntegrity
                isSuccess = success
                ctsState = ctsProfileMatch
                basicIntegrityState = basicIntegrity
                evalType = if (evaluationType.contains("HARDWARE")) "HARDWARE_BACKED" else "BASIC"
                safetyNetTitle =
                    if (success) R.string.safetynet_attest_success
                    else R.string.safetynet_attest_failure
                hasResult = true
                
                if (timestampMs > 0) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    timestampString = sdf.format(Date(timestampMs))
                } else {
                    timestampString = ""
                }
                apkDigestString = apkCertificateDigestSha256.firstOrNull() ?: ""
            }
        } ?: run {
            isSuccess = false
            ctsState = false
            basicIntegrityState = false
            evalType = "N/A"
            safetyNetTitle = R.string.safetynet_api_error
            hasResult = false
            timestampString = ""
            apkDigestString = ""
        }
    }

    companion object {
        private var cachedResult: SafetyNetResponse? = null
    }

}
