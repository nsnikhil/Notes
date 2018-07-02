package com.nrs.nsnik.notes.view.fragments

import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxPopupMenu
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.util.RvItemTouchHelper
import com.nrs.nsnik.notes.view.adapters.NotesAdapter
import com.nrs.nsnik.notes.view.fragments.dialogFragments.CreateFolderDialog
import com.nrs.nsnik.notes.view.listeners.NoteItemClickListener
import com.nrs.nsnik.notes.viewmodel.FolderViewModel
import com.nrs.nsnik.notes.viewmodel.NoteViewModel
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.empty_state.*
import kotlinx.android.synthetic.main.fab_reveal.*
import kotlinx.android.synthetic.main.list_layout.*
import timber.log.Timber

class ListFragment : Fragment(), NoteItemClickListener {

    private lateinit var mNoteViewModel: NoteViewModel
    private lateinit var mFolderViewModel: FolderViewModel

    private var mNotesList: List<NoteEntity> = mutableListOf()
    private var mFolderList: List<FolderEntity> = mutableListOf()

    private var mFolderName: String = "noFolder"
    private lateinit var mNotesAdapter: NotesAdapter

    private val mInEditorMode: Boolean = false

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
            R.id.menuMainSearch -> {
            }
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
                RxView.clicks(fabAdd).subscribe({ if (isRevealed) disappear() else reveal() },
                        { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(fabAddNote).subscribe {
                    disappear()
                    openNoteEditor(mFolderName, null)
                },

                RxView.clicks(fabAddFolder).subscribe {
                    disappear()
                    val bundle = Bundle()
                    bundle.putString(activity?.resources?.getString(R.string.bundleCreateFolderParentFolder), mFolderName)
                    val dialog = CreateFolderDialog()
                    dialog.arguments = bundle
                    dialog.show(fragmentManager, "createFolderDialog")
                }
        )
    }

    private fun setViewModel(folderName: String) {
        mFolderViewModel.getFolderByParentNoPinNoLock(folderName).observe(this, androidx.lifecycle.Observer { swapFolder(it) })
        mNoteViewModel.getNoteByFolderNameNoPinNoLock(folderName).observe(this, androidx.lifecycle.Observer { swapNotes(it) })
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
        if (position != RecyclerView.NO_POSITION) {
            val startPos = 1
            val currPos = position - startPos
            val bundle = Bundle()
            bundle.putString(activity?.resources?.getString(R.string.bundleListFragmentFolderName), mFolderList[currPos].folderName!!)
            activity?.findNavController(R.id.mainNavHost)?.navigate(R.id.navItemNotes, bundle)
        }
    }

    @Throws(Exception::class)
    private fun openNote(position: Int) {
        val startPos = mFolderList.size + 2
        val currPos = position - startPos
        openNoteEditor(null, mNotesList[currPos])
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
            0 -> if (!mInEditorMode) {
                inflatePopUpMenu(view)
            }
            1 -> if (!mInEditorMode) {
                inflatePopUpMenu(view)
            }
        }
    }

    private fun inflatePopUpMenu(view: View) {
        val popupMenu = PopupMenu(activity, view, Gravity.END)
        popupMenu.inflate(R.menu.pop_up_menu)
        RxPopupMenu.itemClicks(popupMenu).subscribe {
            when (it.itemId) {
                R.id.popUpStar -> {
                }
                R.id.popUpLock -> {
                }
                R.id.popUpEdit -> {
                }
                R.id.popUpMove -> {
                }
                R.id.popUpShare -> {
                }
                R.id.popUpDelete -> {
                }
            }
        }
        popupMenu.show()
    }

    /**
     * if the add notes and folder fab is not visible
     * the perform animation and then make them visible
     */
    private fun reveal() {
        val rotateAnimation = RotateAnimation(0f, 135f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.duration = 50
        rotateAnimation.fillAfter = true
        rotateAnimation.isFillEnabled = true
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            val scaleUp = AnimationUtils.loadAnimation(activity, R.anim.jump_from_down)

            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                scaleUp.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        isRevealed = true
                        fabAddNoteContainer.visibility = View.VISIBLE
                        fabAddFolderContainer.visibility = View.VISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                fabAddFolderContainer.startAnimation(scaleUp)
                fabAddNoteContainer.startAnimation(scaleUp)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        fabAdd.startAnimation(rotateAnimation)

    }

    /**
     * if the add notes and folder fab is visible
     * the perform animation and then make them in-visible
     */
    private fun disappear() {
        val rotateAnimation = RotateAnimation(135f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.duration = 50
        rotateAnimation.fillAfter = true
        rotateAnimation.isFillEnabled = true
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            val scaleDown = AnimationUtils.loadAnimation(activity, R.anim.jump_to_down)

            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                scaleDown.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        isRevealed = false
                        fabAddNoteContainer.visibility = View.INVISIBLE
                        fabAddFolderContainer.visibility = View.INVISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                fabAddFolderContainer.startAnimation(scaleDown)
                fabAddNoteContainer.startAnimation(scaleDown)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        fabAdd.startAnimation(rotateAnimation)

    }

    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }
}
