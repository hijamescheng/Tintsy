package dev.lab.crashless.tintsy.usecase

import android.graphics.Bitmap
import dev.lab.crashless.tintsy.util.OpenCVUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplyFilterUseCase @Inject constructor(val openCVUtil: OpenCVUtil) {

    suspend fun applyFilter(filterId: Int, bitmap: Bitmap): Bitmap {
        openCVUtil.applyFilterNative(bitmap, filterId)
        return bitmap
    }
}