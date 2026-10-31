package com.zubair.social.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Phone ke paas internet-capable network hai? (Wi-Fi/mobile data on, aur Android ne usay "validated" mana.) */
fun Context.isOnline(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
