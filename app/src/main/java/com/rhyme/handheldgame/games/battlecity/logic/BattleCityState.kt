package com.rhyme.handheldgame.games.battlecity.logic

import androidx.compose.ui.geometry.Offset
import com.rhyme.handheldgame.shared.GameAction
import com.rhyme.handheldgame.shared.GameState

// com/rhyme/handheldgame/games/battlecity/logic/BattleCityState.kt
data class BattleCityState(
    val playerTank: Tank,
    val enemyTanks: List<Tank>,
    val bullets: List<Bullet>,
    val walls: List<Wall>,
    val score: Int = 0,
    val level: Int = 1,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
) : GameState

data class Wall(
    val position: Offset,
    val isDestructible: Boolean
)

// com/rhyme/handheldgame/games/battlecity/logic/BattleCityAction.kt
sealed class BattleCityAction : GameAction {
    object Start : BattleCityAction()
    object Pause : BattleCityAction()
    object Resume : BattleCityAction()
    data class Move(val direction: Direction) : BattleCityAction()
    object Shoot : BattleCityAction()
    object Tick : BattleCityAction() // 游戏时钟
}
