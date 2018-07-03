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

package com.nrs.nsnik.notes.view.fragments

import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxPopupMenu
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.util.RvItemTouchHelper
import com.nrs.nsnik.notes.util.events.PasswordEvent
import com.nrs.nsnik.notes.view.adapters.NotesAdapter
import com.nrs.nsnik.notes.view.fragments.dialogFragments.CreateFolderDialog
import com.nrs.nsnik.notes.view.fragments.dialogFragments.PasswordDialogFragment
import com.nrs.nsnik.notes.view.listeners.NoteItemClickListener
import com.nrs.nsnik.notes.viewmodel.FolderViewModel
import com.nrs.nsnik.notes.viewmodel.NoteViewModel
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.empty_state.*
import kotlinx.android.synthetic.main.fab_reveal.*
import kotlinx.android.synthetic.main.list_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class ListFragment : Fragment(), NoteItemClickListener {

    private lateinit var mNoteViewModel: NoteViewModel
    private lateinit var mFolderViewModel: FolderViewModel

    private var mNotesList: List<NoteEntity> = mutableListOf()
    private var mFolderList: List<FolderEntity> = mutableListOf()

    private var mFolderName: String = "noFolder"
    private lateinit var mNotesAdapter: NotesAdapter

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var isRevealed: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        listeners()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuMainSearch -> activity?.findNavController(R.id.mainNavHost)?.navigate(R.id.listToSearch)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initialize() {

        mFolderName = arguments?.getString(activity?.resources?.getString(R.string.bundleListFragmentFolderName), "noFolder")!!

        mNoteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        mFolderViewModel = ViewModelProviders.of(this).get(FolderViewModel::class.java)

        //Setting up recycler view
        mNotesAdapter = NotesAdapter(activity!!, mNotesList, mFolderList, this)

        commonList.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            hasFixedSize()
            itemAnimator = DefaultItemAnimator()
            adapter = mNotesAdapter
        }

        val touchHelper = ItemTouchHelper(RvItemTouchHelper(mNotesAdapter))
        touchHelper.attachToRecyclerView(commonList)

        setViewModel(mFolderName)
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
                    val dialog = CreateFolderDialog()
                    dialog.arguments = bundle
                    dialog.show(fragmentManager, "createFolderDialog")
                }
        )
    }

    private fun setViewModel(folderName: String) {
        mFolderViewModel.getFolderByParentOrdered(folderName).observe(this, androidx.lifecycle.Observer { swapFolder(it) })
        mNoteViewModel.getNoteByFolderNameOrdered(folderName).observe(this, androidx.lifecycle.Observer { swapNotes(it) })
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
        if (mFolderList[currPos].locked == 1) showPassWordDialog(ItemType.FOLDER, currPos)
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
        if (mNotesList[currPos].locked == 1) showPassWordDialog(ItemType.NOTES, currPos)
        else openNoteEditor(null, mNotesList[currPos])
    }

    private fun showPassWordDialog(itemType: ItemType, position: Int) {
        val bundle = Bundle()
        bundle.putInt(activity?.resources?.getString(R.string.bundleItemType), itemType.ordinal)
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

    private fun makeDeleteDialog(message: String, folderEntity: FolderEntity, noteEntity: NoteEntity, isFolder: Boolean) {
        if (activity != null) {
            val delete = AlertDialog.Builder(activity!!)
            delete.setTitle(activity?.resources?.getString(R.string.warning))
                    .setMessage(message)
                    .setNegativeButton(activity?.resources?.getString(R.string.no)) { dialogInterface, i -> }
                    .setPositiveButton(activity?.resources?.getString(R.string.yes)) { dialogInterface, i ->
                        delete(isFolder, folderEntity, noteEntity)
                    }
            delete.create().show()
        }
    }


    private fun delete(isFolder: Boolean, folderEntity: FolderEntity, noteEntity: NoteEntity) {
        if (isFolder) mFolderViewModel.deleteFolder(folderEntity) else mNoteViewModel.deleteNote(noteEntity)
    }

    override fun onClick(position: Int, itemViewType: Int) {
        when (itemViewType) {
            0 -> openNote(position)
            1 -> openFolder(position)
        }
    }

    override fun onLongClick(position: Int, itemViewType: Int, view: View) {
        when (itemViewType) {
            0 -> inflatePopUpMenu(position, view)
            1 -> inflatePopUpMenu(position, view)
        }
    }

    private fun inflatePopUpMenu(position: Int, view: View) {
        val popupMenu = PopupMenu(activity, view, Gravity.END)
        popupMenu.inflate(R.menu.pop_up_menu)
        RxPopupMenu.itemClicks(popupMenu).subscribe {
            when (it.itemId) {
                R.id.popUpStar -> {
                    Toast.makeText(activity!!, "TODO", Toast.LENGTH_SHORT).show()
                }
                R.id.popUpLock -> {
                    Toast.makeText(activity!!, "TODO", Toast.LENGTH_SHORT).show()
                }
                R.id.popUpEdit -> {
                    Toast.makeText(activity!!, "TODO", Toast.LENGTH_SHORT).show()
                }
                R.id.popUpMove -> {
                    Toast.makeText(activity!!, "TODO", Toast.LENGTH_SHORT).show()
                }
                R.id.popUpShare -> {
                    Toast.makeText(activity!!, "TODO", Toast.LENGTH_SHORT).show()
                }
                R.id.popUpDelete -> {
                    Toast.makeText(activity!!, "TODO", Toast.LENGTH_SHORT).show()
                }
            }
        }
        popupMenu.show()
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
                transitionAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        isRevealed = changeFabVisibility()
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                fabAddFolderContainer.startAnimation(transitionAnimation)
                fabAddNoteContainer.startAnimation(transitionAnimation)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        fabAdd.startAnimation(rotateAnimation)
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
            ItemType.NOTES -> openNoteEditor(null, mNotesList[passwordEvent.position])
            ItemType.FOLDER -> openFolderWithBundle(passwordEvent.position)
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
}