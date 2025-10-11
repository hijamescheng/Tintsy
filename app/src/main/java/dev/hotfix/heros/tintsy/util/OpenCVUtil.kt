package dev.hotfix.heros.tintsy.util

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenCVUtil @Inject constructor(){

    init {
        System.loadLibrary("tintsy") // your .so name
    }

    external fun applyGrayScaleFilter(inputPath: String, outputPath: String)
    external fun applyFilterNative(bitmap: Bitmap, filterType: Int)
}