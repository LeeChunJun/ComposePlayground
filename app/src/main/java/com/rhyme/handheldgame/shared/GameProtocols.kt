package com.rhyme.handheldgame.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow


interface Game {
    val name: String
    val description: String
    fun createViewModel(): ViewModel
    fun createScreen(): @Composable () -> Unit
}

// core/src/main/java/com/jetgame/core/GameState.kt
interface GameState {
    val isRunning: Boolean
    val isPaused: Boolean
    val score: Int
}

// core/src/main/java/com/jetgame/core/GameAction.kt
interface GameAction

// core/src/main/java/com/jetgame/core/GameViewModel.kt
abstract class GameViewModel<T : GameState, A : GameAction> : ViewModel() {
    abstract val state: StateFlow<T>
    abstract fun dispatch(action: A)
}
