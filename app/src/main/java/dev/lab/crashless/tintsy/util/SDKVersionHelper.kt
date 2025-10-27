package dev.lab.crashless.tintsy.util

import android.os.Build

class SDKVersionHelper {

    fun isAtLeastSnowCone(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isBetween(start: Int, end: Int): Boolean = Build.VERSION.SDK_INT >= start && Build.VERSION.SDK_INT <= end
}