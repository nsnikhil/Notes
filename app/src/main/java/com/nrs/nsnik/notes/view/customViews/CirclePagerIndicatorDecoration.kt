/*
 *     Notes  Copyright (C) 2018  Nikhil Soni
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <http://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <http://www.gnu.org/philosophy/why-not-lgpl.html>.
 */

package com.nrs.nsnik.notes.view.customViews

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CirclePagerIndicatorDecoration : RecyclerView.ItemDecoration() {

    private val colorActive = -0x1000000
    private val colorInactive = 0x66000000

    private val density = Resources.getSystem().displayMetrics.density
    private val indicatorHeight = (density * 16).toInt()
    private val indicatorStrokeWidth = density * 2
    private val indicatorItemLength = density * 16
    private val indicatorItemPadding = density * 4
    private val interpolator = AccelerateDecelerateInterpolator()
    private val paint = Paint()

    init {
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = indicatorStrokeWidth
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val itemCount = parent.adapter!!.itemCount
        val totalLength = indicatorItemLength * itemCount
        val paddingBetweenItems = Math.max(0, itemCount - 1) * indicatorItemPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f
        val indicatorPosY = parent.height - indicatorHeight / 2f

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount)

        val layoutManager = parent.layoutManager as LinearLayoutManager?
        val activePosition = layoutManager!!.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        val activeChild = layoutManager.findViewByPosition(activePosition)
        val left = activeChild!!.left
        val width = activeChild.width

        val progress = interpolator.getInterpolation(left * -1 / width.toFloat())
        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress, itemCount)
    }

    private fun drawInactiveIndicators(c: Canvas, indicatorStartX: Float, indicatorPosY: Float, itemCount: Int) {
        paint.color = colorInactive

        val itemWidth = indicatorItemLength + indicatorItemPadding

        var start = indicatorStartX
        for (i in 0 until itemCount) {
            c.drawLine(start, indicatorPosY, start + indicatorItemLength, indicatorPosY, paint)
            start += itemWidth
        }
    }

    private fun drawHighlights(c: Canvas, indicatorStartX: Float, indicatorPosY: Float, highlightPosition: Int, progress: Float, itemCount: Int) {
        paint.color = colorActive

        val itemWidth = indicatorItemLength + indicatorItemPadding

        if (progress == 0f) {
            val highlightStart = indicatorStartX + itemWidth * highlightPosition
            c.drawLine(highlightStart, indicatorPosY, highlightStart + indicatorItemLength, indicatorPosY, paint)
        } else {
            var highlightStart = indicatorStartX + itemWidth * highlightPosition
            val partialLength = indicatorItemLength * progress

            c.drawLine(highlightStart + partialLength, indicatorPosY, highlightStart + indicatorItemLength, indicatorPosY, paint)
            if (highlightPosition < itemCount - 1) {
                highlightStart += itemWidth
                c.drawLine(highlightStart, indicatorPosY, highlightStart + partialLength, indicatorPosY, paint)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = indicatorHeight
    }
}