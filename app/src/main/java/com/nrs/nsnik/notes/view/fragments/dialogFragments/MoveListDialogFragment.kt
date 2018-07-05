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

package com.nrs.nsnik.notes.view.fragments.dialogFragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.util.events.FolderClickEvent
import com.nrs.nsnik.notes.view.adapters.FolderListAdapter
import com.nrs.nsnik.notes.view.fragments.ListFragment
import com.nrs.nsnik.notes.view.listeners.ItemPositionClickListener
import com.nrs.nsnik.notes.viewmodel.FolderViewModel
import com.nrs.nsnik.notes.viewmodel.NoteViewModel
import kotlinx.android.synthetic.main.recycler_view.*
import org.greenrobot.eventbus.EventBus
import kotlin.streams.toList

class MoveListDialogFragment : DialogFragment(), ItemPositionClickListener {

    private lateinit var folderListAdapter: FolderListAdapter
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var folderViewModel: FolderViewModel
    private lateinit var currentFolder: String
    private lateinit var parentFolder: String
    private var itemClickPosition: Int = -1
    private lateinit var itemType: ListFragment.ItemType
    private var folderList = mutableListOf<FolderEntity>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_move_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        val resources = activity?.resources

        currentFolder = arguments?.getString(resources?.getString(R.string.bundleEntityCurrentFolder), "noFolder")!!
        parentFolder = arguments?.getString(resources?.getString(R.string.bundleEntityParentFolder), "noFolder")!!
        itemType = ListFragment.ItemType.values()[arguments?.getInt(resources?.getString(R.string.bundleItemType), 0)!!]
        itemClickPosition = arguments?.getInt(resources?.getString(R.string.bundleItemToMovePosition), -1)!!

        folderViewModel = ViewModelProviders.of(this).get(FolderViewModel::class.java)
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)

        folderListAdapter = FolderListAdapter(this)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            itemAnimator = DefaultItemAnimator()
            adapter = folderListAdapter
        }

        folderViewModel.getFolders().observe(this, Observer {

            val folderEntity = FolderEntity()
            folderEntity.folderName = "noFolder"
            folderEntity.color = "#212121"
            folderEntity.locked = 0
            folderEntity.pinned = 0
            folderEntity.parentFolderName = "Root"
            folderList.add(folderEntity)

            folderList.addAll(it)

            folderList = folderList.stream().filter {
                it.parentFolderName == parentFolder
            }.filter {
                it.folderName != currentFolder
            }.toList().toMutableList()

            folderListAdapter.submitList(folderList)
        })
    }

    override fun itemClicked(position: Int) {
        EventBus.getDefault().post(FolderClickEvent(folderList[position].folderName!!, itemType, itemClickPosition))
        dismiss()
    }

}