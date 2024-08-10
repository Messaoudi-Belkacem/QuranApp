package com.example.quranapp.screen.permission

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.quranapp.R
import com.example.quranapp.navigation.Screen
import com.example.quranapp.screen.PermissionDeniedDialog
import com.example.quranapp.util.addPaddingValues

@Composable
fun PermissionScreen(
    innerPadding: PaddingValues,
    navHostController: NavHostController
) {
    val tag = "PermissionScreen.kt"
    val newPadding = addPaddingValues(innerPadding, PaddingValues(24.dp))
    val isSystemInDarkTheme = isSystemInDarkTheme()
    var showDialog by remember { mutableStateOf(false) }

    // Request Permission Launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            navHostController.navigate(Screen.HomeRoute.route)
        } else {
            Log.d(tag, "Permission is denied")
            showDialog = true
        }
    }

    if (showDialog) {
        PermissionDeniedDialog(onDismiss = { showDialog = false })
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(newPadding)
    ) {
        Column {
            Text(
                text = "Access Required",
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We need your permission",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
        Image(
            painter = painterResource(
                if (isSystemInDarkTheme) R.drawable.permission_screen_illustration_dark
                else R.drawable.permission_screen_illustration
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
        )
        Column {
            Text(
                text = "To provide you with the best experience, we need access to your device's storage." +
                       "This allows us to save and retrieve your files, images, and other data, ensuring smooth functionality",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(64.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            ) {
                Text(
                    text = "Allow Access",
                    fontSize = 14.sp,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = { /*TODO*/ }
            ) {
                Text(
                    text = "Deny",
                    fontSize = 14.sp,
                )
            }
        }
    }
}