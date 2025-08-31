package com.rhyme.handheldgame.games.battlecity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rhyme.handheldgame.games.battlecity.logic.BattleCityViewModel
import com.rhyme.handheldgame.games.tetris.ui.GameButton

// com/rhyme/handheldgame/games/battlecity/ui/BattleCityScreen.kt
@Composable
fun BattleCityScreen(
    viewModel: BattleCityViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(10.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val cellSize = size.width / 12f

            // 绘制墙壁
            state.walls.forEach { wall ->
                drawRect(
                    color = if (wall.isDestructible) Color.Gray else Color.DarkGray,
                    topLeft = Offset(wall.position.x * cellSize, wall.position.y * cellSize),
                    size = Size(cellSize, cellSize)
                )
            }

            // 绘制玩家坦克
            drawTank(state.playerTank, cellSize)

            // 绘制敌人坦克
            state.enemyTanks.forEach { tank ->
                drawTank(tank, cellSize)
            }

            // 绘制子弹
            state.bullets.forEach { bullet ->
                drawCircle(
                    color = Color.Yellow,
                    center = Offset(
                        bullet.position.x * cellSize + cellSize / 2,
                        bullet.position.y * cellSize + cellSize / 2
                    ),
                    radius = cellSize / 6
                )
            }
        }

        // 游戏控制按钮
        BattleCityControls(
            onMove = { direction -> viewModel.dispatch(BattleCityAction.Move(direction)) },
            onShoot = { viewModel.dispatch(BattleCityAction.Shoot) },
            onStart = { viewModel.dispatch(BattleCityAction.Start) }
        )
    }
}

fun DrawScope.drawTank(tank: Tank, cellSize: Float) {
    val color = when (tank.type) {
        TankType.Player -> Color.Green
        TankType.Enemy -> Color.Red
    }

    drawRect(
        color = color,
        topLeft = Offset(tank.position.x * cellSize, tank.position.y * cellSize),
        size = Size(cellSize, cellSize)
    )

    // 绘制炮管方向
    val barrelEnd = when (tank.direction) {
        Direction.Up -> Offset(tank.position.x * cellSize + cellSize / 2, tank.position.y * cellSize)
        Direction.Down -> Offset(tank.position.x * cellSize + cellSize / 2, (tank.position.y + 1) * cellSize)
        Direction.Left -> Offset(tank.position.x * cellSize, tank.position.y * cellSize + cellSize / 2)
        Direction.Right -> Offset((tank.position.x + 1) * cellSize, tank.position.y * cellSize + cellSize / 2)
    }

    val barrelStart = Offset(
        tank.position.x * cellSize + cellSize / 2,
        tank.position.y * cellSize + cellSize / 2
    )

    drawLine(
        color = Color.Black,
        start = barrelStart,
        end = barrelEnd,
        strokeWidth = cellSize / 4
    )
}

@Composable
fun BattleCityControls(
    onMove: (Direction) -> Unit,
    onShoot: () -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            GameButton(
                onClick = { onMove(Direction.Up) },
                size = 60.dp
            ) {
                Text("↑")
            }
        }

        Row {
            GameButton(
                onClick = { onMove(Direction.Left) },
                size = 60.dp
            ) {
                Text("←")
            }

            GameButton(
                onClick = { onStart() },
                size = 60.dp
            ) {
                Text("Start")
            }

            GameButton(
                onClick = { onMove(Direction.Right) },
                size = 60.dp
            ) {
                Text("→")
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            GameButton(
                onClick = { onMove(Direction.Down) },
                size = 60.dp
            ) {
                Text("↓")
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            GameButton(
                onClick = { onShoot() },
                size = 80.dp
            ) {
                Text("Shoot")
            }
        }
    }
}
