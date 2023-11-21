package com.example.webviewproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webviewproject.ui.theme.WebViewProjectTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // viewModel 을 받아와야함.
            val viewModel = viewModel<MainViewModel>()
            HomeScreen(viewModel = viewModel)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    // focus를 잃게 해서, 키보드가 내려갈 수 있도록 설정
    val focusManager = LocalFocusManager.current

    // 구조 분해를 사용해서, url를 remember saveable 로
    val (inputUrl, setUrl) = rememberSaveable {
        mutableStateOf("https://www.google.com")
    }

    // Scaffold에 설정할 Scaffold State 설정
    // snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text(text = "My WebBrowser") },
                actions = {
                    IconButton(onClick = {
                        // undo
                        viewModel.undo()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                            tint = Color.Black,
                        )

                    }
                    IconButton(onClick = {
                        // redo
                        viewModel.redo()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "forward",
                            tint = Color.Black,
                        )
                    }
                })
        }
    ) {
        Column(
            modifier = Modifier
                // padding it 추가해주자
                .padding(it)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = inputUrl,
                onValueChange = setUrl,
                label = { Text("https://") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    // 1. ViewModel 에 url 을 저장하고 있는데, url을 받도록 해야함.
                    // 2. webView가 다시 그려지도록 설정해줘야함. MyWebView로 이동
                    viewModel.url.value = inputUrl
                    focusManager.clearFocus()
                })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Webview 생성
            MyWebView(viewModel = viewModel, snackbarHostState = snackbarHostState)
        }
    }
}

@Composable
fun MyWebView(
    viewModel: MainViewModel, snackbarHostState: SnackbarHostState,
) {
    // foctory = 처음 화면에 표시해야하는 view 객체의 instance를 지정해주면 됨.
    // composition 발생 시, update 실행됨.

    // couroutine scope를 위해서, scope 호출. factory, update는 coroutine이 아님.
//    val scope = rememberCoroutineScope()
    val webView = rememberWebView()

    // 한번만 실행되는 것을 처리하는 안전한 방법. Composable 안에서 LaunchedEffect 사용.
    // 전달하는 객체가 변화가 됐을 때, 실행이 되는데, Composable 수명 주기와 동일하게 가고 싶다면,
    // Unit 으로 지정해주면 됨.
    // 컴포저블 내에서 코루틴 스코프를 이용할 때, LaunchedEffect도 하나의 방법이다. 
    LaunchedEffect(Unit) {
        // undoShareFlow 변수 변화를 관찰 -> collectLatest
        viewModel.undoShareFlow.collectLatest {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                snackbarHostState.showSnackbar("더 이상 뒤로 갈 수 없음")
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.redoSharedFlow.collectLatest {
            if (webView.canGoForward()) {
                webView.goForward()
            } else {
                snackbarHostState.showSnackbar("더 이상 앞으로 갈 수 없음")
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { webView },
        // 화면 갱신은 이쪽 코드를 타게 되는데, sharedFlow를 보게끔 여기서 설정해줘야 한다.
        update = { webView ->
            webView.loadUrl(viewModel.url.value)
            // 빈 url 후 뒤로 가기를 누르면 Snackbar가 무한히 나옴 -> 무한히 실행되므로 수정해야함. -> MyWebview 로 이동
//            scope.launch {
//                // undoShareFlow 변수 변화를 관찰 -> collectLatest
//                viewModel.undoShareFlow.collectLatest {
//                    if (webView.canGoBack()) {
//                        webView.goBack()
//                    } else {
//                        snackbarHostState.showSnackbar("더 이상 뒤로 갈 수 없음")
//                    }
//                }
//            }
//            scope.launch {
//                viewModel.redoSharedFlow.collectLatest {
//                    if (webView.canGoForward()) {
//                        webView.goForward()
//                    } else {
//                        snackbarHostState.showSnackbar("더 이상 앞으로 갈 수 없음")
//                    }
//                }
//            }
        })
}

@Composable
fun rememberWebView(): WebView {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply{
            // 추가 setting 필요.
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadUrl("https://google.com")
        }
    }
    return webView
}