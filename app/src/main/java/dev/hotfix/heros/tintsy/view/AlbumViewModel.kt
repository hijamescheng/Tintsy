package dev.hotfix.heros.tintsy.view

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hotfix.heros.tintsy.repository.LocalMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(repository: LocalMediaRepository) : ViewModel() {

    private val _screenState = MutableStateFlow(0)
    val screenState: StateFlow<ScreenState> = _screenState.map {
        val result = repository.loadImages()
        if (result.isSuccess) {
            val list = result.getOrDefault(emptyList())
            val headerImageUri = if(list.isNotEmpty()) list[0] else Uri.EMPTY
            onImageSelected(headerImageUri)
            ScreenState(imageList = list, isError = false, isLoading = false)
        } else {
            ScreenState(imageList = emptyList(), isError = true, isLoading = false)
        }
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000),
        initialValue = ScreenState(isError = false, isLoading = true)
    )

    private val _headerImageUri = MutableStateFlow<Uri>(Uri.EMPTY)
    val headerImageUri = _headerImageUri

    private val _shouldShowPermissionRationale = MutableStateFlow(false)
    val shouldShowPermissionRationale = _shouldShowPermissionRationale

    fun onPermissionGranted() {
        _screenState.value = 1
    }

    fun onShouldShowPermissionRationale(show: Boolean) {
        _shouldShowPermissionRationale.value = show
    }

    fun onImageSelected(uri: Uri) {
        _headerImageUri.update {
            uri
        }
    }

    data class ScreenState(
        val imageList: List<Uri> = emptyList(),
        val isError: Boolean,
        val isLoading: Boolean
    )
}