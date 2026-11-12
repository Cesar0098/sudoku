package com.example.model

object MoveHistoryCodec {
    fun encode(moves: List<SudokuMove>): String = moves.joinToString("|") { move ->
        move.deltas.joinToString(";") { d ->
            listOf(d.row, d.col, d.oldValue, d.newValue,
                d.oldNotes.sorted().joinToString(""), d.newNotes.sorted().joinToString(""),
                if (d.oldIsError) 1 else 0, if (d.newIsError) 1 else 0).joinToString(",")
        }
    }

    fun decode(data: String): List<SudokuMove> = runCatching {
        if (data.isBlank()) emptyList() else data.split('|').map { move ->
            SudokuMove(move.split(';').map { encoded ->
                val p = encoded.split(',')
                require(p.size == 8)
                val r = p[0].toInt(); val c = p[1].toInt()
                val old = p[2].toInt(); val new = p[3].toInt()
                require(r in 0..8 && c in 0..8 && old in 0..9 && new in 0..9)
                fun notes(s: String) = s.map { it.digitToInt().also { n -> require(n in 1..9) } }.toSet()
                CellDelta(r, c, old, new, notes(p[4]), notes(p[5]), p[6] == "1", p[7] == "1")
            })
        }
    }.getOrDefault(emptyList())
}
