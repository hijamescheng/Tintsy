package dev.hotfix.heros.tintsy

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import dev.hotfix.heros.tintsy.ui.theme.Background
import dev.hotfix.heros.tintsy.ui.theme.Blue
import dev.hotfix.heros.tintsy.ui.theme.BottomSheetBG
import dev.hotfix.heros.tintsy.ui.theme.LightGrey
import dev.hotfix.heros.tintsy.ui.theme.TintsyTheme
import dev.hotfix.heros.tintsy.ui.theme.getTopAppBarColors
import dev.hotfix.heros.tintsy.util.PermissionChecker
import dev.hotfix.heros.tintsy.view.AlbumViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: AlbumViewModel by viewModels()

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.onPermissionGranted()
                    }
                }
            } else {
                viewModel.onShouldShowPermissionRationale(true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TintsyTheme {
                TintsyNavHost()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestPhotoAlumPermission()
    }

    private fun requestPhotoAlumPermission() {
        val permissionString = Manifest.permission.READ_MEDIA_IMAGES
        val isGranted = PermissionChecker.checkIfPermissionGranted(permissionString, this)
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this, permissionString
        )
        when {
            isGranted -> {
                viewModel.onShouldShowPermissionRationale(false)
            }

            shouldShowRationale -> viewModel.onShouldShowPermissionRationale(true)
            else -> requestPermissionLauncher.launch(permissionString)
        }
    }

    @Composable
    fun TintsyNavHost() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                MainScreen(
                    onNext = { navController.navigate("filter") },
                    onSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.setData(uri)
                        startActivity(intent)
                    }
                )
            }
            composable("filter") { backStackEntry ->
                val uri by viewModel.headerImageUri.collectAsStateWithLifecycle()
                FilterScreen(uri, { navController.navigate("home") }, {})
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FilterScreen(uri: Uri, onCancel: () -> Unit, onDone: () -> Unit) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                FilterList(
                    onCancel = onCancel,
                    onDone = onDone
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
    fun FilterList(onCancel: () -> Unit, onDone: () -> Unit) {
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
                items(10) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Filter name", style = TextStyle(fontSize = 14.sp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp)) // Apply rounded corners with a radius of 16.dp
                                .background(Color.White)
                        )
                    }
                }
            }
            HorizontalDivider(color = LightGrey)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onCancel) { Text("Cancel") }
                TextButton(onDone) { Text("Done") }
            }
        }
    }

    @Composable
    fun MainScreen(onNext: () -> Unit, onSettings: () -> Unit) {
        val screenSate = viewModel.screenState.collectAsStateWithLifecycle()
        val shouldShowRationale by viewModel.shouldShowPermissionRationale.collectAsStateWithLifecycle()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!shouldShowRationale) TopAppBar(onNext)
            },
        ) { innerPadding ->
            if (!shouldShowRationale) {
                ImageSelectionScreen(innerPadding, screenSate.value)
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
    fun ImageSelectionScreen(padding: PaddingValues, screenSate: AlbumViewModel.ScreenState) {
        val span: (LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(4) }
        LazyVerticalGrid(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize(),
            columns = GridCells.Fixed(4)
        ) {
            item(span = span) {
                HeaderView()
            }

            stickyHeader {
                StickyHeader()
            }

            itemsIndexed(items = screenSate.imageList) { index, uri ->
                ImageItemCell(uri, index) { uri ->
                    viewModel.onImageSelected(uri)
                }
            }
        }
    }

    @Composable
    fun HeaderView() {
        val uri by viewModel.headerImageUri.collectAsStateWithLifecycle()
        Box(
            modifier = Modifier.height(getScreenHeightDp() / 2),
            contentAlignment = Alignment.Center
        ) {
            Text(text = uri.toString(), color = Color.Transparent)
            AsyncImage(
                model = uri,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "",
                contentScale = ContentScale.Fit
            )
        }
    }

    @Composable
    fun getScreenHeightDp(): Dp {
        // Access the current configuration provided by the CompositionLocal
        val configuration = LocalConfiguration.current

        // Return the screen height in Dp
        return configuration.screenHeightDp.dp
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
}
