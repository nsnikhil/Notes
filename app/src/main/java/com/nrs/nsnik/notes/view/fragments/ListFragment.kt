package com.nrs.nsnik.notes.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.util.FileUtil
import com.nrs.nsnik.notes.util.RvItemTouchHelper
import com.nrs.nsnik.notes.util.events.FolderClickEvent
import com.nrs.nsnik.notes.view.adapters.NotesAdapter
import com.nrs.nsnik.notes.view.fragments.dialogFragments.CreateFolderDialog
import com.nrs.nsnik.notes.view.listeners.NoteItemClickListener
import com.nrs.nsnik.notes.viewmodel.FolderViewModel
import com.nrs.nsnik.notes.viewmodel.NoteViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.empty_state.*
import kotlinx.android.synthetic.main.fab_reveal.*
import kotlinx.android.synthetic.main.list_layout.*
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.*

class ListFragment : Fragment(), NoteItemClickListener {

    private var mNoteViewModel: NoteViewModel? = null
    private var mFolderViewModel: FolderViewModel? = null

    private var mNotesList: List<NoteEntity>? = null
    private var mFolderList: List<FolderEntity>? = null

    private var mFolderName: String? = null
    private var mNotesAdapter: NotesAdapter? = null
    private var mFileUtil: FileUtil? = null

    private val mInEditorMode: Boolean = false
    private var mSelectedNoteId: List<Int>? = null
    private var mSelectedFolderId: List<Int>? = null

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var isRevealed: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        listeners()
    }

    private fun initialize() {

        if (activity != null && arguments != null) {
            mFolderName = arguments!!.getString(activity?.resources?.getString(R.string.bundleListFragmentFolderName))
        }

        if (activity != null) {
            mFileUtil = (activity?.application as MyApplication).fileUtil
        }

        mSelectedNoteId = ArrayList()
        mSelectedFolderId = ArrayList()

        mNoteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        mFolderViewModel = ViewModelProviders.of(this).get(FolderViewModel::class.java)

        mNotesList = ArrayList()
        mFolderList = ArrayList()


        //Setting up recycler view

        mNotesAdapter = NotesAdapter(activity!!, mNotesList!!, mFolderList!!, this)

        commonList.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            hasFixedSize()
            itemAnimator = DefaultItemAnimator()
            adapter = mNotesAdapter
        }


        val touchHelper = ItemTouchHelper(RvItemTouchHelper(mNotesAdapter!!))
        touchHelper.attachToRecyclerView(commonList)

        setViewModel()

    }

    private fun listeners() {

        compositeDisposable.addAll(
                RxView.clicks(fabAdd).subscribe({ if (isRevealed) disappear() else reveal() },
                        { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(fabAddNote).subscribe {
                    fabAddNote.findNavController().navigate(R.id.listToAddNewNote)
                    disappear()
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

    private fun setViewModel() {
//        mNoteViewModel!!.getNoteByFolderNameNoPinNoLock(mFolderName).observe(this, ???({ this.swapNotes(it) }))
//
//        mFolderViewModel!!.getFolderByParentNoPinNoLock(mFolderName).observe(this, ???({ this.swapFolder(it) }))
    }

    private fun swapFolder(folderEntityList: List<FolderEntity>?) {
        if (folderEntityList == null) return
        mFolderList = folderEntityList
        mNotesAdapter!!.updateFolderList(folderEntityList)
        setEmpty()
    }

    private fun swapNotes(noteEntityList: List<NoteEntity>?) {
        if (noteEntityList == null) return
        mNotesList = noteEntityList
        mNotesAdapter!!.updateNotesList(mNotesList!!)
        setEmpty()
    }

    private fun setEmpty() {
        if (mNotesList!!.isEmpty() && mFolderList!!.isEmpty()) {
            emptyState.visibility = View.VISIBLE
        } else {
            emptyState.visibility = View.GONE
        }
    }

    private fun openFolder(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val startPos = 1
            val currPos = position - startPos
            EventBus.getDefault().post(FolderClickEvent(mFolderList!![currPos].folderName!!))
        }
    }

    @Throws(Exception::class)
    private fun openNote(position: Int) {
        if (activity != null) {

            val startPos = mFolderList!!.size + 2
            val currPos = position - startPos
            val noteEntity = mFileUtil!!.getNote(mNotesList!![currPos].fileName!!)

        }
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
        if (isFolder) {
            mFolderViewModel!!.deleteFolder(folderEntity)
        } else {
            mNoteViewModel!!.deleteNote(noteEntity)
        }
    }

    override fun onClick(position: Int, itemViewType: Int) {
        when (itemViewType) {
            0 -> try {
                openNote(position)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            1 -> openFolder(position)
        }
    }

    override fun onLongClick(position: Int, itemViewType: Int) {
        when (itemViewType) {
            0 -> if (!mInEditorMode) {

            }
            1 -> if (!mInEditorMode) {

            }
        }
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
