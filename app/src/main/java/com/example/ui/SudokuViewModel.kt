package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppThemeMode
import com.example.data.db.GameHistoryEntity
import com.example.data.db.GameStatsEntity
import com.example.data.db.UserSettingsEntity
import com.example.data.repository.SudokuRepository
import com.example.engine.SudokuGenerator
import com.example.model.MoveHistoryCodec
import com.example.model.CellDelta
import com.example.model.CompletionBurst
import com.example.model.Difficulty
import com.example.model.LineCompletionEvent
import com.example.model.LineType
import com.example.model.SudokuBoard
import com.example.model.SudokuCell
import com.example.model.SudokuMove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GameUiState(
    val board: SudokuBoard = SudokuBoard.createEmpty(),
    val selectedCell: Pair<Int, Int>? = null,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isNotesMode: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val errorsCount: Int = 0,
    val maxErrors: Int = 3,
    val hintsUsed: Int = 0,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val generationError: String? = null,
    val requestedDifficulty: Difficulty = Difficulty.MEDIUM,
    val hintExplanation: String? = null,
    val hasActiveSavedGame: Boolean = false,
    val isGenerating: Boolean = false,
    // Última ráfaga de fila/columna/bloque recién completados, para disparar el
    // efecto de "onda de brillo" en la UI. Se reemplaza solo cuando un movimiento
    // realmente completa algo; si no, se conserva la anterior sin cambios (la UI
    // la usa como key de animación, así que no volver a asignarla no la reproduce
    // de nuevo).
    val completionBurst: CompletionBurst? = null
)

