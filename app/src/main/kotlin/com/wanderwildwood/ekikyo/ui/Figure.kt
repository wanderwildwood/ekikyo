package com.wanderwildwood.ekikyo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wanderwildwood.ekikyo.iching.Line

/**
 * A hexagram drawn as six solid bars, the first line at the foot. [lines] are cast values (6 to
 * 9) from the bottom up, and may be fewer than six while a cast is under way: the places not
 * cast yet are left empty. [marks] puts the old lines' signs to the right, ○ on a whole line
 * that will change and × on a broken one. Nothing but black on white, no grey, for the panel.
 */
@Composable
fun Figure(lines: List<Int>, width: Dp, modifier: Modifier = Modifier, marks: Boolean = true, description: String? = null) {
    val ink = MaterialTheme.colorScheme.onSurface
    val bar = width / 9f
    val gap = bar * 0.8f
    val markRoom = if (marks) bar * 2.2f else 0.dp
    val height = bar * 6 + gap * 5
    Canvas(
        modifier
            .size(width + markRoom, height)
            .let { m -> if (description != null) m.semantics { contentDescription = description } else m },
    ) {
        val w = width.toPx()
        val t = bar.toPx()
        val g = gap.toPx()
        val split = w * 0.2f
        for (i in lines.indices) {
            val v = lines[i]
            val top = (5 - i) * (t + g)
            if (Line.yang(v)) {
                drawRect(ink, Offset(0f, top), Size(w, t))
            } else {
                val half = (w - split) / 2
                drawRect(ink, Offset(0f, top), Size(half, t))
                drawRect(ink, Offset(w - half, top), Size(half, t))
            }
            if (marks && Line.changing(v)) {
                val cx = w + markRoom.toPx() * 0.62f
                val cy = top + t / 2
                val r = t * 0.62f
                val stroke = Stroke(width = t * 0.28f)
                if (v == Line.OLD_YANG) {
                    drawCircle(ink, r, Offset(cx, cy), style = stroke)
                } else {
                    drawLine(ink, Offset(cx - r, cy - r), Offset(cx + r, cy + r), stroke.width)
                    drawLine(ink, Offset(cx - r, cy + r), Offset(cx + r, cy - r), stroke.width)
                }
            }
        }
    }
}

/** Lines of a hexagram by its bits, all young: for a hexagram shown on its own. */
fun youngLines(bits: Int): List<Int> = List(6) { if ((bits shr it) and 1 == 1) Line.YOUNG_YANG else Line.YOUNG_YIN }

/** A small figure for lists. */
val SmallFigure: Dp = 36.dp
