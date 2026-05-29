package com.regisk.legacy.core.download

import android.content.Context
import androidx.core.net.toFile
import com.regisk.legacy.DynAPK
import com.regisk.legacy.R
import com.regisk.legacy.core.Info
import com.regisk.legacy.core.isRunningAsStub
import com.regisk.legacy.core.tasks.HideAPK
import com.regisk.legacy.core.utils.MediaStoreUtils.outputStream
import com.regisk.legacy.ktx.relaunchApp
import com.regisk.legacy.ktx.withStreams
import com.regisk.legacy.ktx.writeTo
import java.io.File
import java.io.InputStream
import java.io.OutputStream

private fun Context.patch(apk: File) {
    val patched = File(apk.parent, "patched.apk")
    HideAPK.patch(this, apk, patched, packageName, applicationInfo.nonLocalizedLabel)
    apk.delete()
    patched.renameTo(apk)
}

private fun BaseDownloader.notifyHide(id: Int) {
    update(id) {
        it.setProgress(0, 0, true)
            .setContentTitle(getString(R.string.hide_app_title))
            .setContentText("")
    }
}

private class DupOutputStream(
    private val o1: OutputStream,
    private val o2: OutputStream
) : OutputStream() {
    override fun write(b: Int) {
        o1.write(b)
        o2.write(b)
    }
    override fun write(b: ByteArray?, off: Int, len: Int) {
        o1.write(b, off, len)
        o2.write(b, off, len)
    }
    override fun close() {
        o1.close()
        o2.close()
    }
}

suspend fun BaseDownloader.handleAPK(subject: Subject.Manager, stream: InputStream) {
    fun write(output: OutputStream) {
        val ext = subject.externalFile.outputStream()
        val o = DupOutputStream(ext, output)
        withStreams(stream, o) { src, out -> src.copyTo(out) }
    }

    write(subject.file.outputStream())
}
