package com.regisk.legacy.di

import okhttp3.Dns
import okhttp3.OkHttpClient
import java.net.InetAddress

private class DnsResolver : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return Dns.SYSTEM.lookup(hostname)
    }
}
