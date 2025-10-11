package dev.hotfix.heros.tintsy.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.hotfix.heros.tintsy.model.FilterInfo
import dev.hotfix.heros.tintsy.model.FilterSample
import dev.hotfix.heros.tintsy.util.OpenCVUtil
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class FilterRepository @Inject constructor(
    @ApplicationContext val context: Context,
    val openCVUtil: OpenCVUtil,
    defaultDispatcher: CoroutineDispatcher
) :
    AbstractRepository<List<FilterSample>>(defaultDispatcher) {

    suspend fun loadFilteredImageSamples(uri: Uri, targetWidth: Int): Result<List<FilterSample>> {
        return execute {
            val bitmap = decodeImage(uri, targetWidth)
            getFilters().map { filterInfo ->
                val bitmapCopy = bitmap.copy(bitmap.config!!, true)
                openCVUtil.applyFilterNative(bitmapCopy, filterInfo.id)
                FilterSample(bitmapCopy, filterInfo, false)
            }
        }
    }

    // apply a list of filter names
    fun getFilters() = listOf(FilterInfo(0, "Gray"), FilterInfo(1, "Blur"))

    fun decodeImage(uri: Uri, targetWidth: Int): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, source ->
            val aspectRatio = info.size.width * 1.0 / info.size.height
            val targetHeight = (targetWidth / aspectRatio).toInt()
            decoder.setTargetSize(targetWidth, targetHeight)
            decoder.isMutableRequired = true
        }
    }
}