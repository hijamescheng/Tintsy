package dev.lab.crashless.tintsy.usecase

import android.net.Uri
import dev.lab.crashless.tintsy.model.FilterSample
import dev.lab.crashless.tintsy.repository.FilterRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFiltersUseCase @Inject constructor(val filterRepository: FilterRepository) {

    suspend fun getFilterPreviews(uri: Uri, width: Int): Result<List<FilterSample>> {
        return filterRepository.loadFilteredImageSamples(uri, width)
    }
}