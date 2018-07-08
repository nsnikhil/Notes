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

package com.nrs.nsnik.notes.view.fragments.dialogFragments

import android.content.Context
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
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.util.AppUtil
import com.nrs.nsnik.notes.util.PasswordUtil
import com.nrs.nsnik.notes.util.events.ColorPickerEvent
import com.nrs.nsnik.notes.viewmodel.FolderViewModel
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial
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
    private var folderEntity: FolderEntity? = null

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

            folderEntity = ByteBufferSerial().fromByteArray(arguments?.getByteArray(activity?.resources?.getString(R.string.bundleFolderEntity)), FolderEntity.SERIALIZER)

            if (folderEntity != null) {

                dialogFolderName.setText(folderEntity?.folderName)

                mColor = folderEntity?.color!!
                dialogFolderColor!!.backgroundTintList = AppUtil.stateList(mColor)

                isLocked = folderEntity?.locked!!
                dialogFolderLock.setImageDrawable(if (isLocked == 1) getDrawable(R.drawable.ic_lock_black_48px) else getDrawable(R.drawable.ic_lock_open_black_48px))

                isStarred = folderEntity?.pinned!!
                dialogFolderStar.setImageDrawable(if (isStarred == 1) getDrawable(R.drawable.ic_star_black_48px) else getDrawable(R.drawable.ic_star_border_black_48px))

                mParentFolderName = folderEntity?.parentFolderName!!

                dialogFolderCreate.text = activity?.resources?.getString(R.string.update)
            }
        }
    }

    private fun listeners() {
        compositeDisposable.addAll(
                RxView.clicks(dialogFolderColor).subscribe {
                    val dialog = ColorPickerDialogFragment()
                    dialog.show(fragmentManager, "color")
                },
                RxView.clicks(dialogFolderCreate).subscribe { if (folderEntity == null) updateFolder(FolderEntity()) else updateFolder(folderEntity!!) },
                RxView.clicks(dialogFolderCancel).subscribe { dismiss() },
                RxView.clicks(dialogFolderLock).subscribe { setLock() },
                RxView.clicks(dialogFolderStar).subscribe {
                    isStarred = changeValue(dialogFolderStar, isStarred, getDrawable(R.drawable.ic_star_black_48px),
                            getDrawable(R.drawable.ic_star_border_black_48px))
                }
        )
    }

    private fun setLock() {
        if (PasswordUtil.checkLock((activity?.applicationContext as MyApplication).sharedPreferences, activity!!, fragmentManager!!, "password")) {
            isLocked = changeValue(dialogFolderLock, isLocked, getDrawable(R.drawable.ic_lock_black_48px), getDrawable(R.drawable.ic_lock_open_black_48px))
        }
    }

    private fun getDrawable(drawableId: Int): Drawable {
        return ContextCompat.getDrawable(activity!!, drawableId)!!
    }

    private fun changeValue(imageView: ImageView, value: Int, drawable: Drawable, drawableAlt: Drawable): Int {
        imageView.setImageDrawable(if (value == 0) drawable else drawableAlt)
        return if (value == 0) 1 else 0
    }

    private fun updateFolder(folderEntity: FolderEntity) {
        if (dialogFolderName.text.toString().isNotEmpty()) {
            folderEntity.folderName = dialogFolderName.text.toString()
            folderEntity.color = mColor
            folderEntity.locked = isLocked
            folderEntity.pinned = isStarred
            folderEntity.parentFolderName = mParentFolderName
            if (this.folderEntity == null) mFolderViewModel.insertFolder(folderEntity) else mFolderViewModel.updateFolder(folderEntity)
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
        dialogFolderColor!!.backgroundTintList = AppUtil.stateList(mColor)
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
