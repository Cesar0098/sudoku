package com.example.engine

import com.example.model.Difficulty
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
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

    fun generate(difficulty: Difficulty, baseRandom: Random = Random): GeneratedPuzzle =
        runBlocking { generateAsync(difficulty, baseRandom) }

    suspend fun generateAsync(difficulty: Difficulty, random: Random = Random): GeneratedPuzzle =
        kotlinx.coroutines.withContext(Dispatchers.Default) {
            repeat(128) {
                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                val solution = generateCompleteGrid(random)
                val candidate = digForExactDifficulty(solution, difficulty, random)
                if (candidate != null && SudokuSolver.evaluateDifficulty(candidate.puzzle) == difficulty) {
                    return@withContext candidate
                }
                kotlinx.coroutines.yield()
            }
            error("No se pudo generar este nivel. Volvé a intentarlo.")
        }

    private suspend fun digForExactDifficulty(
        solution: IntArray,
        difficulty: Difficulty,
        random: Random
    ): GeneratedPuzzle? {
        val puzzle = solution.clone()
        var clues = 81
        var bestMatch: IntArray? = null

        for (idx in (0 until 81).shuffled(random)) {
            kotlinx.coroutines.currentCoroutineContext().ensureActive()
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

    fun getSeedPuzzle(difficulty: Difficulty, random: Random = Random): GeneratedPuzzle {
        return generate(difficulty, random)
    }
}