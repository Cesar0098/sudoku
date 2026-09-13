package com.example.engine

import com.example.model.Difficulty

object SudokuSolver {
    private const val FULL_MASK = 0x3FE // bits 1..9 set

    private fun boxOf(row: Int, col: Int) = (row / 3) * 3 + (col / 3)

    /** All 27 units (9 rows, 9 cols, 9 boxes) as cell-index arrays, built once. */
    private val UNITS: List<IntArray> by lazy {
        val units = mutableListOf<IntArray>()
        for (r in 0 until 9) units.add(IntArray(9) { c -> r * 9 + c })
        for (c in 0 until 9) units.add(IntArray(9) { r -> r * 9 + c })
        for (b in 0 until 9) {
            val startRow = (b / 3) * 3
            val startCol = (b % 3) * 3
            units.add(IntArray(9) { k -> (startRow + k / 3) * 9 + (startCol + k % 3) })
        }
        units
    }

    fun isValid(grid: IntArray, row: Int, col: Int, num: Int): Boolean {
        for (c in 0 until 9) {
            if (c != col && grid[row * 9 + c] == num) return false
        }
        for (r in 0 until 9) {
            if (r != row && grid[r * 9 + col] == num) return false
        }
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val currR = startRow + r
                val currC = startCol + c
                if ((currR != row || currC != col) && grid[currR * 9 + currC] == num) {
                    return false
                }
            }
        }
        return true
    }

    private fun backtrackCount(grid: IntArray, limit: Int): Int {
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)

        for (i in 0 until 81) {
            val v = grid[i]
            if (v != 0) {
                val r = i / 9
                val c = i % 9
                val bit = 1 shl v
                rowMask[r] = rowMask[r] or bit
                colMask[c] = colMask[c] or bit
                boxMask[boxOf(r, c)] = boxMask[boxOf(r, c)] or bit
            }
        }

        var count = 0

        fun search(): Boolean {
            var bestIdx = -1
            var bestMask = 0
            var bestCount = 10

            for (i in 0 until 81) {
                if (grid[i] == 0) {
                    val r = i / 9
                    val c = i % 9
                    val used = rowMask[r] or colMask[c] or boxMask[boxOf(r, c)]
                    val avail = FULL_MASK and used.inv()
                    val cnt = Integer.bitCount(avail)

                    if (cnt == 0) return false // dead end
                    if (cnt < bestCount) {
                        bestCount = cnt
                        bestIdx = i
                        bestMask = avail
                        if (cnt == 1) break
                    }
                }
            }

            if (bestIdx == -1) {
                count++
                return count >= limit
            }

            val r = bestIdx / 9
            val c = bestIdx % 9
            val b = boxOf(r, c)
            var remaining = bestMask

            while (remaining != 0) {
                val bit = remaining and (-remaining)
                grid[bestIdx] = Integer.numberOfTrailingZeros(bit)

                rowMask[r] = rowMask[r] or bit
                colMask[c] = colMask[c] or bit
                boxMask[b] = boxMask[b] or bit

                if (search()) return true

                rowMask[r] = rowMask[r] and bit.inv()
                colMask[c] = colMask[c] and bit.inv()
                boxMask[b] = boxMask[b] and bit.inv()
                grid[bestIdx] = 0

                remaining = remaining and (remaining - 1)
            }
            return false
        }

        search()
        return count
    }

    fun solve(grid: IntArray): Boolean {
        return backtrackCount(grid, limit = 1) >= 1
    }

    fun countSolutions(grid: IntArray, limit: Int = 2): Int {
        return backtrackCount(grid.clone(), limit)
    }

    fun hasUniqueSolution(grid: IntArray): Boolean {
        if (analyze(grid).solvedByLogic) return true
        return countSolutions(grid, limit = 2) == 1
    }

    internal fun uniqueSolutionDifficulty(grid: IntArray, targetDifficulty: Difficulty): Difficulty? {
        // 1. FAST PATH: Chequeo de unicidad ultra-rápido por fuerza bruta primero.
        if (countSolutions(grid, limit = 2) != 1) return null

        // 2. Comprobación lógica
        val analysis = analyze(grid, targetDifficulty.requiredTier)
        val isUnique = analysis.solvedByLogic
        if (!isUnique) return Difficulty.EXPERT // Si la lógica falló, cae en guess/backtracking (Tier 4)

        val difficulty = tierToDifficulty(analysis.maxTierUsed)
        
        // 3. Early bail-out: Aborta si ya nos pasamos de la dificultad requerida
        if (difficulty.requiredTier > targetDifficulty.requiredTier) return null

        return difficulty
    }

    private class Analysis(val solvedByLogic: Boolean, val maxTierUsed: Int)

    private fun analyze(puzzle: IntArray, maxAllowedTier: Int = 4): Analysis {
        val grid = puzzle.clone()
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)
        val cand = IntArray(81)

        for (i in 0 until 81) {
            val v = grid[i]
            if (v != 0) {
                val r = i / 9
                val c = i % 9
                val bit = 1 shl v
                rowMask[r] = rowMask[r] or bit
                colMask[c] = colMask[c] or bit
                boxMask[boxOf(r, c)] = boxMask[boxOf(r, c)] or bit
            }
        }

        fun refreshCandidates() {
            for (i in 0 until 81) {
                if (grid[i] != 0) {
                    cand[i] = 0
                    continue
                }
                val r = i / 9
                val c = i % 9
                cand[i] = FULL_MASK and (rowMask[r] or colMask[c] or boxMask[boxOf(r, c)]).inv()
            }
        }

        refreshCandidates()

        fun place(i: Int, num: Int) {
            val r = i / 9
            val c = i % 9
            val bit = 1 shl num
            grid[i] = num
            rowMask[r] = rowMask[r] or bit
            colMask[c] = colMask[c] or bit
            boxMask[boxOf(r, c)] = boxMask[boxOf(r, c)] or bit
            refreshCandidates()
        }

        fun tryNakedSingles(): Boolean {
            for (i in 0 until 81) {
                if (grid[i] == 0 && Integer.bitCount(cand[i]) == 1) {
                    place(i, Integer.numberOfTrailingZeros(cand[i]))
                    return true
                }
            }
            return false
        }

        fun tryHiddenSingles(): Boolean {
            for (unit in UNITS) {
                for (num in 1..9) {
                    val bit = 1 shl num
                    var found = -1
                    var count = 0
                    for (i in unit) {
                        if (grid[i] == 0 && (cand[i] and bit) != 0) {
                            count++
                            found = i
                            if (count > 1) break
                        }
                    }
                    if (count == 1) {
                        place(found, num)
                        return true
                    }
                }
            }
            return false
        }

        fun tryLockedCandidates(): Boolean {
            for (b in 0 until 9) {
                val startRow = (b / 3) * 3
                val startCol = (b % 3) * 3
                for (num in 1..9) {
                    val bit = 1 shl num
                    var count = 0
                    var rMask = 0
                    var cMask = 0
                    for (r in 0 until 3) {
                        for (c in 0 until 3) {
                            val i = (startRow + r) * 9 + (startCol + c)
                            if (grid[i] == 0 && (cand[i] and bit) != 0) {
                                count++
                                rMask = rMask or (1 shl (startRow + r))
                                cMask = cMask or (1 shl (startCol + c))
                            }
                        }
                    }
                    if (count in 2..3) {
                        var progressed = false
                        if (Integer.bitCount(rMask) == 1) {
                            val r = Integer.numberOfTrailingZeros(rMask)
                            for (c in 0 until 9) {
                                if (c < startCol || c >= startCol + 3) {
                                    val i = r * 9 + c
                                    if (grid[i] == 0 && (cand[i] and bit) != 0) {
                                        cand[i] = cand[i] and bit.inv()
                                        progressed = true
                                    }
                                }
                            }
                        } else if (Integer.bitCount(cMask) == 1) {
                            val c = Integer.numberOfTrailingZeros(cMask)
                            for (r in 0 until 9) {
                                if (r < startRow || r >= startRow + 3) {
                                    val i = r * 9 + c
                                    if (grid[i] == 0 && (cand[i] and bit) != 0) {
                                        cand[i] = cand[i] and bit.inv()
                                        progressed = true
                                    }
                                }
                            }
                        }
                        if (progressed) return true
                    }
                }
            }
            return false
        }

        fun tryNakedPairs(): Boolean {
            for (unit in UNITS) {
                for (i in 0 until 8) {
                    val a = unit[i]
                    if (grid[a] != 0 || Integer.bitCount(cand[a]) != 2) continue
                    for (j in i + 1 until 9) {
                        val b = unit[j]
                        if (cand[a] == cand[b]) {
                            val mask = cand[a]
                            var progressed = false
                            for (k in 0 until 9) {
                                val c = unit[k]
                                if (c != a && c != b && grid[c] == 0 && (cand[c] and mask) != 0) {
                                    cand[c] = cand[c] and mask.inv()
                                    progressed = true
                                }
                            }
                            if (progressed) return true
                        }
                    }
                }
            }
            return false
        }

        fun tryHiddenPairs(): Boolean {
            for (unit in UNITS) {
                val masks = IntArray(10)
                for (k in 0 until 9) {
                    val i = unit[k]
                    if (grid[i] != 0) continue
                    val cMask = cand[i]
                    for (num in 1..9) {
                        if ((cMask and (1 shl num)) != 0) masks[num] = masks[num] or (1 shl k)
                    }
                }
                for (n1 in 1..8) {
                    if (Integer.bitCount(masks[n1]) != 2) continue
                    for (n2 in n1 + 1..9) {
                        if (masks[n1] == masks[n2]) {
                            val combinedNumMask = (1 shl n1) or (1 shl n2)
                            var progressed = false
                            val posMask = masks[n1]
                            val k1 = Integer.numberOfTrailingZeros(posMask)
                            val k2 = 31 - Integer.numberOfLeadingZeros(posMask)
                            val i1 = unit[k1]
                            val i2 = unit[k2]
                            
                            if (cand[i1] != combinedNumMask) {
                                cand[i1] = cand[i1] and combinedNumMask
                                progressed = true
                            }
                            if (cand[i2] != combinedNumMask) {
                                cand[i2] = cand[i2] and combinedNumMask
                                progressed = true
                            }
                            if (progressed) return true
                        }
                    }
                }
            }
            return false
        }

        fun tryXWing(): Boolean {
            for (num in 1..9) {
                val bit = 1 shl num
                
                // Filas
                val rowCols = IntArray(9) { -1 }
                for (r in 0 until 9) {
                    var cMask = 0
                    for (c in 0 until 9) {
                        if (grid[r * 9 + c] == 0 && (cand[r * 9 + c] and bit) != 0) cMask = cMask or (1 shl c)
                    }
                    if (Integer.bitCount(cMask) == 2) rowCols[r] = cMask
                }
                for (r1 in 0 until 8) {
                    if (rowCols[r1] == -1) continue
                    for (r2 in r1 + 1 until 9) {
                        if (rowCols[r1] == rowCols[r2]) {
                            val cMask = rowCols[r1]
                            val c1 = Integer.numberOfTrailingZeros(cMask)
                            val c2 = 31 - Integer.numberOfLeadingZeros(cMask)
                            var progressed = false
                            for (r in 0 until 9) {
                                if (r == r1 || r == r2) continue
                                val i1 = r * 9 + c1
                                val i2 = r * 9 + c2
                                if (grid[i1] == 0 && (cand[i1] and bit) != 0) {
                                    cand[i1] = cand[i1] and bit.inv()
                                    progressed = true
                                }
                                if (grid[i2] == 0 && (cand[i2] and bit) != 0) {
                                    cand[i2] = cand[i2] and bit.inv()
                                    progressed = true
                                }
                            }
                            if (progressed) return true
                        }
                    }
                }

                // Columnas
                val colRows = IntArray(9) { -1 }
                for (c in 0 until 9) {
                    var rMask = 0
                    for (r in 0 until 9) {
                        if (grid[r * 9 + c] == 0 && (cand[r * 9 + c] and bit) != 0) rMask = rMask or (1 shl r)
                    }
                    if (Integer.bitCount(rMask) == 2) colRows[c] = rMask
                }
                for (c1 in 0 until 8) {
                    if (colRows[c1] == -1) continue
                    for (c2 in c1 + 1 until 9) {
                        if (colRows[c1] == colRows[c2]) {
                            val rMask = colRows[c1]
                            val r1 = Integer.numberOfTrailingZeros(rMask)
                            val r2 = 31 - Integer.numberOfLeadingZeros(rMask)
                            var progressed = false
                            for (c in 0 until 9) {
                                if (c == c1 || c == c2) continue
                                val i1 = r1 * 9 + c
                                val i2 = r2 * 9 + c
                                if (grid[i1] == 0 && (cand[i1] and bit) != 0) {
                                    cand[i1] = cand[i1] and bit.inv()
                                    progressed = true
                                }
                                if (grid[i2] == 0 && (cand[i2] and bit) != 0) {
                                    cand[i2] = cand[i2] and bit.inv()
                                    progressed = true
                                }
                            }
                            if (progressed) return true
                        }
                    }
                }
            }
            return false
        }

        var maxTier = 0

        while (true) {
            if (maxTier > maxAllowedTier) break // Bail-out temprano

            if (tryNakedSingles() || tryHiddenSingles()) {
                maxTier = maxOf(maxTier, 1)
                continue
            }
            if (tryLockedCandidates()) {
                maxTier = maxOf(maxTier, 2)
                continue
            }
            if (tryNakedPairs() || tryHiddenPairs()) {
                maxTier = maxOf(maxTier, 3)
                continue
            }
            if (tryXWing()) {
                maxTier = maxOf(maxTier, 4)
                continue
            }
            break
        }

        val solvedByLogic = (0 until 81).none { grid[it] == 0 }
        return Analysis(solvedByLogic, if (solvedByLogic) maxTier else 4)
    }

    fun evaluateDifficulty(puzzle: IntArray): Difficulty {
        return tierToDifficulty(analyze(puzzle).maxTierUsed)
    }

    private fun tierToDifficulty(tier: Int): Difficulty {
        return when (tier) {
            0, 1 -> Difficulty.EASY
            2 -> Difficulty.MEDIUM
            3 -> Difficulty.HARD
            else -> Difficulty.EXPERT
        }
    }
}