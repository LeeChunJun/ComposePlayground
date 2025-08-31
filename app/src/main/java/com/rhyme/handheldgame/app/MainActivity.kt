package com.rhyme.handheldgame.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rhyme.handheldgame.games.tetris.ui.GameBody
import com.rhyme.handheldgame.games.tetris.ui.GameScreen
import com.rhyme.handheldgame.games.tetris.ui.PreviewGamescreen
import com.rhyme.handheldgame.games.tetris.ui.combinedClickable
import com.rhyme.handheldgame.games.tetris.ui.theme.ComposetetrisTheme
import com.rhyme.handheldgame.games.tetris.logic.Action
import com.rhyme.handheldgame.games.tetris.logic.Direction
import com.rhyme.handheldgame.games.tetris.logic.GameViewModel
import com.rhyme.handheldgame.games.tetris.logic.SoundUtil
import com.rhyme.handheldgame.games.tetris.logic.StatusBarUtil
import com.rhyme.handheldgame.shared.Game
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.transparentStatusBar(this)
        SoundUtil.init(this)

        setContent {
            ComposetetrisTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {

                    val viewModel = viewModel<GameViewModel>()
                    val viewState = viewModel.viewState.value

                    LaunchedEffect(key1 = Unit) {
                        while (isActive) {
                            delay(650L - 55 * (viewState.level - 1))
                            viewModel.dispatch(Action.GameTick)
                        }
                    }

                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(key1 = Unit) {
                        val observer = object : DefaultLifecycleObserver {
                            override fun onResume(owner: LifecycleOwner) {
                                viewModel.dispatch(Action.Resume)
                            }

                            override fun onPause(owner: LifecycleOwner) {
                                viewModel.dispatch(Action.Pause)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }


                    GameBody(combinedClickable(
                        onMove = { direction: Direction ->
                            if (direction == Direction.Up) viewModel.dispatch(Action.Drop)
                            else viewModel.dispatch(Action.Move(direction))
                        },
                        onRotate = {
                            viewModel.dispatch(Action.Rotate)
                        },
                        onRestart = {
                            viewModel.dispatch(Action.Reset)
                        },
                        onPause = {
                            if (viewModel.viewState.value.isRuning) {
                                viewModel.dispatch(Action.Pause)
                            } else {
                                viewModel.dispatch(Action.Resume)
                            }
                        },
                        onMute = {
                            viewModel.dispatch(Action.Mute)
                        }
                    )) {
                        GameScreen(
                            Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        SoundUtil.release()
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposetetrisTheme {
        GameBody {
            PreviewGamescreen(Modifier.fillMaxSize())
        }
    }
}

// com/rhyme/handheldgame/app/MainActivity.kt
@Composable
fun MainScreen() {
    var currentGame by remember { mutableStateOf<Game?>(null) }

    if (currentGame == null) {
        GameSelectionScreen(
            games = listOf(TetrisGame(), BattleCityGame()),
            onGameSelected = { game -> currentGame = game }
        )
    } else {
        GameWrapper(
            game = currentGame!!,
            onBack = { currentGame = null }
        )
    }
}

@Composable
fun GameSelectionScreen(
    games: List<Game>,
    onGameSelected: (Game) -> Unit
) {
    LazyColumn {
        items(games) { game ->
            GameItem(
                game = game,
                onClick = { onGameSelected(game) }
            )
        }
    }
}
