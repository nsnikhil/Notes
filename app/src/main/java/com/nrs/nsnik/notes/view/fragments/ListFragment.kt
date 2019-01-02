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

package com.nrs.nsnik.notes.view.fragments

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxPopupMenu
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.util.AppUtil
import com.nrs.nsnik.notes.util.FileUtil
import com.nrs.nsnik.notes.util.PasswordUtil
import com.nrs.nsnik.notes.util.RvItemTouchHelper
import com.nrs.nsnik.notes.util.events.FolderClickEvent
import com.nrs.nsnik.notes.util.events.PasswordEvent
import com.nrs.nsnik.notes.view.adapters.NotesAdapter
import com.nrs.nsnik.notes.view.fragments.dialogFragments.ActionAlertDialog
import com.nrs.nsnik.notes.view.fragments.dialogFragments.CreateFolderDialog
import com.nrs.nsnik.notes.view.fragments.dialogFragments.MoveListDialogFragment
import com.nrs.nsnik.notes.view.fragments.dialogFragments.PasswordDialogFragment
import com.nrs.nsnik.notes.view.listeners.ListHeaderClickListener
import com.nrs.nsnik.notes.view.listeners.NoteItemClickListener
import com.nrs.nsnik.notes.viewmodel.FolderViewModel
import com.nrs.nsnik.notes.viewmodel.NoteViewModel
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.empty_state.*
import kotlinx.android.synthetic.main.fab_reveal.*
import kotlinx.android.synthetic.main.fragment_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.util.*

class ListFragment : Fragment(), NoteItemClickListener, ListHeaderClickListener {

    private lateinit var mNoteViewModel: NoteViewModel
    private lateinit var mFolderViewModel: FolderViewModel

    private var mNotesList: List<NoteEntity> = mutableListOf()
    private var mFolderList: List<FolderEntity> = mutableListOf()

    private var mFolderName: String = "noFolder"
    private lateinit var mNotesAdapter: NotesAdapter

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var isRevealed: Boolean = false

