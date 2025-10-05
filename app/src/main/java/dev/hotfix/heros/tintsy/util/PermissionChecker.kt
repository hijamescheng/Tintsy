package dev.hotfix.heros.tintsy.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionChecker {

    fun checkIfPermissionGranted(permission: String, context: Context): Boolean = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

}