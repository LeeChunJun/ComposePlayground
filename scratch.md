1. 新架构分层设计.
├── app                    // 主应用模块
├── core                   // 核心模块（通用组件）
│   ├── ui                 // 通用UI组件
│   └── navigation         // 导航组件
├── games                  // 游戏模块
│   ├── tetris             // 俄罗斯方块模块
│   │   ├── logic
│   │   └── ui
│   └── battlecity         // 坦克大战模块
│       ├── logic
│       └── ui
└── shared                 // 共享资源模块

2. 接口设计
// core/src/main/java/com/jetgame/core/Game.kt
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

3. 实现方案
// app/src/main/java/com/jetgame/app/MainActivity.kt
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

------

坦克大战游戏实现
1. 游戏实体定义

// games/battlecity/src/main/java/com/jetgame/battlecity/logic/Entities.kt
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

2. 游戏状态和动作
// games/battlecity/src/main/java/com/jetgame/battlecity/logic/BattleCityState.kt
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

// games/battlecity/src/main/java/com/jetgame/battlecity/logic/BattleCityAction.kt
sealed class BattleCityAction : GameAction {
    object Start : BattleCityAction()
    object Pause : BattleCityAction()
    object Resume : BattleCityAction()
    data class Move(val direction: Direction) : BattleCityAction()
    object Shoot : BattleCityAction()
    object Tick : BattleCityAction() // 游戏时钟
}

3. 游戏 ViewModel
// games/battlecity/src/main/java/com/jetgame/battlecity/logic/BattleCityViewModel.kt
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

4. 游戏 UI 实现
// games/battlecity/src/main/java/com/jetgame/battlecity/ui/BattleCityScreen.kt
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

5. 游戏入口实现
// games/battlecity/src/main/java/com/jetgame/battlecity/BattleCityGame.kt
class BattleCityGame : Game {
    override val name: String = "Battle City"
    override val description: String = "经典坦克大战游戏"
    
    override fun createViewModel(): ViewModel = BattleCityViewModel()
    
    override fun createScreen(): @Composable () -> Unit = {
        BattleCityScreen()
    }
}
-----

总结
以上方案提供了一个可扩展的游戏架构，主要特点包括：
- 模块化设计：将不同游戏分离到独立模块中，便于维护和扩展
- 统一接口：通过 Game 接口统一不同类型游戏的接入方式
- 可复用组件：保留了原项目中的通用UI组件（如 GameButton）
- MVI架构：延续了原项目的架构模式，保持一致性
通过这种方式，你可以轻松地添加更多游戏类型，同时保持代码的清晰和可维护性。

