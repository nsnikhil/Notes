package com.nrs.nsnik.notes.view.fragments.dialogFragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.util.events.ColorPickerEvent
import com.nrs.nsnik.notes.viewmodel.FolderViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.new_folder_dialog.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class CreateFolderDialog : DialogFragment() {

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var mColor = "#212121"
    private var isLocked = 0
    private var isStarred = 0
    private lateinit var mFolderViewModel: FolderViewModel
    private var mParentFolderName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.new_folder_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        listeners()
    }

    private fun initialize() {
        mFolderViewModel = ViewModelProviders.of(this).get(FolderViewModel::class.java)
        if (activity != null && arguments != null) {
            mParentFolderName = arguments?.getString(activity?.resources?.getString(R.string.bundleCreateFolderParentFolder))
        }
    }

    private fun listeners() {
        compositeDisposable.addAll(
                RxView.clicks(dialogFolderColor).subscribe {
                    val dialog = ColorPickerDialogFragment()
                    dialog.show(fragmentManager, "color")
                },
                RxView.clicks(dialogFolderCreate).subscribe { createFolder() },
                RxView.clicks(dialogFolderCancel).subscribe { dismiss() },
                RxView.clicks(dialogFolderLock).subscribe {
                    isLocked = changeValue(dialogFolderLock, isLocked,
                            ContextCompat.getDrawable(activity!!, R.drawable.ic_lock_black_48px)!!,
                            ContextCompat.getDrawable(activity!!, R.drawable.ic_lock_open_black_48px)!!)
                },
                RxView.clicks(dialogFolderStar).subscribe {
                    isStarred = changeValue(dialogFolderStar, isStarred,
                            ContextCompat.getDrawable(activity!!, R.drawable.ic_star_black_48px)!!,
                            ContextCompat.getDrawable(activity!!, R.drawable.ic_star_border_black_48px)!!)
                }
        )
    }

    private fun changeValue(imageView: ImageView, value: Int, drawable: Drawable, drawableAlt: Drawable): Int {
        imageView.setImageDrawable(if (value == 0) drawable else drawableAlt)
        return if (value == 0) 1 else 0
    }

    private fun createFolder() {
        if (dialogFolderName.text.toString().isNotEmpty()) {
            val folderEntity = FolderEntity()
            folderEntity.folderName = dialogFolderName.text.toString()
            folderEntity.color = mColor
            folderEntity.locked = isLocked
            folderEntity.pinned = isStarred
            folderEntity.parentFolderName = mParentFolderName
            mFolderViewModel.insertFolder(folderEntity)
            dismiss()
        } else {
            dialogFolderName.error = activity?.resources?.getString(R.string.errorNoFolderName)
        }
    }

    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onColorPickerEvent(colorPickerEvent: ColorPickerEvent) {
        mColor = colorPickerEvent.color
        dialogFolderColor!!.backgroundTintList = stateList(mColor)
    }

    private fun stateList(colorString: String): ColorStateList {
        val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_pressed))
        val color = Color.parseColor(colorString)
        val colors = intArrayOf(color, color, color, color)
        return ColorStateList(states, colors)
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

//    private inner class InsertFolderWorker : Worker() {
//
//        override fun doWork(): Result {
//            return Result.SUCCESS
//        }
//    }
}
