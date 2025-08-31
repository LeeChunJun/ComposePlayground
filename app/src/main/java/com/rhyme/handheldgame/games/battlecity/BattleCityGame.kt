package com.rhyme.handheldgame.games.battlecity

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.rhyme.handheldgame.games.battlecity.logic.BattleCityViewModel
import com.rhyme.handheldgame.games.battlecity.ui.BattleCityScreen
import com.rhyme.handheldgame.shared.Game

// com/rhyme/handheldgame/games/battlecity/BattleCityGame.kt
class BattleCityGame : Game {
    override val name: String = "Battle City"
    override val description: String = "经典坦克大战游戏"

    override fun createViewModel(): ViewModel = BattleCityViewModel()

    override fun createScreen(): @Composable () -> Unit = {
        BattleCityScreen()
    }
}
