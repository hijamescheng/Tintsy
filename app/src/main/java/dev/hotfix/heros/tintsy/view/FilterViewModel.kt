package dev.hotfix.heros.tintsy.view

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.hotfix.heros.tintsy.model.FilterSample
import dev.hotfix.heros.tintsy.usecase.ApplyFilterUseCase
import dev.hotfix.heros.tintsy.usecase.GetFiltersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            val bitmap = try {
                val source = ImageDecoder.createSource(appContext.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, source ->
                    decoder.isMutableRequired = true
                }
            } catch (e: Exception) {
                null
            }
            if (selectedBitmap == null) selectedBitmap = bitmap
            _selectedImage.emit(bitmap)
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

    fun saveBitmapToGallery() {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, generateFileName())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + dirctory
            ) // custom folder
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = appContext.contentResolver
        val imageUri: Uri? =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val bitmapToSave = selectedImage.value

        bitmapToSave?.let {
            imageUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmapToSave.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }
    }

    fun generateFileName(): String = "${fileNamePrefix}_${UUID.randomUUID()}.jpg"

    companion object {
        const val dirctory = "/Tintsy"
        const val fileNamePrefix = "Tintsy"
    }
}