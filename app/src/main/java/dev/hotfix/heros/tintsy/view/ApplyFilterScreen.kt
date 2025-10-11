package dev.hotfix.heros.tintsy.view

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.hotfix.heros.tintsy.ui.theme.BottomSheetBG
import dev.hotfix.heros.tintsy.ui.theme.LightGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    uri: Uri,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    val filterViewModel: FilterViewModel = hiltViewModel()
    val filterList by filterViewModel.filterList.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            FilterList(
                list = filterList,
                onCancel = onCancel,
                onDone = onDone,
                onFilterClicked = { id ->
                    filterViewModel.onSelectFilter(id)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(top = innerPadding.calculateTopPadding(), bottom = 300.dp)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = uri,
                contentScale = ContentScale.Fit,
                contentDescription = "",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FilterList(
    list: List<FilterViewModel.ImageFilter>,
    onFilterClicked: (id: String) -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .fillMaxWidth()
            .background(BottomSheetBG)
    ) {
        LazyRow(
            modifier = Modifier
                .padding(vertical = 50.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(items = list, key = { item -> item.id }) { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.name, style = TextStyle(fontSize = 14.sp), modifier = Modifier.padding(bottom = 8.dp))
                    Box(
                        modifier = Modifier
                            .clickable { onFilterClicked(item.id) }
                            .width(80.dp)
                            .height(80.dp)
                            .then(
                                if (item.isSelected) {
                                    Modifier
                                        .border(
                                            border = BorderStroke(1.dp, Color.White),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                } else {
                                    Modifier.clip(
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            )
                            .background(Color.Blue)
                    )
                }
            }
        }
        HorizontalDivider(color = LightGrey)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onCancel) { Text("Cancel") }
            TextButton(onDone) { Text("Done") }
        }
    }
}