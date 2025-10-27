package dev.lab.crashless.tintsy.model

import android.graphics.Bitmap

data class FilterInfo(val id: Int, val name: String)
data class FilterSample(val bitmap: Bitmap, val filterInfo: FilterInfo, val isSelected: Boolean)