package dev.hotfix.heros.tintsy.usecase

import android.graphics.Bitmap
import dev.hotfix.heros.tintsy.util.OpenCVUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplyFilterUseCase @Inject constructor(val openCVUtil: OpenCVUtil) {

    suspend fun applyFilter(filterId: Int, bitmap: Bitmap): Bitmap {
        openCVUtil.applyFilterNative(bitmap, filterId)
        return bitmap
    }
}