package com.rhyme.handheldgame.games.battlecity.logic

import androidx.compose.ui.geometry.Offset

// com/rhyme/handheldgame/games/battlecity/logic/Entities.kt
data class Tank(
    val position: Offset,
    val direction: Direction,
    val type: TankType
) {
    fun move(direction: Direction): Tank = copy(
        position = position + direction.toOffset(),
        direction = direction
    )
}

data class Bullet(
    val position: Offset,
    val direction: Direction
)

enum class TankType {
    Player, Enemy
}

enum class Direction {
    Up, Down, Left, Right;

    fun toOffset(): Offset = when (this) {
        Up -> Offset(0f, -1f)
        Down -> Offset(0f, 1f)
        Left -> Offset(-1f, 0f)
        Right -> Offset(1f, 0f)
    }
}
