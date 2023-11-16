package com.example.bmicalculator

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bmicalculator.ui.theme.BMICalculatorTheme
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // gradle에 viewmodel 추가
            val viewModel = viewModel<BmiViewModel>()
            val navController = rememberNavController()

            // viewModel.bmiCalculate에서 값 변경이 일어나면서, 컴포즈 다시 실행, recompition 일어나면서, bmi 변경
            val bmi = viewModel.bmi.value
            // startDestination : 어디로 이동할건지,
            NavHost(navController = navController, startDestination = "home") {
                composable(route = "home") {
                    HomeScreen() {
                        height, weight ->
                            viewModel.bmiCalculate(height, weight)
                            navController.navigate("result")
                    }
                }
                composable(route = "result") {
                    ResultScreen(navController, bmi = bmi)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
//    navController: NavController,
    onResultClicked: (Double, Double) -> Unit,
) {
    // 화면 전환시 날라가지 않도록 -> rememberSaveable
    val (height, setHeight) = rememberSaveable {
        mutableStateOf("")
    }
    val (weight, setWeight) = rememberSaveable {
        mutableStateOf("")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("비만도 계산기") }
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            OutlinedTextField(
                value = height,
                onValueChange = setHeight,
                label = { Text("키") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = setWeight,
                label = { Text("몸무게") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // 계산을 먼저 할 수 있도록 함.
                    if (height.isNotEmpty() && weight.isNotEmpty()) {
                        onResultClicked(height.toDouble(), weight.toDouble())
                    }
//                    navController.navigate("result")
                },
                modifier = Modifier.align(Alignment.End) // 오른쪽 끝으로 이동시킴
            ) {
                Text("결과")
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, bmi: Double) {
    val text = when {
        bmi >= 35 -> "고도 비만"
        bmi >= 30 -> "2단계 비만"
        bmi >= 25 -> "1단계 비만"
        bmi >= 23 -> "과체중"
        bmi >= 18.5 -> "정상"
        else -> "저체중"
    }
    val imageRes = when {
        bmi >= 23 -> R.drawable.baseline_sentiment_very_dissatisfied_24
        bmi >= 18.5 -> R.drawable.baseline_sentiment_satisfied_24
        else -> R.drawable.baseline_sentiment_dissatisfied_24
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("비만도 계산기") }, navigationIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "home",
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
            })
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text, fontSize = 30.sp) // 글자는 sp이다.
            Spacer(modifier = Modifier.height(50.dp))
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                colorFilter = ColorFilter.tint(color = Color.Black)
            )
        }
    }
}

class BmiViewModel : ViewModel() {
    // compose에 data를 표시하기 위해서 state 표시
    private val _bmi = mutableStateOf(0.0)

    // 외부로 노출시키기 위한 bmi 변수
    // ViewModel 에서 State 값이 변화했을 때, 화면이 다시 그려진다는 것을 기억해두기
    val bmi: State<Double> = _bmi

    fun bmiCalculate(
        height: Double, weight: Double
    ) {
        _bmi.value = weight / (height / 100.0).pow(2.0)
    }
}

@Preview
@Composable
fun Preview() {
}