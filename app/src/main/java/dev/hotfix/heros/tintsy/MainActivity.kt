package dev.hotfix.heros.tintsy

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.hotfix.heros.tintsy.ui.theme.TintsyTheme
import dev.hotfix.heros.tintsy.util.PermissionChecker
import dev.hotfix.heros.tintsy.view.AlbumViewModel
import dev.hotfix.heros.tintsy.view.FilterScreen
import dev.hotfix.heros.tintsy.view.MainScreen
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader

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

    companion object {
        init {
            //System.loadLibrary("imagefilter")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (OpenCVLoader.initLocal()) {
            Log.d("OpenCV library loaded", "success")
        } else {
            Log.d("OpenCV library load failed", "fail")
        }
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
                val shouldShowRationale by viewModel.shouldShowPermissionRationale.collectAsStateWithLifecycle()
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                val headerUri by viewModel.headerImageUri.collectAsStateWithLifecycle()

                MainScreen(
                    headerUri = headerUri,
                    screenState = screenState,
                    shouldShowRationale = shouldShowRationale,
                    onImageSelected = { uri -> viewModel.onImageSelected(uri) },
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
}
