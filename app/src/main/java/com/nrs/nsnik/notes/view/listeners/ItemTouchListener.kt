/*
 *     Credit Card Security V1  Copyright (C) 2018  sid-sun
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

package com.nrs.nsnik.notes.view.listeners


import androidx.recyclerview.widget.RecyclerView

interface ItemTouchListener {

    /**
     * called any time item is moving
     *
     * @param fromPosition the starting position of the dragging operation
     * @param toPosition   the end position of the dragging operation
     * @param viewHolder   the viewholder of the starting item that was dragged
     * @param target       the viewholder of the item ao toPostition
     */
    fun onItemMove(fromPosition: Int, toPosition: Int, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder)

    /**
     * called once the item have finished moving
     *
     * @param fromPosition the starting position of the dragging operation
     * @param toPosition   the end position of the dragging operation
     * @param recyclerView the recycler View on which the operation happened
     * @param viewHolder   the viewholder of the starting item that was dragged
     */
    fun onItemMoved(fromPosition: Int, toPosition: Int, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder)

    /**
     * @param position the position at which item was swiped/removed
     */
    fun onItemDismiss(position: Int)
}