class SudokuViewModel(
    private val repository: SudokuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettingsFlow
        .map { it ?: UserSettingsEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserSettingsEntity()
        )

    val allStats: StateFlow<List<GameStatsEntity>> = repository.allStatsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Historial reciente ya agrupado por dificultad y en orden cronológico
    // (más vieja primero), listo para el gráfico de progreso de StatsScreen.
    val recentHistoryByDifficulty: StateFlow<Map<String, List<GameHistoryEntity>>> =
        repository.recentHistoryFlow
            .map { history -> history.groupBy { it.difficulty }.mapValues { (_, v) -> v.reversed() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )

    private val undoStack = ArrayDeque<SudokuMove>()
    private val redoStack = ArrayDeque<SudokuMove>()
    private var sessionId = "legacy"
    private var generationJob: Job? = null
    private var persistenceJob: Job? = null
    private fun persist(block: suspend () -> Unit) {
        val previous = persistenceJob
        persistenceJob = viewModelScope.launch { previous?.join(); block() }
    }
    private var timerJob: Job? = null

    // true recién cuando el ViewModel realmente cargó una partida (nueva o
    // continuada) en esta sesión de proceso. Antes de eso, uiState.board es
    // solo el tablero vacío inicial (SudokuBoard.createEmpty()). Bug que esto
    // soluciona: la app tiene un único ViewModel compartido y un observer de
    // ciclo de vida que llama a pauseGame() al pasar a segundo plano sin
    // importar en qué pantalla esté el usuario. Si eso pasaba estando en
    // Inicio (por ejemplo, recién abierta la app, antes de tocar "Continuar
    // partida"), pauseGame() igual disparaba saveCurrentGame() y terminaba
    // pisando la partida real guardada en la base de datos con ese tablero
    // vacío. La próxima vez que se tocaba "Continuar partida", se restauraba
    // ese tablero vacío. Este flag evita guardar nada hasta que haya una
    // partida de verdad cargada.
    private var hasActiveGameLoaded = false

    // Un error se muestra en pantalla (celda en rojo) si el usuario activó la
    // auto-comprobación, o si el límite de errores está activo (en ese caso
    // necesita ver sus errores para no perder "a ciegas"). Se usa tanto al
    // jugar en vivo (inputNumber) como al restaurar una partida guardada
    // (resumeSavedGame), para que el comportamiento sea coherente.
    private fun shouldShowErrors(settings: UserSettingsEntity): Boolean =
        settings.autoCheckErrors || settings.errorLimitEnabled

    init {
        // Observe if an active game is saved
        viewModelScope.launch {
            repository.activeGameFlow.collect { saved ->
                _uiState.update { it.copy(hasActiveSavedGame = saved != null && !saved.isCompleted) }
            }
        }
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value
                if (!current.isPaused && !current.isCompleted && !current.isGameOver && !current.isGenerating) {
                    val updatedSeconds = current.elapsedSeconds + 1
                    _uiState.update { it.copy(elapsedSeconds = updatedSeconds) }
                    // Auto-save periodically every 10 seconds
                    if (updatedSeconds % 10 == 0L) {
                        saveCurrentGame()
                    }
                }
            }
        }
    }

    fun pauseGame() {
        if (_uiState.value.isGenerating) {
            generationJob?.cancel()
            return
        }
        if (!hasActiveGameLoaded) return
        if (_uiState.value.isCompleted || _uiState.value.isGameOver) return
        timerJob?.cancel()
        _uiState.update { it.copy(isPaused = true, hintExplanation = null) }
        saveCurrentGame()
    }

    fun resumeGame() {
        if (!hasActiveGameLoaded || _uiState.value.isCompleted || _uiState.value.isGameOver) return
        _uiState.update { it.copy(isPaused = false, generationError = null) }
        startTimer()
    }

    fun startNewGame(difficulty: Difficulty, seed: Int? = null) {
        if (_uiState.value.isGenerating) return
        timerJob?.cancel()
        generationJob = viewModelScope.launch {
            persistenceJob?.join()
            val previousState = _uiState.value
            _uiState.update { it.copy(isGenerating = true, generationError = null, requestedDifficulty = difficulty, difficulty = difficulty, isPaused = false, isCompleted = false, isGameOver = false) }
            val puzzle = try {
                SudokuGenerator.generateAsync(difficulty, seed?.let { kotlin.random.Random(it) } ?: kotlin.random.Random)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                _uiState.value = previousState.copy(isPaused = true, isGenerating = false,
                    requestedDifficulty = difficulty,
                    generationError = if (hasActiveGameLoaded) null else "La generación se interrumpió. Podés volver a intentarlo.")
                throw cancelled
            } catch (failure: Exception) {
                _uiState.value = previousState.copy(isGenerating = false, isPaused = true, requestedDifficulty = difficulty,
                    generationError = "No se pudo preparar el tablero. Volvé a intentarlo.")
                return@launch
            }
            undoStack.clear()
            redoStack.clear()
            sessionId = java.util.UUID.randomUUID().toString()

            val board = SudokuBoard.fromGrid(puzzle.puzzle, puzzle.solution)
            repository.recordGameStarted(difficulty)
            hasActiveGameLoaded = true

            _uiState.update {
                it.copy(
                    board = board,
                    selectedCell = null,
                    difficulty = difficulty,
                    isNotesMode = false,
                    elapsedSeconds = 0L,
                    errorsCount = 0,
                    hintsUsed = 0,
                    isPaused = false,
                    isCompleted = false,
                    isGameOver = false,
                    canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty(),
                    isGenerating = false,
                    completionBurst = null, hintExplanation = null, generationError = null
                )
            }

            saveCurrentGame()
            startTimer()
        }
    }

    fun startDailyGame() {
        val today = java.util.Calendar.getInstance()
        val seed = today.get(java.util.Calendar.YEAR) * 1000 + today.get(java.util.Calendar.DAY_OF_YEAR)
        startNewGame(Difficulty.MEDIUM, seed)
    }

    fun resumeSavedGame() {
        if (hasActiveGameLoaded && !_uiState.value.isCompleted && !_uiState.value.isGameOver) {
            resumeGame()
            return
        }
        viewModelScope.launch {
            persistenceJob?.join()
            val entity = repository.getActiveGame() ?: return@launch

            // Defensa adicional: si la partida guardada ya había alcanzado el
            // límite de errores (p.ej. quedó así por el bug anterior, o el
            // límite se activó después de guardarla), no se puede "revivir".
            val settings = repository.getUserSettings()
            if (settings.errorLimitEnabled && entity.errorsCount >= _uiState.value.maxErrors) {
                repository.clearActiveGame()
                return@launch
            }

            val showErrors = shouldShowErrors(settings)
            val (board, diff) = repository.restoreBoard(entity, showErrors)
            undoStack.clear()
            undoStack.addAll(MoveHistoryCodec.decode(entity.undoData))
            redoStack.clear()
            redoStack.addAll(MoveHistoryCodec.decode(entity.redoData))
            sessionId = entity.sessionId
            hasActiveGameLoaded = true

            _uiState.update {
                it.copy(
                    board = board,
                    selectedCell = null,
                    difficulty = diff,
                    isNotesMode = false,
                    elapsedSeconds = entity.elapsedSeconds,
                    errorsCount = entity.errorsCount,
                    hintsUsed = entity.hintsUsed,
                    isPaused = false,
                    isCompleted = entity.isCompleted,
                    isGameOver = false,
                    canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty(),
                    isGenerating = false,
                    completionBurst = null, hintExplanation = null, generationError = null
                )
            }

            startTimer()
        }
    }

    fun restartCurrentGame() {
        if (!hasActiveGameLoaded || _uiState.value.isGenerating) return
        val current = _uiState.value
        if (current.isCompleted || current.isGameOver) {
            sessionId = java.util.UUID.randomUUID().toString()
            persist { repository.recordGameStarted(current.difficulty) }
        }
        val resetCells = current.board.cells.map { cell ->
            if (cell.isGiven) cell
            else cell.copy(value = 0, notes = emptySet(), isError = false)
        }
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                board = SudokuBoard(resetCells),
                selectedCell = null,
                isNotesMode = false,
                elapsedSeconds = 0L,
                errorsCount = 0,
                hintsUsed = 0,
                isPaused = false,
                isCompleted = false,
                isGameOver = false,
                canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty(),
                completionBurst = null, hintExplanation = null, generationError = null
            )
        }
        saveCurrentGame()
        startTimer()
    }

    fun selectCell(row: Int, col: Int) {
        if (row !in 0..8 || col !in 0..8 || _uiState.value.isGenerating) return
        if (_uiState.value.isPaused || _uiState.value.isCompleted || _uiState.value.isGameOver) return
        _uiState.update { it.copy(selectedCell = Pair(row, col)) }
    }

    fun toggleNotesMode() {
        _uiState.update { it.copy(isNotesMode = !it.isNotesMode) }
    }

    fun inputNumber(number: Int) {
        if (number !in 1..9) return
        val state = _uiState.value
        if (state.isGenerating || state.isPaused || state.isCompleted || state.isGameOver) return
        val sel = state.selectedCell ?: return
        val currentCell = state.board.getCell(sel.first, sel.second)

        // Clue cells cannot be modified
        if (currentCell.isGiven) return

        val settings = userSettings.value

        if (state.isNotesMode) {
            if (currentCell.value != 0) return
            // Toggle note
            val newNotes = if (currentCell.notes.contains(number)) {
                currentCell.notes - number
            } else {
                currentCell.notes + number
            }
            val delta = CellDelta(
                row = sel.first,
                col = sel.second,
                oldValue = currentCell.value,
                newValue = currentCell.value,
                oldNotes = currentCell.notes,
                newNotes = newNotes,
                oldIsError = currentCell.isError,
                newIsError = currentCell.isError
            )
            redoStack.clear()
            undoStack.addLast(SudokuMove(listOf(delta)))

            val updatedCells = state.board.cells.map { cell ->
                if (cell.row == sel.first && cell.col == sel.second) {
                    cell.copy(notes = newNotes)
                } else cell
            }
            _uiState.update {
                it.copy(board = SudokuBoard(updatedCells), canRedo = redoStack.isNotEmpty(), canUndo = undoStack.isNotEmpty())
            }
            saveCurrentGame()
            return
        }

        // Sin comprobación, ningún comportamiento de edición revela la solución.
        // Normal number placement mode
        if (currentCell.value == number) {
            // Ya estaba puesto este número. Si es correcto, un segundo toque no debe
            // hacer nada: antes se borraba igual (bug del "doble-tap" que hacía
            // desaparecer aciertos ya colocados, por ejemplo al tocar dos veces sin
            // querer el mismo botón del teclado). Solo si el número puesto es
            // incorrecto seguimos permitiendo que un segundo toque lo borre, para
            // poder corregir un error rápido sin pasar por el botón de borrar.
            if (!shouldShowErrors(settings) || number == currentCell.solutionValue) return
            erase()
            return
        }

        // La celda ya tiene puesto el número correcto: un toque accidental de
        // otro número no debe pisarlo ni contarse como error (igual que ya
        // pasa con las celdas dadas). Si de verdad se quiere cambiar, primero
        // hay que borrarla con el botón de borrar.
        if (shouldShowErrors(settings) && currentCell.value != 0 && currentCell.value == currentCell.solutionValue) return

        val isCorrect = (number == currentCell.solutionValue)
        val isError = !isCorrect
        val newErrorsCount = if (isError) state.errorsCount + 1 else state.errorsCount
        val reachedGameOver = settings.errorLimitEnabled && newErrorsCount >= state.maxErrors

        val deltas = mutableListOf<CellDelta>()
        val mainDelta = CellDelta(
            row = sel.first,
            col = sel.second,
            oldValue = currentCell.value,
            newValue = number,
            oldNotes = currentCell.notes,
            newNotes = emptySet(),
            oldIsError = currentCell.isError,
            newIsError = if (shouldShowErrors(settings)) isError else false
        )
        deltas.add(mainDelta)

        val relatedCoords = if (settings.autoRemoveNotes && (isCorrect || !shouldShowErrors(settings))) {
            state.board.getRelatedCoords(sel.first, sel.second)
        } else {
            emptySet()
        }

        val updatedCells = state.board.cells.map { cell ->
            if (cell.row == sel.first && cell.col == sel.second) {
                cell.copy(
                    value = number,
                    notes = emptySet(),
                    isError = if (shouldShowErrors(settings)) isError else false
                )
            } else if (relatedCoords.contains(cell.row to cell.col) && cell.notes.contains(number)) {
                val updatedNotes = cell.notes - number
                deltas.add(
                    CellDelta(
                        row = cell.row,
                        col = cell.col,
                        oldValue = cell.value,
                        newValue = cell.value,
                        oldNotes = cell.notes,
                        newNotes = updatedNotes,
                        oldIsError = cell.isError,
                        newIsError = cell.isError
                    )
                )
                cell.copy(notes = updatedNotes)
            } else cell
        }

        val newBoard = SudokuBoard(updatedCells)
        redoStack.clear()
        undoStack.addLast(SudokuMove(deltas))

        val isSolved = newBoard.isSolvedCorrectly

        // Solo puede haberse completado una fila/columna/bloque recién ahora si el
        // número puesto era correcto (un número mal puesto nunca termina de llenar
        // nada de forma válida).
        val newBurst = if (isCorrect && shouldShowErrors(settings)) {
            buildCompletionBurst(newBoard, sel.first, sel.second)
        } else {
            null
        }

        _uiState.update {
            it.copy(
                board = newBoard,
                completionBurst = newBurst ?: it.completionBurst,
                errorsCount = newErrorsCount,
                isGameOver = reachedGameOver,
                isCompleted = isSolved,
                canRedo = redoStack.isNotEmpty(), canUndo = undoStack.isNotEmpty()
            )
        }

        when {
            isSolved -> {
                timerJob?.cancel()
                val completedSession = sessionId
                persist {
                    repository.recordGameCompleted(
                        sessionId = completedSession,
                        difficulty = state.difficulty,
                        elapsedSeconds = state.elapsedSeconds,
                        errors = newErrorsCount,
                        hints = state.hintsUsed
                    )
                }
            }
            reachedGameOver -> {
                // Antes: se guardaba como si la partida siguiera "activa", así
                // que volvía a aparecer "Continuar partida" y se podía retomar
                // un tablero ya perdido. Ahora se descarta, igual que al ganar.
                timerJob?.cancel()
                persist {
                    repository.clearActiveGame()
                }
            }
            else -> saveCurrentGame()
        }
    }

    fun erase() {
        val state = _uiState.value
        if (state.isGenerating || state.isPaused || state.isCompleted || state.isGameOver) return
        val sel = state.selectedCell ?: return
        val currentCell = state.board.getCell(sel.first, sel.second)

        if (currentCell.isGiven) return
        if (currentCell.value == 0 && currentCell.notes.isEmpty()) return

        val delta = CellDelta(
            row = sel.first,
            col = sel.second,
            oldValue = currentCell.value,
            newValue = 0,
            oldNotes = currentCell.notes,
            newNotes = emptySet(),
            oldIsError = currentCell.isError,
            newIsError = false
        )
        redoStack.clear()
        undoStack.addLast(SudokuMove(listOf(delta)))

        val updatedCells = state.board.cells.map { cell ->
            if (cell.row == sel.first && cell.col == sel.second) {
                cell.copy(value = 0, notes = emptySet(), isError = false)
            } else cell
        }

        _uiState.update {
            it.copy(
                board = SudokuBoard(updatedCells),
                canRedo = redoStack.isNotEmpty(), canUndo = undoStack.isNotEmpty()
            )
        }
        saveCurrentGame()
    }

    fun undo() {
        val current = _uiState.value
        if (current.isGenerating || current.isPaused || current.isCompleted || current.isGameOver || undoStack.isEmpty()) return
        val move = undoStack.removeLast()
        redoStack.addLast(move)
        val state = _uiState.value

        val deltaMap = move.deltas.associateBy { it.row to it.col }

        val updatedCells = state.board.cells.map { cell ->
            val delta = deltaMap[cell.row to cell.col]
            if (delta != null) {
                cell.copy(
                    value = delta.oldValue,
                    notes = delta.oldNotes,
                    isError = shouldShowErrors(userSettings.value) && delta.oldValue != 0 && delta.oldValue != cell.solutionValue
                )
            } else cell
        }

        _uiState.update {
            it.copy(
                board = SudokuBoard(updatedCells),
                canRedo = redoStack.isNotEmpty(), canUndo = undoStack.isNotEmpty()
            )
        }
        saveCurrentGame()
    }

    fun redo() {
        val state = _uiState.value
        if (state.isGenerating || state.isPaused || state.isCompleted || state.isGameOver || redoStack.isEmpty()) return
        val move = redoStack.removeLast()
        val deltas = move.deltas.associateBy { it.row to it.col }
        val cells = state.board.cells.map { cell ->
            deltas[cell.row to cell.col]?.let { d -> cell.copy(value = d.newValue, notes = d.newNotes,
                isError = shouldShowErrors(userSettings.value) && d.newValue != 0 && d.newValue != cell.solutionValue) } ?: cell
        }
        undoStack.addLast(move)
        _uiState.update { it.copy(board = SudokuBoard(cells), canUndo = true, canRedo = redoStack.isNotEmpty()) }
        saveCurrentGame()
    }

    fun explainHint() {
        val state = _uiState.value
        if (state.isPaused || state.isCompleted || state.isGameOver || state.isGenerating) return
        val wrong = state.board.cells.firstOrNull { !it.isGiven && it.value != 0 && it.value != it.solutionValue }
        val empty = state.board.cells.filter { it.value == 0 }
        val single = if (wrong == null) empty.firstOrNull { cell ->
            val used = state.board.getRelatedCoords(cell.row, cell.col).map { state.board.getCell(it.first, it.second).value }.toSet()
            (1..9).count { it !in used } == 1
        } else null
        val target = wrong ?: single ?: findHintCell(state.board) ?: return
        val explanation = when {
            wrong != null -> "Revisá la fila ${target.row + 1}, columna ${target.col + 1}: ese valor no coincide con la solución."
            single != null -> "En la fila ${target.row + 1}, columna ${target.col + 1}, los números de su fila, columna y bloque dejan un único candidato."
            else -> "No encontré un candidato único inmediato. Podés seguir con tus notas o revelar esta celda."
        }
        _uiState.update { it.copy(selectedCell = target.row to target.col, hintExplanation = explanation,
            hintsUsed = it.hintsUsed + 1) }
        saveCurrentGame()
    }

    fun dismissHint() { _uiState.update { it.copy(hintExplanation = null) } }

    fun useHint() {
        val state = _uiState.value
        if (state.isGenerating || state.isPaused || state.isCompleted || state.isGameOver) return

        // Prefer currently selected cell if empty or incorrect and not given
        val sel = state.selectedCell
        val targetCell = if (sel != null) {
            val cell = state.board.getCell(sel.first, sel.second)
            if (!cell.isGiven && (cell.value == 0 || cell.value != cell.solutionValue)) cell
            else findHintCell(state.board)
        } else {
            findHintCell(state.board)
        } ?: return

        val solutionNum = targetCell.solutionValue
        val settings = userSettings.value
        val relatedCoords = if (settings.autoRemoveNotes) {
            state.board.getRelatedCoords(targetCell.row, targetCell.col)
        } else {
            emptySet()
        }

        val updatedCells = state.board.cells.map { cell ->
            if (cell.row == targetCell.row && cell.col == targetCell.col) {
                cell.copy(value = solutionNum, notes = emptySet(), isError = false)
            } else if (relatedCoords.contains(cell.row to cell.col) && cell.notes.contains(solutionNum)) {
                cell.copy(notes = cell.notes - solutionNum)
            } else cell
        }

        val newBoard = SudokuBoard(updatedCells)
        val isSolved = newBoard.isSolvedCorrectly
        redoStack.clear()
        val deltas = updatedCells.zip(state.board.cells).mapNotNull { (new, old) ->
            if (new == old) null else CellDelta(old.row, old.col, old.value, new.value,
                old.notes, new.notes, old.isError, new.isError)
        }
        undoStack.addLast(SudokuMove(deltas))
        val newHintsUsed = state.hintsUsed + if (state.hintExplanation == null) 1 else 0
        // Una pista siempre coloca el número correcto, así que también puede
        // completar la fila/columna/bloque de esa celda.
        val newBurst = buildCompletionBurst(newBoard, targetCell.row, targetCell.col)

        _uiState.update {
            it.copy(
                board = newBoard,
                selectedCell = Pair(targetCell.row, targetCell.col),
                hintsUsed = newHintsUsed,
                hintExplanation = null, canUndo = true, canRedo = false,
                isCompleted = isSolved,
                completionBurst = newBurst ?: it.completionBurst
            )
        }

        if (isSolved) {
            timerJob?.cancel()
            val completedSession = sessionId
            persist {
                repository.recordGameCompleted(
                    sessionId = completedSession,
                    difficulty = state.difficulty,
                    elapsedSeconds = state.elapsedSeconds,
                    errors = state.errorsCount,
                    hints = newHintsUsed
                )
            }
        } else {
            saveCurrentGame()
        }
    }

    // Revisa si la fila, la columna y/o el bloque 3x3 de la celda recién colocada
    // quedaron completos (los 9 valores correctos) con este movimiento, y arma la
    // ráfaga de eventos para que la UI dispare el efecto de "onda de brillo".
    // Devuelve null si no se completó nada, para no pisar la ráfaga anterior.
    private fun buildCompletionBurst(board: SudokuBoard, row: Int, col: Int): CompletionBurst? {
        val blockIndex = (row / 3) * 3 + (col / 3)
        val events = buildList {
            if (board.isRowComplete(row)) add(LineCompletionEvent(LineType.ROW, row))
            if (board.isColComplete(col)) add(LineCompletionEvent(LineType.COLUMN, col))
            if (board.isBlockComplete(blockIndex)) add(LineCompletionEvent(LineType.BLOCK, blockIndex))
        }
        return if (events.isNotEmpty()) CompletionBurst(events) else null
    }

    private fun findHintCell(board: SudokuBoard): SudokuCell? {
        return board.cells.firstOrNull { !it.isGiven && (it.value == 0 || it.value != it.solutionValue) }
    }

    private fun saveCurrentGame() {
        val state = _uiState.value
        if (!hasActiveGameLoaded || state.isCompleted || state.isGameOver || state.board.cells.isEmpty() || state.isGenerating) return
        val savedSession = sessionId
        val undoData = MoveHistoryCodec.encode(undoStack.toList())
        val redoData = MoveHistoryCodec.encode(redoStack.toList())
        persist {
            repository.saveActiveGame(
                board = state.board,
                difficulty = state.difficulty,
                elapsedSeconds = state.elapsedSeconds,
                errorsCount = state.errorsCount,
                hintsUsed = state.hintsUsed,
                isPaused = state.isPaused,
                isCompleted = state.isCompleted,
                sessionId = savedSession, undoData = undoData, redoData = redoData
            )
        }
    }

    fun updateSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.saveUserSettings(settings)
            _uiState.update { state -> state.copy(board = SudokuBoard(state.board.cells.map { cell ->
                cell.copy(isError = shouldShowErrors(settings) && !cell.isGiven && cell.value != 0 && cell.value != cell.solutionValue)
            })) }
            if (settings.errorLimitEnabled && _uiState.value.errorsCount >= _uiState.value.maxErrors) {
                timerJob?.cancel()
                _uiState.update { it.copy(isGameOver = true) }
                persist { repository.clearActiveGame() }
            }
        }
    }

    fun setTheme(themeMode: AppThemeMode) {
        val updated = userSettings.value.copy(themeMode = themeMode.name)
        updateSettings(updated)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun provideFactory(repository: SudokuRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SudokuViewModel(repository) as T
                }
            }
    }
}
