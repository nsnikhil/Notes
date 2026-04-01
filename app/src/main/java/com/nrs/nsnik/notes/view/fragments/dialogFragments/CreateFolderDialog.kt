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
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding4.view.clicks
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.databinding.NewFolderDialogBinding
import com.nrs.nsnik.notes.util.AppUtil
import com.nrs.nsnik.notes.util.PasswordUtil
import com.nrs.nsnik.notes.util.events.ColorPickerEvent
import com.nrs.nsnik.notes.viewmodel.FolderViewModel
import kotlinx.serialization.json.Json
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class CreateFolderDialog : DialogFragment() {

    private var _binding: NewFolderDialogBinding? = null
    private val binding get() = _binding!!

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var mColor = "#F8F8FF"
    private var isLocked = 0
    private var isStarred = 0
    private lateinit var mFolderViewModel: FolderViewModel
    private var mParentFolderName: String? = null
    private var folderEntity: FolderEntity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = NewFolderDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        listeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        mFolderViewModel = ViewModelProvider(this)[FolderViewModel::class.java]
        if (activity != null && arguments != null) {
            mParentFolderName = arguments?.getString(activity?.resources?.getString(R.string.bundleCreateFolderParentFolder))

            val folderBytes = arguments?.getByteArray(activity?.resources?.getString(R.string.bundleFolderEntity))
            folderEntity = if (folderBytes != null) Json.decodeFromString<FolderEntity>(String(folderBytes, Charsets.UTF_8)) else null

            if (folderEntity != null) {

                binding.dialogFolderName.setText(folderEntity?.folderName)

                mColor = folderEntity?.color!!
                binding.dialogFolderColor.backgroundTintList = AppUtil.stateList(mColor)

                isLocked = folderEntity?.locked!!
                binding.dialogFolderLock.setImageDrawable(if (isLocked == 1) getDrawable(R.drawable.ic_lock_48px) else getDrawable(R.drawable.ic_lock_open_48px))

                isStarred = folderEntity?.pinned!!
                binding.dialogFolderStar.setImageDrawable(if (isStarred == 1) getDrawable(R.drawable.ic_star_48px) else getDrawable(R.drawable.ic_star_border_48px))

                mParentFolderName = folderEntity?.parentFolderName!!

                binding.dialogFolderCreate.text = activity?.resources?.getString(R.string.update)
            }
        }

        setFocus(binding.dialogFolderName)
    }


    private fun setFocus(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }


    private fun listeners() {
        compositeDisposable.addAll(
                binding.dialogFolderColor.clicks().subscribe {
                    val dialog = ColorPickerDialogFragment()
                    dialog.show(parentFragmentManager, "color")
                },
                binding.dialogFolderCreate.clicks().subscribe { if (folderEntity == null) updateFolder(FolderEntity()) else updateFolder(folderEntity!!) },
                binding.dialogFolderCancel.clicks().subscribe { dismiss() },
                binding.dialogFolderLock.clicks().subscribe { setLock() },
                binding.dialogFolderStar.clicks().subscribe {
                    isStarred = changeValue(binding.dialogFolderStar, isStarred, getDrawable(R.drawable.ic_star_48px),
                            getDrawable(R.drawable.ic_star_border_48px))
                }
        )
    }

    private fun setLock() {
        if (PasswordUtil.checkLock((activity?.applicationContext as MyApplication).sharedPreferences, activity!!, fragmentManager!!, "password"))
            isLocked = changeValue(binding.dialogFolderLock, isLocked, getDrawable(R.drawable.ic_lock_48px), getDrawable(R.drawable.ic_lock_open_48px))
    }

    private fun getDrawable(drawableId: Int): Drawable {
        return ContextCompat.getDrawable(activity!!, drawableId)!!
    }

    private fun changeValue(imageView: ImageView, value: Int, drawable: Drawable, drawableAlt: Drawable): Int {
        imageView.setImageDrawable(if (value == 0) drawable else drawableAlt)
        return if (value == 0) 1 else 0
    }

    private fun updateFolder(folderEntity: FolderEntity) {
        if (binding.dialogFolderName.text.toString().isNotEmpty()) {
            folderEntity.folderName = binding.dialogFolderName.text.toString()
            folderEntity.color = mColor
            folderEntity.locked = isLocked
            folderEntity.pinned = isStarred
            folderEntity.parentFolderName = mParentFolderName
            if (this.folderEntity == null) mFolderViewModel.insertFolder(folderEntity) else mFolderViewModel.updateFolder(folderEntity)
            dismiss()
        } else {
            binding.dialogFolderName.error = activity?.resources?.getString(R.string.errorNoFolderName)
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
        binding.dialogFolderColor.backgroundTintList = AppUtil.stateList(mColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

}
