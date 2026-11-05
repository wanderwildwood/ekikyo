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
 * A hexagram (or, with [rows] = 3, a trigram) drawn as solid bars, the first line at the foot.
 * [lines] are cast values (6 to 9) from the bottom up, and may be fewer than [rows] while a cast
 * is under way: with [slots] the places not cast yet are drawn as thin outlines, so the shape of
 * what is coming shows; without, they are left empty. [marks] puts the old lines' signs to the
 * right, ○ on a whole line that will change and × on a broken one. Black on white only, for the
 * panel. The gap between bars is as tall as a bar and the break in a broken line a quarter of its
 * width, so even a small figure reads as lines and not as stripes.
 */
@Composable
fun Figure(
    lines: List<Int>,
    width: Dp,
    modifier: Modifier = Modifier,
    marks: Boolean = true,
    description: String? = null,
    rows: Int = 6,
    slots: Boolean = false,
) {
    val ink = MaterialTheme.colorScheme.onSurface
    val bar = width / 9f
    val gap = bar
    val markRoom = if (marks) bar * 2.4f else 0.dp
    val height = bar * rows + gap * (rows - 1)
    Canvas(
        modifier
            .size(width + markRoom, height)
            .let { m -> if (description != null) m.semantics { contentDescription = description } else m },
    ) {
        val w = width.toPx()
        val t = bar.toPx()
        val g = gap.toPx()
        val split = w * 0.24f
        val hair = 1.dp.toPx()
        for (i in 0 until rows) {
            val top = (rows - 1 - i) * (t + g)
            val v = lines.getOrNull(i)
            if (v == null) {
                if (slots) {
                    drawRect(ink, Offset(hair / 2, top + hair / 2), Size(w - hair, t - hair), style = Stroke(hair))
                }
                continue
            }
            if (Line.yang(v)) {
                drawRect(ink, Offset(0f, top), Size(w, t))
            } else {
                val half = (w - split) / 2
                drawRect(ink, Offset(0f, top), Size(half, t))
                drawRect(ink, Offset(w - half, top), Size(half, t))
            }
            if (marks && Line.changing(v)) {
                val cx = w + markRoom.toPx() * 0.6f
                val cy = top + t / 2
                val r = t * 0.6f
                val stroke = Stroke(width = t * 0.26f)
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

/** Lines of a hexagram (or trigram) by its bits, all young: for a figure shown on its own. */
fun youngLines(bits: Int, rows: Int = 6): List<Int> =
    List(rows) { if ((bits shr it) and 1 == 1) Line.YOUNG_YANG else Line.YOUNG_YIN }

/** A small figure for lists. */
val SmallFigure: Dp = 40.dp
