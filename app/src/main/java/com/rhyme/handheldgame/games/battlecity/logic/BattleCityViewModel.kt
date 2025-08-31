package com.rhyme.handheldgame.games.battlecity.logic

import androidx.compose.ui.geometry.Offset
import com.rhyme.handheldgame.shared.GameViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// com/rhyme/handheldgame/games/battlecity/logic/BattleCityViewModel.kt
class BattleCityViewModel : GameViewModel<BattleCityState, BattleCityAction>() {
    private val _state = MutableStateFlow(BattleCityState(
        playerTank = Tank(Offset(6f, 22f), Direction.Up, TankType.Player),
        enemyTanks = emptyList(),
        bullets = emptyList(),
        walls = generateWalls()
    ))

    override val state: StateFlow<BattleCityState> = _state.asStateFlow()

    override fun dispatch(action: BattleCityAction) {
        when (action) {
            is BattleCityAction.Start -> startGame()
            is BattleCityAction.Pause -> pauseGame()
            is BattleCityAction.Resume -> resumeGame()
            is BattleCityAction.Move -> movePlayer(action.direction)
            is BattleCityAction.Shoot -> playerShoot()
            is BattleCityAction.Tick -> updateGame()
        }
    }

    private fun startGame() {
        _state.value = _state.value.copy(isRunning = true)
    }

    private fun pauseGame() {
        _state.value = _state.value.copy(isPaused = true)
    }

    private fun resumeGame() {
        _state.value = _state.value.copy(isPaused = false)
    }

    private fun movePlayer(direction: Direction) {
        if (!_state.value.isRunning || _state.value.isPaused) return

        val newState = _state.value.copy(
            playerTank = _state.value.playerTank.move(direction)
        )
        _state.value = newState
    }

    private fun playerShoot() {
        if (!_state.value.isRunning || _state.value.isPaused) return

        val tank = _state.value.playerTank
        val bullet = Bullet(
            position = tank.position,
            direction = tank.direction
        )

        val newState = _state.value.copy(
            bullets = _state.value.bullets + bullet
        )
        _state.value = newState
    }

    private fun updateGame() {
        if (!_state.value.isRunning || _state.value.isPaused) return

        // 更新子弹位置
        val updatedBullets = _state.value.bullets.map { bullet ->
            bullet.copy(position = bullet.position + bullet.direction.toOffset())
        }.filter { bullet ->
            // 移除超出边界的子弹
            bullet.position.x >= 0 && bullet.position.x < 12 &&
            bullet.position.y >= 0 && bullet.position.y < 24
        }

        val newState = _state.value.copy(
            bullets = updatedBullets
        )
        _state.value = newState
    }

    private fun generateWalls(): List<Wall> {
        // 生成边界墙和一些障碍物
        val walls = mutableListOf<Wall>()

        // 边界墙
        for (x in 0..11) {
            walls.add(Wall(Offset(x.toFloat(), 0f), false))
            walls.add(Wall(Offset(x.toFloat(), 23f), false))
        }
        for (y in 1..22) {
            walls.add(Wall(Offset(0f, y.toFloat()), false))
            walls.add(Wall(Offset(11f, y.toFloat()), false))
        }

        // 随机障碍物
        // 这里可以添加更多复杂的关卡设计
        walls.add(Wall(Offset(5f, 10f), true))
        walls.add(Wall(Offset(6f, 10f), true))
        walls.add(Wall(Offset(5f, 11f), true))
        walls.add(Wall(Offset(6f, 11f), true))

        return walls
    }
}
