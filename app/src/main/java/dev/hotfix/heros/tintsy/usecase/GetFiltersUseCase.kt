package dev.hotfix.heros.tintsy.usecase

import android.net.Uri
import dev.hotfix.heros.tintsy.model.FilterSample
import dev.hotfix.heros.tintsy.repository.FilterRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFiltersUseCase @Inject constructor(val filterRepository: FilterRepository) {

    suspend fun getFilterPreviews(uri: Uri, width: Int): Result<List<FilterSample>> {
        return filterRepository.loadFilteredImageSamples(uri, width)
    }
}