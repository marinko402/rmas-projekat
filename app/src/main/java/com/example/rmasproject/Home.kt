package com.example.rmasproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.tooling.preview.Preview
import com.example.rmasproject.ui.theme.RMASProjectTheme

@Composable
fun Home(){
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier= Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Text(text = "Home")
        }
    }
}

/*@Preview(
    showSystemUi = true,
    showBackground = true,
    //uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun HomePreview() {
    AuthViewModel.isPreviewMode = true
    RMASProjectTheme {
        Home(
            modifier = Modifier,
            navController = NavController(LocalContext.current),
            authViewModel = MockAuthViewModel(),
            userViewModel = MockUserViewModel(),
            placeViewModel = MockPlaceViewModel()
        )
    }
}*/