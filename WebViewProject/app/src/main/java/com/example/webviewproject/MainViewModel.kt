package com.example.webviewproject

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val url = mutableStateOf("https://www.google.com")

    // 한번에 하나의 데이터를 발행하면서, 동일한 데이터를 발행하게 하는 Flow에서 파생된 것 중, SharedFlow를 사용
    private val _undoSharedFlow = MutableSharedFlow<Boolean>()
    val undoShareFlow = _undoSharedFlow.asSharedFlow()

    private val _redoSharedFlow = MutableSharedFlow<Boolean>()
    val redoSharedFlow = _redoSharedFlow.asSharedFlow()


    // 뒤로 가기
    fun undo() {
        // viewModel 내의 scope에서 실행할 때는 viewModelScope 의 launch를 사용하고,
        viewModelScope.launch {
            // .emit(true) : 동일한 값을 계속 발행할 때 사용하기 좋음.
            _undoSharedFlow.emit(true)
        }
    }

    // 앞으로 가기
    fun redo() {
        viewModelScope.launch {
            _redoSharedFlow.emit(true)
        }

    }
}