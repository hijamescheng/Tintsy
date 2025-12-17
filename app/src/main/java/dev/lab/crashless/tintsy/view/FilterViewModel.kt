package dev.lab.crashless.tintsy.view

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lab.crashless.tintsy.model.FilterSample
import dev.lab.crashless.tintsy.usecase.ApplyFilterUseCase
import dev.lab.crashless.tintsy.usecase.GetFiltersUseCase
import dev.lab.crashless.tintsy.view.navigation.NavigationEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    val getFiltersUseCase: GetFiltersUseCase,
    val applyFilterUseCase: ApplyFilterUseCase,
    val appContext: Application
) : AndroidViewModel(appContext) {

    private val _filterSamples = MutableStateFlow<List<FilterSample>>(emptyList())
    val filterSamples = _filterSamples

    private val _selectedImage = MutableStateFlow<Bitmap?>(null)
    val selectedImage: StateFlow<Bitmap?> = _selectedImage

    private val _navigationEventFlow = MutableSharedFlow<NavigationEvent>()
    val navigationEventFlow: SharedFlow<NavigationEvent> = _navigationEventFlow

    var selectedBitmap: Bitmap? = null
    var selectedFilterId: Int = -1

    fun loadFilterSamples(uri: Uri) {
        viewModelScope.launch {
            val result = getFiltersUseCase.getFilterPreviews(uri, 200)
            if (result.isSuccess) {
                _filterSamples.value = result.getOrElse { emptyList() }
            }
        }
    }

    fun loadBitmapFromUri(uri: Uri) {
        viewModelScope.launch {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                decodeWithImageDecoder(uri)
            } else {
                decodeWithMediaStore(uri)
            }
            if (selectedBitmap == null) selectedBitmap = bitmap
            _selectedImage.emit(bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun decodeWithImageDecoder(uri: Uri): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(appContext.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, source ->
                decoder.isMutableRequired = true
            }
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun decodeWithMediaStore(
        uri: Uri
    ): Bitmap? {
        return try {
            MediaStore.Images.Media.getBitmap(
                appContext.contentResolver,
                uri
            )
        } catch (e: Exception) {
            null
        }
    }

    fun onSelectFilter(id: Int) {
        applyFilter(id)
        _filterSamples.update { list ->
            list.map { item ->
                if (item.filterInfo.id == id) {
                    item.copy(isSelected = true)
                } else {
                    item.copy(isSelected = false)
                }
            }
        }
        selectedFilterId = id
    }

    fun applyFilter(filterId: Int) {
        if (filterId == selectedFilterId) return
        viewModelScope.launch {
            selectedBitmap?.let {
                val bitmapCopy = it.copy(it.config!!, true)
                _selectedImage.emit(applyFilterUseCase.applyFilter(filterId, bitmapCopy))
            }
        }
    }

    fun saveBitMapToGalleryCompact() {
        viewModelScope.launch(Dispatchers.IO) {
            val filename = generateFileName()

            try {
                var outputStream: OutputStream? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ → Scoped Storage
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/Tintsy"
                        )
                    }

                    val uri = appContext.contentResolver.insert(
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                        values
                    )
                    uri?.let { outputStream = appContext.contentResolver.openOutputStream(uri) }

                } else {
                    // Android 8–9 → Legacy external storage
                    val picturesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val tintsyDir = File(picturesDir, "Tintsy")
                    if (!tintsyDir.exists()) tintsyDir.mkdirs()

                    val file = File(tintsyDir, filename)
                    outputStream = FileOutputStream(file)

                    // Optional: notify MediaScanner about the new file
                    appContext.sendBroadcast(
                        android.content.Intent(
                            android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            android.net.Uri.fromFile(file)
                        )
                    )
                }

                outputStream?.use {
                    selectedImage.value?.compress(Bitmap.CompressFormat.JPEG, 95, it)
                    _navigationEventFlow.emit(NavigationEvent.NavigateToSuccess)
                }
            } catch (e: Exception) {

            }
        }
    }

    fun generateFileName(): String = "${fileNamePrefix}_${UUID.randomUUID()}.jpg"

    companion object {
        const val dirctory = "/Tintsy"
        const val fileNamePrefix = "Tintsy"
    }
}