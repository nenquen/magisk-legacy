package com.regisk.legacy.data.repository

import com.regisk.legacy.core.Const
import com.regisk.legacy.core.model.su.SuLog
import com.regisk.legacy.data.database.SuLogDao
import com.regisk.legacy.ktx.await
import com.topjohnwu.superuser.Shell


class LogRepository(
    private val logDao: SuLogDao
) {

    suspend fun fetchSuLogs() = logDao.fetchAll()

    suspend fun fetchRegiskLogs(): String {
        val list = object : AbstractMutableList<String>() {
            val buf = StringBuilder()
            override val size get() = 0
            override fun get(index: Int): String = ""
            override fun removeAt(index: Int): String = ""
            override fun set(index: Int, element: String): String = ""
            override fun add(index: Int, element: String) {
                if (element.isNotEmpty()) {
                    buf.append(element)
                    buf.append('\n')
                }
            }
        }
        Shell.su("cat ${Const.REGISK_LOG}").to(list).await()
        return list.buf.toString()
    }

    suspend fun clearLogs() = logDao.deleteAll()

    fun clearRegiskLogs(cb: (Shell.Result) -> Unit) =
        Shell.su("echo -n > ${Const.REGISK_LOG}").submit(cb)

    suspend fun insert(log: SuLog) = logDao.insert(log)

}
