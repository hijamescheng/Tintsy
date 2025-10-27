package dev.lab.crashless.tintsy.view

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.lab.crashless.tintsy.R
import dev.lab.crashless.tintsy.ui.theme.getTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSaveSuccessfully(onClose: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                colors = getTopAppBarColors(),
                actions = {
                    IconButton(onClose) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Close"
                        )
                    }
                })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.success),
                contentDescription = "success",
                modifier = Modifier.size(80.dp)
            )
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = "Image saved, find it in your album"
            )
            val context = LocalContext.current
            Text(
                "open album",
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    openGalleryAppButton(context)
                })
        }
    }
}

private fun openGalleryAppButton(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        type = "image/*"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No gallery app found", Toast.LENGTH_SHORT).show()
    }
}
