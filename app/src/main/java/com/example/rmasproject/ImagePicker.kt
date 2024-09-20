package com.example.rmasproject

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ImagePicker(
    navController: NavController,
    modifier: Modifier,
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imgUri = Uri.EMPTY

    val tempUri = remember {
        mutableStateOf<Uri?>(null)
    }

    fun getTempUri(): Uri? {
        return try {
            val imagesDir = File(context.cacheDir, "images")
            imagesDir.mkdirs()
            val file = File.createTempFile(
                "image_" + System.currentTimeMillis().toString(),
                ".jpg",
                imagesDir
            )
            FileProvider.getUriForFile(
                context,
                context.getString(R.string.fileprovider),
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            it?.let {
                it.let { uri ->
                    imgUri = uri
                }
            }
        }
    )

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = {isSaved ->
            if (isSaved) {
                tempUri.value?.let { uri ->
                    imgUri = uri
                }
            } else {
                Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val tmpUri = getTempUri()
            tempUri.value = tmpUri
            takePhotoLauncher.launch(tempUri.value!!)
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    if (showBottomSheet){
        MyModalBottomSheet(
            onDismiss = {
                showBottomSheet = false
            },
            onTakePhotoClick = {
                showBottomSheet = false

                val permission = Manifest.permission.CAMERA
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                ) {
                    val tmpUri = getTempUri()
                    tempUri.value = tmpUri
                    takePhotoLauncher.launch(tempUri.value!!)
                } else {
                    cameraPermissionLauncher.launch(permission)
                }
            },
            onPhotoGalleryClick = {
                showBottomSheet = false
                imagePicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedButton(
            onClick = { showBottomSheet = true },
            modifier = modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Upload/Take photo"
            )
        }

        imgUri.let {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = it,
                    modifier = modifier.size(
                        200.dp
                    ),
                    contentDescription = "photo",
                )
            }
        }

        Row(
            modifier = modifier.align(Alignment.CenterHorizontally)
        ) {
            /*Button(
                onClick = onBackStep
            ) {
                Text(
                    text = "Back"
                )
            }*/
        }


    }
}
@Composable
fun MyModalBottomSheet(
    onDismiss: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onPhotoGalleryClick: () -> Unit
) {
    MyModalBottomSheetContent(
        header = "Choose an action",
        onDismiss = {
            onDismiss.invoke()
        },
        items = listOf(
            BottomSheetItem(
                title = "Take Photo",
                icon = Icons.Outlined.CameraAlt,
                onClick = {
                    onTakePhotoClick.invoke()
                }
            ),
            BottomSheetItem(
                title = "Select Photo",
                icon = Icons.Default.Folder,
                onClick = {
                    onPhotoGalleryClick.invoke()
                }
            ),
        )
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyModalBottomSheetContent(
    onDismiss: () -> Unit,
    header: String = "Choose an action",
    items: List<BottomSheetItem> = listOf(),
) {
    val skipPartiallyExpanded by remember {
        mutableStateOf(false)
    }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val edgeToEdgeEnabled by remember {
        mutableStateOf(false)
    }
    val windowInsets = if (edgeToEdgeEnabled)
        WindowInsets(0) else BottomSheetDefaults.windowInsets

    ModalBottomSheet(
        shape = MaterialTheme.shapes.medium.copy(
            bottomStart = CornerSize(0),
            bottomEnd = CornerSize(0)
        ),
        onDismissRequest = { onDismiss.invoke() },
        sheetState = bottomSheetState,
        windowInsets = windowInsets
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                text = header,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            items.forEach {item ->
                androidx.compose.material3.ListItem(
                    modifier = Modifier.clickable {
                        item.onClick.invoke()
                    },
                    headlineContent = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    },
                )
            }
        }
    }
}

data class BottomSheetItem(
    val title: String = "",
    val icon: ImageVector,
    val onClick: () -> Unit
)