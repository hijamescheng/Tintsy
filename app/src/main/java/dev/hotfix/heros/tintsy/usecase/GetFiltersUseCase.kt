package dev.hotfix.heros.tintsy.usecase

import android.net.Uri
import coil3.Bitmap
import dev.hotfix.heros.tintsy.model.FilterSample
import dev.hotfix.heros.tintsy.repository.FilterRepository
import dev.hotfix.heros.tintsy.view.FilterViewModel.ImageFilter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFiltersUseCase @Inject constructor(val filterRepository: FilterRepository) {

    suspend fun getFilterPreviews(uri: Uri, width: Int): Result<List<FilterSample>> {
        return filterRepository.loadFilteredImageSamples(uri, width)
    }

    fun getFilters(id: String): List<ImageFilter> {
        return getFilterList().map {
            if (it.id == id || (id.isEmpty() && it.id == "origin")) {
                it.copy(isSelected = true)
            } else {
                it.copy(isSelected = false)
            }
        }
    }

    fun getFilterList(): List<ImageFilter> {
        return listOf(
            ImageFilter(id = "origin", name = "Original", isSelected = true),
            ImageFilter(id = "grey", name = "Grey", isSelected = false),
            ImageFilter(id = "blur", name = "Blur", isSelected = false),
            ImageFilter(id = "fade", name = "Fade", isSelected = false),
            ImageFilter(id = "fade_warm", name = "Fade warm", isSelected = false),
            ImageFilter(id = "fade_cool", name = "Fade cool", isSelected = false),
            ImageFilter(id = "simple", name = "Simple", isSelected = false),
            ImageFilter(id = "simple_warm", name = "Simple warm", isSelected = false)

        )
    }
}