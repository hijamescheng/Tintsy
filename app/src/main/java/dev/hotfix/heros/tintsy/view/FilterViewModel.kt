package dev.hotfix.heros.tintsy.view

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hotfix.heros.tintsy.model.FilterSample
import dev.hotfix.heros.tintsy.usecase.GetFiltersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    val getFiltersUseCase: GetFiltersUseCase
) : ViewModel() {

    private val _filterSamples = MutableStateFlow<List<FilterSample>>(emptyList())
    val filterSamples = _filterSamples

    fun loadFilterSamples(uri: Uri) {
        viewModelScope.launch {
            val result = getFiltersUseCase.getFilterPreviews(uri, 200)
            if (result.isSuccess) {
                _filterSamples.value = result.getOrElse { emptyList() }
            }
        }
    }

    fun onSelectFilter(id: Int) {
        _filterSamples.update { list ->
            list.map { item ->
                if (item.filterInfo.id == id) {
                    item.copy(isSelected = true)
                } else {
                    item.copy(isSelected = false)
                }
            }
        }
    }

    data class ImageFilter(val id: String, val name: String, val isSelected: Boolean)
}