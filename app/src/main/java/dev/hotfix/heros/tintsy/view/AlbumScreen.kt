package dev.hotfix.heros.tintsy.view

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.hotfix.heros.tintsy.ui.theme.Background
import dev.hotfix.heros.tintsy.ui.theme.Blue
import dev.hotfix.heros.tintsy.ui.theme.getTopAppBarColors

@Composable
fun MainScreen(
    headerUri: Uri,
    screenState: AlbumViewModel.ScreenState,
    shouldShowRationale: Boolean,
    onImageSelected: (Uri) -> Unit,
    onNext: () -> Unit,
    onSettings: () -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!shouldShowRationale) TopAppBar(onNext)
        },
    ) { innerPadding ->
        if (!shouldShowRationale) {
            ImageSelectionScreen(headerUri, innerPadding, screenState, onImageSelected)
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Tintsy requires read media permission to work")
                TextButton(onClick = onSettings) {
                    Text("Settings")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(onNext: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Tintsy") },
        colors = getTopAppBarColors(),
        actions = {
            TextButton(onClick = onNext) { Text("NEXT", color = Blue) }
        })
}

@Composable
fun getScreenHeightDp(): Dp {
    // Access the current configuration provided by the CompositionLocal
    val configuration = LocalConfiguration.current

    // Return the screen height in Dp
    return configuration.screenHeightDp.dp
}

@Composable
fun HeaderView(headerUri: Uri) {
    Box(
        modifier = Modifier.height(getScreenHeightDp() / 2),
        contentAlignment = Alignment.Center
    ) {
        Text(text = headerUri.toString(), color = Color.Transparent)
        AsyncImage(
            model = headerUri,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "",
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun ImageSelectionScreen(
    headerUri: Uri,
    padding: PaddingValues,
    screenSate: AlbumViewModel.ScreenState,
    onImageSelected: (uri: Uri) -> Unit
) {
    val span: (LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(4) }
    LazyVerticalGrid(
        modifier = Modifier
            .padding(top = padding.calculateTopPadding())
            .fillMaxSize(),
        columns = GridCells.Fixed(4)
    ) {
        item(span = span) {
            HeaderView(headerUri)
        }

        stickyHeader {
            StickyHeader()
        }

        itemsIndexed(items = screenSate.imageList) { index, uri ->
            ImageItemCell(uri, index) { uri ->
                onImageSelected(uri)
            }
        }
    }
}

@Composable
fun StickyHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(color = Background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Album >",
            modifier = Modifier.padding(start = 16.dp),
            color = Color.White,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun ImageItemCell(
    uri: Uri,
    index: Int,
    onItemClicked: (Uri) -> Unit
) {
    Column {
        Row(modifier = Modifier.clickable {
            onItemClicked(uri)
        }) {
            AsyncImage(
                model = uri,
                contentScale = ContentScale.Crop,
                contentDescription = "",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
            if (index % 4 != 0) VerticalDivider(color = Background)
        }
        HorizontalDivider(color = Background, thickness = 2.dp)
    }
}