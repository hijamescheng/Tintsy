package dev.hotfix.heros.tintsy.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hotfix.heros.tintsy.usecase.GetFiltersUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    ioDispatcher: CoroutineDispatcher,
    getFiltersUseCase: GetFiltersUseCase
): ViewModel() {

    private val _filterList = MutableStateFlow("")
    val filterList = _filterList.map { id ->
        getFiltersUseCase.getFilters(id)
    }.flowOn(ioDispatcher).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onSelectFilter(id: String) {
        _filterList.value = id
    }
    data class ImageFilter(val id: String, val name: String, val isSelected: Boolean)
}