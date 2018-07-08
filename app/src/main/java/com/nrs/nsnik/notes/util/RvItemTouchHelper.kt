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

package com.nrs.nsnik.notes.util

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nrs.nsnik.notes.view.listeners.ItemTouchListener
import java.util.*

class RvItemTouchHelper(private val mListener: ItemTouchListener) : ItemTouchHelper.Callback() {

    private val mDragFromList: MutableList<Int>
    private val mDragToList: MutableList<Int>
    private var mDragFromPosition = -1
    private var mDragToPosition = -1


    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    /*
    returns the maximum value from starting index
    indicates from which index should the
    id swapping start
     */
    private val max: Int
        get() {
            var max = mDragFromList[0]
            for (i in 1 until mDragFromList.size) {
                if (max < mDragFromList[i]) {
                    max = mDragFromList[i]
                }
            }
            return max
        }

    /*
    returns the minimum value from ending index
    indicates to which index should the
    id swapping end
    */
    private val min: Int
        get() {
            var min = mDragToList[0]
            for (i in 1 until mDragToList.size) {
                if (min > mDragToList[i]) {
                    min = mDragToList[i]
                }
            }
            return min
        }

    init {
        mDragFromList = ArrayList()
        mDragToList = ArrayList()
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        /*
        if view type if note or folder
        then only allow dragging or swiping
         */
        if (viewHolder.itemViewType === NOTES || viewHolder.itemViewType === FOLDER) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            return makeMovementFlags(dragFlags, swipeFlags)
        }
        return 0
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        /*
         as the item is dragged add each starting
         and ending position of that item in a list
         */
        mDragFromList.add(viewHolder.adapterPosition)
        mDragToList.add(target.adapterPosition)
        mListener.onItemMove(viewHolder.adapterPosition, target.adapterPosition, viewHolder, target)
        return true
    }

    /*
    clearView is called once the user have complete the dragging
    or swiping operations
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        /*
        get the maximum and minimum position i.e.
        the range upto whihc the item was dragged
         and pass that range to adapter
         */
        if (mDragFromList.size > 0) {
            mDragFromPosition = max
        }
        if (mDragToList.size > 0) {
            mDragToPosition = min
        }
        if (mDragFromPosition != -1 && mDragToPosition != -1) {
            mListener.onItemMoved(mDragFromPosition, mDragToPosition, recyclerView, viewHolder)
        }
        /*
        clear the list and values after passing to adapter
         */
        mDragFromList.clear()
        mDragToList.clear()
        mDragFromPosition = -1
        mDragToPosition = -1
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mListener.onItemDismiss(viewHolder.adapterPosition)
    }

    companion object {


        private val NOTES = 0
        private val FOLDER = 1
        private val HEADER = 2
    }
}
