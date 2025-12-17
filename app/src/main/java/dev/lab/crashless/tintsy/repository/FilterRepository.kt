package dev.lab.crashless.tintsy.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lab.crashless.tintsy.model.FilterInfo
import dev.lab.crashless.tintsy.model.FilterSample
import dev.lab.crashless.tintsy.util.OpenCVUtil
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import androidx.core.graphics.scale

class FilterRepository @Inject constructor(
    @ApplicationContext val context: Context,
    val openCVUtil: OpenCVUtil,
    defaultDispatcher: CoroutineDispatcher
) :
    AbstractRepository<List<FilterSample>>(defaultDispatcher) {

    suspend fun loadFilteredImageSamples(uri: Uri, targetWidth: Int): Result<List<FilterSample>> {
        return execute {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                decodeImage(uri, targetWidth)
            } else {
                decodeImageLegacy(uri, targetWidth)
            }
            getFilters().map { filterInfo ->
                val bitmapCopy = bitmap.copy(bitmap.config!!, true)
                openCVUtil.applyFilterNative(bitmapCopy, filterInfo.id)
                FilterSample(bitmapCopy, filterInfo, false)
            }
        }
    }

    // apply a list of filter names
    fun getFilters() = listOf(
        FilterInfo(0, "Gray"),
        FilterInfo(1, "Blur"),
        FilterInfo(3, "Vintage"),
        FilterInfo(4, "Shadow"),
        FilterInfo(5, "Grain"),
        FilterInfo(6, "Film")
    )

    @RequiresApi(Build.VERSION_CODES.P)
    fun decodeImage(uri: Uri, targetWidth: Int): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, source ->
            val aspectRatio = info.size.width * 1.0 / info.size.height
            val targetHeight = (targetWidth / aspectRatio).toInt()
            decoder.setTargetSize(targetWidth, targetHeight)
            decoder.isMutableRequired = true
        }
    }

    @Suppress("DEPRECATION")
    private fun decodeImageLegacy(
        uri: Uri,
        targetWidth: Int
    ): Bitmap {
        val original = MediaStore.Images.Media.getBitmap(
            context.contentResolver,
            uri
        )

        val aspectRatio = original.width.toFloat() / original.height
        val targetHeight = (targetWidth / aspectRatio).toInt()

        return original.scale(targetWidth, targetHeight)
    }
}