package com.example.engine

import com.example.model.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

object SudokuGenerator {
    data class GeneratedPuzzle(
        val puzzle: IntArray,
        val solution: IntArray,
        val difficulty: Difficulty
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GeneratedPuzzle) return false
            return puzzle.contentEquals(other.puzzle) &&
                    solution.contentEquals(other.solution) &&
                    difficulty == other.difficulty
        }
        override fun hashCode(): Int {
            var result = puzzle.contentHashCode()
            result = 31 * result + solution.contentHashCode()
            result = 31 * result + difficulty.hashCode()
            return result
        }
    }

    private const val MAX_GRID_ATTEMPTS = 8

    fun generateCompleteGrid(random: Random = Random): IntArray {
        val grid = IntArray(81)
        for (block in 0 until 3) {
            val digits = (1..9).shuffled(random)
            val startRow = block * 3
            val startCol = block * 3
            var idx = 0
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    grid[(startRow + r) * 9 + (startCol + c)] = digits[idx++]
                }
            }
        }

        fun fillRemaining(index: Int): Boolean {
            if (index == 81) return true
            if (grid[index] != 0) return fillRemaining(index + 1)

            val row = index / 9
            val col = index % 9
            val candidates = (1..9).shuffled(random)

            for (num in candidates) {
                if (SudokuSolver.isValid(grid, row, col, num)) {
                    grid[index] = num
                    if (fillRemaining(index + 1)) return true
                    grid[index] = 0
                }
            }
            return false
        }

        fillRemaining(0)
        return grid
    }

    /**
     * Genera un puzzle paralelizando los intentos para reducir el tiempo de espera,
     * envuelto en un runBlocking para mantener la firma sincrónica hacia el exterior.
     */
    fun generate(difficulty: Difficulty, baseRandom: Random = Random): GeneratedPuzzle = runBlocking(Dispatchers.Default) {
        coroutineScope {
            val channel = Channel<GeneratedPuzzle>()

            // Lanzamos múltiples intentos en paralelo
            repeat(MAX_GRID_ATTEMPTS) { attemptId ->
                launch {
                    val localRandom = Random(baseRandom.nextInt() + attemptId)
                    val solution = generateCompleteGrid(localRandom)
                    val puzzle = digForExactDifficulty(solution, difficulty, localRandom)
                    if (puzzle != null) {
                        channel.send(puzzle)
                    }
                }
            }

            // Lanzamos el fallback como red de seguridad simultánea
            launch {
                val localRandom = Random(baseRandom.nextInt() + MAX_GRID_ATTEMPTS)
                val solution = generateCompleteGrid(localRandom)
                channel.send(digForClueCount(solution, difficulty, localRandom))
            }

            // Recibimos el primero que termine con éxito (el más rápido)
            val bestMatch = channel.receive()
            
            // Cancelamos el resto de las corrutinas en ejecución para liberar CPU
            coroutineContext.cancelChildren() 
            
            bestMatch
        }
    }

    private fun digForExactDifficulty(
        solution: IntArray,
        difficulty: Difficulty,
        random: Random
    ): GeneratedPuzzle? {
        val puzzle = solution.clone()
        var clues = 81
        var bestMatch: IntArray? = null

        for (idx in (0 until 81).shuffled(random)) {
            if (clues <= difficulty.minClues) break
            val backup = puzzle[idx]
            puzzle[idx] = 0

            val difficultyIfUnique = SudokuSolver.uniqueSolutionDifficulty(puzzle, difficulty)
            if (difficultyIfUnique == null) {
                puzzle[idx] = backup
                continue
            }

            clues--
            if (clues <= difficulty.targetClues && difficultyIfUnique == difficulty) {
                bestMatch = puzzle.clone()
            }
        }
        return bestMatch?.let { GeneratedPuzzle(it, solution, difficulty) }
    }

    private fun digForClueCount(
        solution: IntArray,
        difficulty: Difficulty,
        random: Random
    ): GeneratedPuzzle {
        val puzzle = solution.clone()
        var clues = 81

        for (idx in (0 until 81).shuffled(random)) {
            if (clues <= difficulty.targetClues) break
            val backup = puzzle[idx]
            puzzle[idx] = 0

            if (SudokuSolver.hasUniqueSolution(puzzle)) {
                clues--
            } else {
                puzzle[idx] = backup
            }
        }
        return GeneratedPuzzle(puzzle, solution, difficulty)
    }

    fun getSeedPuzzle(difficulty: Difficulty, random: Random = Random): GeneratedPuzzle {
        return generate(difficulty, random)
    }
}