    private lateinit var searchView: SearchView
    private lateinit var searchItem: MenuItem
    private var sortType: SortType = SortType.PIN

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppUtil.goToIntro(activity)
        initialize()
        listeners()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main_menu, menu)
        searchView = menu?.findItem(R.id.menuMainSearch)?.actionView as SearchView
        searchItem = menu.findItem(R.id.menuMainSearch)
        searchView.setSearchableInfo((activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(activity!!.componentName))
        menuListener()
    }

    @SuppressLint("CheckResult")
    private fun menuListener() {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query!!.isNotEmpty()) setSearchViewModel(query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()) setSearchViewModel(newText.toString())
                return true
            }

        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem?): Boolean {
                setFolderViewModel(mFolderName, sortType)
                setNoteViewModel(mFolderName, sortType)
                return true
            }

        })

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuMainSearch -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initialize() {

        if (arguments != null)
            mFolderName = arguments?.getString(activity?.resources?.getString(R.string.bundleListFragmentFolderName), "noFolder")!!

        mNoteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        mFolderViewModel = ViewModelProviders.of(this).get(FolderViewModel::class.java)

        //Setting up recycler view
        mNotesAdapter = NotesAdapter(activity!!, mNotesList, mFolderList, this, this)

        commonList.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            hasFixedSize()
            itemAnimator = DefaultItemAnimator()
            adapter = mNotesAdapter
        }

        val touchHelper = ItemTouchHelper(RvItemTouchHelper(mNotesAdapter))
        touchHelper.attachToRecyclerView(commonList)

        setFolderViewModel(mFolderName, sortType)
        setNoteViewModel(mFolderName, sortType)
    }

    private fun listeners() {
        compositeDisposable.addAll(
                RxView.clicks(fabAdd).subscribe({
                    if (isRevealed) animate(135f, 0f, R.anim.jump_to_down)
                    else animate(0f, 135f, R.anim.jump_from_down)
                }, { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(fabAddNote).subscribe {
                    animate(135f, 0f, R.anim.jump_to_down)
                    openNoteEditor(mFolderName, null)
                },

                RxView.clicks(fabAddFolder).subscribe {
                    animate(135f, 0f, R.anim.jump_to_down)
                    val bundle = Bundle()
                    bundle.putString(activity?.resources?.getString(R.string.bundleCreateFolderParentFolder), mFolderName)
                    showCreateFolderDialog(bundle)
                }
        )
    }

    private fun showCreateFolderDialog(bundle: Bundle) {
        val dialog = CreateFolderDialog()
        dialog.arguments = bundle
        dialog.show(fragmentManager, "createFolderDialog")
    }

    private fun setNoteViewModel(folderName: String, sortType: SortType) {
        when (sortType) {
            SortType.DATE -> mNoteViewModel.getNoteByFolderName(folderName).observe(this, androidx.lifecycle.Observer { swapNotes(it) })
            SortType.PIN -> mNoteViewModel.getNoteByFolderNameOrdered(folderName).observe(this, androidx.lifecycle.Observer { swapNotes(it) })
            SortType.LOCK -> mNoteViewModel.getNoteByFolderNameOrderedLock(folderName).observe(this, androidx.lifecycle.Observer { swapNotes(it) })
        }
    }

    private fun setFolderViewModel(folderName: String, sortType: SortType) {
        when (sortType) {
            SortType.DATE -> mFolderViewModel.getFolderByParent(folderName).observe(this, androidx.lifecycle.Observer { swapFolder(it) })
            SortType.PIN -> mFolderViewModel.getFolderByParentOrdered(folderName).observe(this, androidx.lifecycle.Observer { swapFolder(it) })
            SortType.LOCK -> mFolderViewModel.getFolderByParentOrderedLock(folderName).observe(this, androidx.lifecycle.Observer { swapFolder(it) })
        }
    }

    private fun setSearchViewModel(query: String) {
        mFolderViewModel.searchFolder(query).observe(this, androidx.lifecycle.Observer { swapFolder(it) })
        mNoteViewModel.searchNote(query).observe(this, androidx.lifecycle.Observer { swapNotes(it) })
    }

    private fun swapFolder(folderEntityList: List<FolderEntity>?) {
        if (folderEntityList == null) return
        mFolderList = folderEntityList
        mNotesAdapter.updateFolderList(folderEntityList)
        setEmpty()
    }

    private fun swapNotes(noteEntityList: List<NoteEntity>?) {
        if (noteEntityList == null) return
        mNotesList = noteEntityList
        mNotesAdapter.updateNotesList(mNotesList)
        setEmpty()
    }

    private fun setEmpty() {
        emptyState.visibility = if (mNotesList.isEmpty() && mFolderList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openFolder(position: Int) {
        val startPos = 1
        val currPos = position - startPos
        if (mFolderList[currPos].locked == 1) showPassWordDialog(ItemType.FOLDER, currPos, EventType.OPEN)
        else openFolderWithBundle(currPos)
    }

    private fun openFolderWithBundle(position: Int) {
        val bundle = Bundle()
        bundle.putString(activity?.resources?.getString(R.string.bundleListFragmentFolderName), mFolderList[position].folderName!!)
        activity?.findNavController(R.id.mainNavHost)?.navigate(R.id.navItemNotes, bundle)
    }

    @Throws(Exception::class)
    private fun openNote(position: Int) {
        val startPos = mFolderList.size + 2
        val currPos = position - startPos
        if (mNotesList[currPos].locked == 1) showPassWordDialog(ItemType.NOTES, currPos, EventType.OPEN)
        else openNoteEditor(null, mNotesList[currPos])
    }

    private fun showPassWordDialog(itemType: ItemType, position: Int, eventType: EventType) {
        val bundle = Bundle()
        bundle.putInt(activity?.resources?.getString(R.string.bundleItemType), itemType.ordinal)
        bundle.putInt(activity?.resources?.getString(R.string.bundleItemEvent), eventType.ordinal)
        bundle.putInt(activity?.resources?.getString(R.string.bundleItemPosition), position)
        val dialog = PasswordDialogFragment()
        dialog.arguments = bundle
        dialog.show(fragmentManager, "password")
    }

    private fun openNoteEditor(folderName: String?, noteEntity: NoteEntity?) {

        val bundle = Bundle()

        if (folderName != null)
            bundle.putString(activity?.resources?.getString(R.string.bundleListFragmentFolderName), mFolderName)

        if (noteEntity != null) {
            val byteArray: ByteArray = ByteBufferSerial().toByteArray(noteEntity, NoteEntity.SERIALIZER)
            bundle.putByteArray(activity?.resources?.getString(R.string.bundleNoteEntity), byteArray)
        }

        fabAddNote.findNavController().navigate(R.id.listToAddNewNote, bundle)
    }


    override fun onClick(position: Int, itemViewType: Int) {
        when (itemViewType) {
            0 -> openNote(position)
            1 -> openFolder(position)
        }
    }

    override fun onLongClick(position: Int, itemViewType: Int, view: View) {
        when (itemViewType) {
            0 -> inflatePopUpMenu(position, itemViewType, view)
            1 -> inflatePopUpMenu(position, itemViewType, view)
        }
    }

    private fun inflatePopUpMenu(position: Int, itemViewType: Int, view: View) {
        val folderPosition = position - 1
        val notePosition = position - (mFolderList.size + 2)

        val popupMenu = PopupMenu(activity, view, Gravity.END)
        popupMenu.inflate(R.menu.pop_up_menu)
        RxPopupMenu.itemClicks(popupMenu).subscribe {
            when (it.itemId) {
                R.id.popUpStar -> {
                    if (itemViewType == 0) {
                        val noteEntity = mNotesList[notePosition]
                        updateNote((activity?.application as MyApplication).fileUtil,
                                noteEntity,
                                if (noteEntity.pinned == 0) 1 else 0,
                                noteEntity.locked,
                                noteEntity.folderName!!
                        )
                    } else {
                        val folderEntity = mFolderList[folderPosition]
                        mFolderViewModel.changeFolderPinStatus(folderEntity.uid, if (folderEntity.pinned == 0) 1 else 0)
                    }
                }
                R.id.popUpLock -> {
                    if (itemViewType == 0)
                        if (mNotesList[notePosition].locked == 0) setLock(ItemType.NOTES, notePosition)
                        else showPassWordDialog(ItemType.NOTES, notePosition, EventType.LOCK)
                    else
                        if (mFolderList[folderPosition].locked == 0) setLock(ItemType.FOLDER, folderPosition)
                        else showPassWordDialog(ItemType.FOLDER, folderPosition, EventType.LOCK)
                }
                R.id.popUpEdit -> {
                    when {
                        itemViewType == 0 -> openNote(position)
                        mFolderList[folderPosition].locked == 0 -> editFolder(folderPosition)
                        else -> showPassWordDialog(ItemType.FOLDER, folderPosition, EventType.EDIT)
                    }
                }
                R.id.popUpMove -> {
                    if (itemViewType == 0)
                        if (mNotesList[notePosition].locked == 0) openMoveListDialog(
                                ItemType.NOTES,
                                mNotesList[notePosition].folderName!!,
                                mNotesList[notePosition].folderName!!,
                                notePosition)
                        else showPassWordDialog(ItemType.NOTES, notePosition, EventType.MOVE)
                    else
                        if (mFolderList[folderPosition].locked == 0) openMoveListDialog(
                                ItemType.FOLDER,
                                mFolderList[folderPosition].folderName!!,
                                mFolderList[folderPosition].parentFolderName!!,
                                folderPosition)
                        else showPassWordDialog(ItemType.FOLDER, folderPosition, EventType.MOVE)
                }
                R.id.popUpShare -> {
                    if (itemViewType == 0)
                        if (mNotesList[notePosition].locked == 0) shareNote(mNotesList[notePosition])
                        else showPassWordDialog(ItemType.NOTES, notePosition, EventType.SHARE)
                    else Toast.makeText(activity, "Folder sharing not supported as of now :(", Toast.LENGTH_LONG).show()
                }
                R.id.popUpDelete -> {
                    if (itemViewType == 0)
                        if (mNotesList[notePosition].locked == 0) deleteNote(notePosition)
                        else showPassWordDialog(ItemType.NOTES, notePosition, EventType.DELETE)
                    else
                        if (mFolderList[folderPosition].locked == 0) deleteFolder(folderPosition)
                        else showPassWordDialog(ItemType.FOLDER, folderPosition, EventType.DELETE)
                }
            }
        }
        popupMenu.show()
    }

    private fun shareNote(noteEntity: NoteEntity) {
        (activity?.application as MyApplication).fileUtil.getLiveNote(noteEntity.fileName!!).observe(this, androidx.lifecycle.Observer {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, it.noteContent)
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        })
    }

    private fun updateNote(fileUtil: FileUtil, noteEntity: NoteEntity, pinned: Int, locked: Int, folderName: String) {
        fileUtil.getLiveNote(noteEntity.fileName!!).observe(this, androidx.lifecycle.Observer {
            it.uid = noteEntity.uid
            it.title = it.title
            it.noteContent = it.noteContent
            it.folderName = folderName
            it.fileName = it.fileName
            it.color = it.color
            it.dateModified = Calendar.getInstance().time
            it.imageList = it.imageList
            it.audioList = it.audioList
            it.checkList = it.checkList
            it.pinned = pinned
            it.locked = locked
            it.hasReminder = it.hasReminder
            mNoteViewModel.updateNote(it)
        })
    }

    private fun setLock(itemType: ItemType, position: Int) {
        if (PasswordUtil.checkLock((activity?.applicationContext as MyApplication).sharedPreferences, activity!!, fragmentManager!!, "password")) {
            when (itemType) {
                ListFragment.ItemType.NOTES -> {
                    val noteEntity = mNotesList[position]
                    updateNote((activity?.application as MyApplication).fileUtil,
                            noteEntity,
                            noteEntity.pinned,
                            if (noteEntity.locked == 0) 1 else 0,
                            noteEntity.folderName!!
                    )
                }
                ListFragment.ItemType.FOLDER -> {
                    val folderEntity = mFolderList[position]
                    mFolderViewModel.changeFolderLockStatus(folderEntity.uid, if (folderEntity.locked == 0) 1 else 0)
                }
            }
        }
    }

    private fun openMoveListDialog(itemType: ItemType, currentFolder: String, parentFolder: String, clickPosition: Int) {
        val bundle = Bundle()
        bundle.putInt(activity?.resources?.getString(R.string.bundleItemType), itemType.ordinal)
        bundle.putString(activity?.resources?.getString(R.string.bundleEntityCurrentFolder), currentFolder)
        bundle.putString(activity?.resources?.getString(R.string.bundleEntityParentFolder), parentFolder)
        bundle.putInt(activity?.resources?.getString(R.string.bundleItemToMovePosition), clickPosition)
        val dialog = MoveListDialogFragment()
        dialog.arguments = bundle
        dialog.show(fragmentManager, "move")
    }

    private fun deleteNote(position: Int) {
        createDeleteDialog(
                activity?.resources?.getString(R.string.deleteSingleNoteWarning)!!,
                DialogInterface.OnClickListener { dialogInterface, i ->
                    val noteEntity = mNotesList[position]
                    mNoteViewModel.deleteNote(noteEntity)
                    (activity?.application as MyApplication).fileUtil.deleteNoteResources(noteEntity)
                })
    }

    private fun deleteFolder(position: Int) {
        createDeleteDialog(
                activity?.resources?.getString(R.string.deleteSingleFolderWarning)!!,
                DialogInterface.OnClickListener { dialogInterface, i ->
                    val fileUtil = (activity?.application as MyApplication).fileUtil
                    mNoteViewModel.getNoteByFolderName(mFolderList[position].folderName!!).observe(this, androidx.lifecycle.Observer {
                        it.forEach {
                            fileUtil.deleteNoteResources(it)
                            mNoteViewModel.deleteNote(it)
                        }
                    })
                    mFolderViewModel.deleteFolder(mFolderList[position])
                }
        )
    }

    private fun editFolder(position: Int) {
        val bundle = Bundle()
        val byteArray: ByteArray = ByteBufferSerial().toByteArray(mFolderList[position], FolderEntity.SERIALIZER)
        bundle.putByteArray(activity?.resources?.getString(R.string.bundleFolderEntity), byteArray)
        showCreateFolderDialog(bundle)
    }

    private fun createDeleteDialog(message: String, positiveClick: DialogInterface.OnClickListener) {
        val resources = activity?.resources
        ActionAlertDialog.showDialog(
                activity!!,
                resources?.getString(R.string.warning)!!,
                message,
                resources.getString(R.string.yes),
                resources.getString(R.string.no),
                positiveClick,
                DialogInterface.OnClickListener { dialogInterface, i ->

                }
        )
    }

    private fun animate(fromDegree: Float, toDegree: Float, animation: Int) {
        val rotateAnimation = RotateAnimation(fromDegree, toDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.duration = 50
        rotateAnimation.fillAfter = true
        rotateAnimation.isFillEnabled = true
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            val transitionAnimation = AnimationUtils.loadAnimation(activity, animation)

            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                listBackground?.visibility = if (isRevealed) View.GONE else View.VISIBLE
                transitionAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        isRevealed = changeFabVisibility()
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                fabAddFolderContainer?.startAnimation(transitionAnimation)
                fabAddNoteContainer?.startAnimation(transitionAnimation)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        fabAdd?.startAnimation(rotateAnimation)
    }

    private fun changeFabVisibility(): Boolean {
        return if (isRevealed) {
            fabAddNoteContainer.visibility = View.INVISIBLE
            fabAddFolderContainer.visibility = View.INVISIBLE
            false
        } else {
            fabAddNoteContainer.visibility = View.VISIBLE
            fabAddFolderContainer.visibility = View.VISIBLE
            true
        }
    }

    override fun headerClick(itemType: ItemType, view: View) {
        when (itemType) {
            ItemType.NOTES -> inflateMorePopUpMenu(itemType, view)
            ItemType.FOLDER -> inflateMorePopUpMenu(itemType, view)
        }
    }

    private fun inflateMorePopUpMenu(itemType: ItemType, view: View) {
        val popupMenu = PopupMenu(activity, view, Gravity.START)
        popupMenu.inflate(R.menu.sort_list)
        RxPopupMenu.itemClicks(popupMenu).subscribe {
            when (it.itemId) {
                R.id.sortPopUpDate -> if (itemType == ItemType.FOLDER) setFolderViewModel(mFolderName, SortType.DATE) else setNoteViewModel(mFolderName, SortType.DATE)
                R.id.sortPopUpPin -> if (itemType == ItemType.FOLDER) setFolderViewModel(mFolderName, SortType.PIN) else setNoteViewModel(mFolderName, SortType.PIN)
                R.id.sortPopUpLock -> if (itemType == ItemType.FOLDER) setFolderViewModel(mFolderName, SortType.LOCK) else setNoteViewModel(mFolderName, SortType.LOCK)
            }
        }
        popupMenu.show()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onPasswordEvent(passwordEvent: PasswordEvent) {
        when (passwordEvent.itemType) {
            ItemType.NOTES ->
                when (passwordEvent.eventType) {
                    EventType.OPEN -> openNoteEditor(null, mNotesList[passwordEvent.position])
                    EventType.EDIT -> openNoteEditor(null, mNotesList[passwordEvent.position])
                    EventType.DELETE -> deleteNote(passwordEvent.position)
                    EventType.MOVE -> openMoveListDialog(
                            ItemType.NOTES,
                            mNotesList[passwordEvent.position].folderName!!,
                            mNotesList[passwordEvent.position].folderName!!,
                            passwordEvent.position)
                    EventType.LOCK -> setLock(ItemType.NOTES, passwordEvent.position)
                    EventType.SHARE -> shareNote(mNotesList[passwordEvent.position])
                }
            ItemType.FOLDER ->
                when (passwordEvent.eventType) {
                    EventType.OPEN -> openFolderWithBundle(passwordEvent.position)
                    EventType.EDIT -> editFolder(passwordEvent.position)
                    EventType.DELETE -> deleteFolder(passwordEvent.position)
                    EventType.MOVE -> openMoveListDialog(
                            ItemType.FOLDER,
                            mFolderList[passwordEvent.position].folderName!!,
                            mFolderList[passwordEvent.position].parentFolderName!!,
                            passwordEvent.position)
                    EventType.LOCK -> setLock(ItemType.FOLDER, passwordEvent.position)
                    EventType.SHARE -> {

                    }
                }
        }
    }

    @Subscribe
    fun onFolderClickEvent(folderClickEvent: FolderClickEvent) {
        when (folderClickEvent.itemType) {
            ListFragment.ItemType.NOTES -> {
                val noteEntity = mNotesList[folderClickEvent.itemClickPosition]
                updateNote((activity?.application as MyApplication).fileUtil, noteEntity, noteEntity.pinned, noteEntity.locked, folderClickEvent.folderName)
            }
            ListFragment.ItemType.FOLDER -> mFolderViewModel.changeFolderParent(mFolderList[folderClickEvent.itemClickPosition].uid, folderClickEvent.folderName)
        }
    }

    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

    enum class ItemType {
        NOTES, FOLDER
    }

    enum class EventType {
        OPEN, EDIT, DELETE, LOCK, MOVE, SHARE
    }

    enum class SortType {
        DATE, PIN, LOCK
    }
}