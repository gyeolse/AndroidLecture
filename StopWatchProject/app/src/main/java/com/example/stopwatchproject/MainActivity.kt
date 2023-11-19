package com.example.stopwatchproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stopwatchproject.ui.theme.StopWatchProjectTheme
import java.util.Timer
import kotlin.concurrent.timer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // viewModel 들고오기
            val viewModel = viewModel<MainViewModel>()

            val sec = viewModel.sec.value
            val milli = viewModel.milli.value
            val isRunning = viewModel.isRunning.value
            val lapTimes = viewModel.lapTimes.value

            MainScreen(
                sec = sec,
                milli = milli,
                isRunning = isRunning,
                lapTimes = lapTimes,
                onReset = { viewModel.reset() },
                onToggle = { running ->
                    if (running) {
                        viewModel.pause()
                    } else {
                        viewModel.start()
                    }
                },
                onLapTime = { viewModel.recordLapTime() },
            )
        }
    }
}

class MainViewModel : ViewModel() {
    // build gradle 에서 viewModel 사용 원활하게 설정하기 위해서 implementation 추가
    private var time = 0
    private var timerTask: Timer? = null

    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    private val _sec = mutableStateOf(0)
    val sec: State<Int> = _sec

    private val _milli = mutableStateOf(0)
    val milli: State<Int> = _milli

    private val _lapTimes = mutableStateOf(mutableListOf<String>())
    val lapTimes: State<List<String>> = _lapTimes

    private var lap = 1

    fun start() {
        _isRunning.value = true

        timerTask = timer(period = 10) {
            time++;
            _sec.value = time / 100
            _milli.value = time % 100
        }
    }

    fun pause() {
        _isRunning.value = false
        timerTask?.cancel()
    }

    fun reset() {
        timerTask?.cancel()

        time = 0
        _isRunning.value = false
        _sec.value = 0
        _milli.value = 0

        // reset LapTimes
        _lapTimes.value.clear()
        lap = 1
    }

    fun recordLapTime() {
        // List 필요
        _lapTimes.value.add(0, "$lap LAP : ${sec.value}.${milli.value}")
        lap++;
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    // 화면에서 변경되는것을 params 로 받는 것이 좋을 듯
    // 각 버튼을 눌렀을 떄의 처리를 위한 콜백 처리 함수 3개 추가,
    sec: Int,
    milli: Int,
    isRunning: Boolean,
    lapTimes: List<String>,
    onReset: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onLapTime: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("StopWatch Project") })
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(text = "$sec", fontSize = 100.sp)
                Text(text = "$milli")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                // weight 속성을 줘야함. 숫자랑 버튼을 제외한 나머지 영역을 차지하게끔
                // LazyColumn 을 사용하거나, rememberScrollState를 써서, 스크롤이 가능하고, 스크롤 유지하게끔
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                lapTimes.forEach { lapTime -> Text(lapTime) }
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingActionButton(
                    onClick = { onReset() },
                    contentColor = Color.Red,
                    containerColor = Color.Red
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_refresh_24),
                        contentDescription = "reset"
                    )
                }

                FloatingActionButton(
                    onClick = { onToggle(isRunning) },
                    contentColor = Color.Green,
                    containerColor = Color.Green
                ) {
                    Image(
                        painter = painterResource(id = if (isRunning) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24),
                        contentDescription = "start/pause"
                    )
                }

                Button(onClick = { onLapTime() }) {
                    Text("Lap Time ")
                }

            }
        }
    }

}