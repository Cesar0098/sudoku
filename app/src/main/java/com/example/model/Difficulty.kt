package com.example.model

/**
 * Difficulty tiers are graded by which human solving technique a puzzle
 * actually *requires* — see [com.example.engine.SudokuSolver.evaluateDifficulty] —
 * not merely by how many clues it has. Clue count is still used to steer
 * generation, but the real acceptance test is the technique tier.
 *
 * @param targetClues nominal clue count the generator aims for first.
 * @param minClues hard floor; the generator never digs a grid emptier than
 *   this while searching for the right technique tier (17 is the proven
 *   theoretical minimum for a uniquely-solvable classic Sudoku).
 * @param requiredTier the maximum logical-technique tier a puzzle must need
 *   to count as this difficulty:
 *   1 = naked/hidden singles only,
 *   2 = also needs locked candidates (pointing pairs / box-line reduction),
 *   3 = also needs naked/hidden pairs,
 *   4 = also needs X-Wing, or can't be finished by logic alone (needs guessing).
 */
enum class Difficulty(
    val id: String,
    val displayName: String,
    val targetClues: Int,
    val minClues: Int,
    val requiredTier: Int
) {
    EASY("easy", "Fácil", targetClues = 42, minClues = 38, requiredTier = 1),
    MEDIUM("medium", "Medio", targetClues = 34, minClues = 28, requiredTier = 2),
    HARD("hard", "Difícil", targetClues = 28, minClues = 22, requiredTier = 3),
    EXPERT("expert", "Experto", targetClues = 24, minClues = 17, requiredTier = 4);

    companion object {
        fun fromId(id: String): Difficulty {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MEDIUM
        }
    }
}
