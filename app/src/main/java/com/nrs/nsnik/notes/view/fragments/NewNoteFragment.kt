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

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Debug
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.model.CheckListObject
import com.nrs.nsnik.notes.util.*
import com.nrs.nsnik.notes.util.events.ColorPickerEvent
import com.nrs.nsnik.notes.util.events.FullScreenEvent
import com.nrs.nsnik.notes.view.adapters.AudioListAdapter
import com.nrs.nsnik.notes.view.adapters.CheckListAdapter
import com.nrs.nsnik.notes.view.adapters.ImageAdapter
import com.nrs.nsnik.notes.view.customViews.CirclePagerIndicatorDecoration
import com.nrs.nsnik.notes.view.fragments.dialogFragments.ActionAlertDialog
import com.nrs.nsnik.notes.view.fragments.dialogFragments.ColorPickerDialogFragment
import com.nrs.nsnik.notes.view.listeners.AdapterType
import com.nrs.nsnik.notes.view.listeners.AudioRecordListener
import com.nrs.nsnik.notes.view.listeners.OnAddClickListener
import com.nrs.nsnik.notes.view.listeners.OnItemRemoveListener
import com.nrs.nsnik.notes.viewmodel.NoteViewModel
import com.twitter.serial.serializer.CollectionSerializers
import com.twitter.serial.serializer.CoreSerializers
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial
import hugo.weaving.DebugLog
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_new_note.*
import kotlinx.android.synthetic.main.new_note_tools.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NewNoteFragment : Fragment(), OnAddClickListener, OnItemRemoveListener, AudioRecordListener {

    companion object {
        private const val ATTACH_PICTURE_REQUEST_CODE = 205
        private const val TAKE_PICTURE_REQUEST_CODE = 206
        private const val RECORD_AUDIO_PERMISSION_CODE = 512
        private const val EXTERNAL_STORAGE_PERMISSION_CODE = 513
        private const val READ_EXTERNAL_STORAGE_PERMISSION = 514
    }

    //Variables used in saving or updating note
    private var mFolderName: String = "noFolder"

    private var mIsLocked: Int = 0
    private var mIsStarred: Int = 0
    private var mHasReminder: Int = 0

    private var mColorCode: String = "#F8F8FF"

    private var mStarMenu: MenuItem? = null
    private var mLockMenu: MenuItem? = null

    private var mCurrentPhotoPath: String? = null

    private var mImagesLocations: MutableList<String> = mutableListOf()
    private var mAudioLocations: MutableList<String> = mutableListOf()
    private var mCheckList: MutableList<CheckListObject> = mutableListOf()

    private lateinit var mImageAdapter: ImageAdapter
    private lateinit var mAudioListAdapter: AudioListAdapter
    private lateinit var mCheckListAdapter: CheckListAdapter

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var mNoteViewModel: NoteViewModel
    private lateinit var mFileUtil: FileUtil
    private lateinit var mRootFolder: File

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>

    private var mNoteEntity: NoteEntity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_new_note, container, false)
        mFileUtil = (activity?.application as MyApplication).fileUtil
        mRootFolder = mFileUtil.rootFolder
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Debug.startMethodTracing()
        initialize()
        listeners()
        Debug.stopMethodTracing()
    }

    private fun initialize() {
        mNoteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)

        mBottomSheetBehavior = BottomSheetBehavior.from(toolsBottomSheet)

        toolsBottomSheet.apply {
            mBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, newState: Float) {
                    newNoteBackground.alpha = (newState / 2)
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {
                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        }
                    }
                }
            })
        }

        mImageAdapter = ImageAdapter(false, this)
        newNoteImageList.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = mImageAdapter
        }
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(newNoteImageList)
        newNoteImageList.addItemDecoration(CirclePagerIndicatorDecoration())

        mAudioListAdapter = AudioListAdapter(this)
        newNoteAudioList.apply {
            layoutManager = LinearLayoutManager(activity)
            itemAnimator = DefaultItemAnimator()
            adapter = mAudioListAdapter
        }


        mCheckListAdapter = CheckListAdapter(this, this)
        newNoteCheckList.apply {
            layoutManager = LinearLayoutManager(activity)
            itemAnimator = DefaultItemAnimator()
            adapter = mCheckListAdapter
        }

        if (arguments != null) setNote()

    }

    private fun listeners() {
        compositeDisposable.addAll(

                RxView.clicks(toolsDate).subscribe { changeState() },

                RxView.clicks(toolsCheckList).subscribe({
                    changeState()
                    addCheckListItem()
                }, { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(toolsCamera).subscribe({
                    changeState()
                    checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, EXTERNAL_STORAGE_PERMISSION_CODE, ::startCameraIntent)
                }, { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(toolsAttachment).subscribe({
                    changeState()
                    checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_PERMISSION, ::startGalleryIntent)
                }, { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(toolsAudio).subscribe({
                    changeState()
                    checkPermission(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_PERMISSION_CODE, ::recordAudio)
                }, { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(toolsReminder).subscribe({
                    changeState()
                    ReminderUtil.setReminder(activity!!, newNoteTitle.text.toString(), newNoteContent.text.toString())
                }, { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(toolsColor).subscribe({
                    changeState()
                    ColorPickerDialogFragment().show(fragmentManager, "color")
                }, { throwable -> Timber.d(throwable.message) })

        )
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.new_note_menu, menu)
        if (menu?.getItem(1) != null) mStarMenu = menu.getItem(1)
        if (menu?.getItem(2) != null) mLockMenu = menu.getItem(2)
        if (mNoteEntity == null) menu?.getItem(3)?.isVisible = false
        setMenuIconState()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.newNoteMenuSave -> if (verifyAndSave()) if (mNoteEntity == null) noteAction(NoteEntity(), ActionType.SAVE) else noteAction(mNoteEntity!!, ActionType.UPDATE)
            R.id.newNoteMenuDelete -> createDeleteDialog()
            R.id.newNoteMenuStar -> mIsStarred = setMenuState(mIsStarred, item, R.drawable.ic_star_48px, R.drawable.ic_star_border_48px, "Starred")
            R.id.newNoteMenuLock -> setLock(item)
            android.R.id.home -> activity?.findNavController(R.id.mainNavHost)?.navigateUp()
        }
        return true
    }

    private fun setLock(item: MenuItem) {
        if (PasswordUtil.checkLock((activity?.applicationContext as MyApplication).sharedPreferences, activity!!, fragmentManager!!, "password"))
            mIsLocked = setMenuState(mIsLocked, item, R.drawable.ic_lock_48px, R.drawable.ic_lock_open_48px, "Locked")
    }

    private fun setMenuState(state: Int, item: MenuItem, drawable: Int = 0, drawableAlt: Int = 0, message: String): Int {
        return if (state == 0) {
            item.setIcon(drawable)
            Toast.makeText(activity!!, message, Toast.LENGTH_LONG).show()
            1
        } else {
            item.setIcon(drawableAlt)
            Toast.makeText(activity!!, "Un $message", Toast.LENGTH_LONG).show()
            0
        }
    }

    @DebugLog
    private fun setNote() {

        mFolderName = arguments?.getString(activity?.resources?.getString(R.string.bundleListFragmentFolderName), "noFolder")!!

        newNoteContent.setText(arguments?.getString(activity?.resources?.getString(R.string.bundleReceiveIntentText), ""))

        val imageUri: Uri? = arguments?.getParcelable(activity?.resources?.getString(R.string.bundleReceiveIntentImage))
        val imageUris: List<Uri?>? = arguments?.getParcelableArrayList(activity?.resources?.getString(R.string.bundleReceiveIntentImageList))

        if (imageUri != null) saveImage(MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri))
        else imageUris?.forEach {
            saveImage(MediaStore.Images.Media.getBitmap(activity?.contentResolver, it))
        }

        mNoteEntity = ByteBufferSerial().fromByteArray(arguments?.getByteArray(activity?.resources?.getString(R.string.bundleNoteEntity)), NoteEntity.SERIALIZER)

        if (mNoteEntity == null) return

        val id: Int = mNoteEntity?.uid!!

        mFileUtil.getLiveNote(mNoteEntity?.fileName!!).observe(this, androidx.lifecycle.Observer {

            mNoteEntity = it

            mNoteEntity?.uid = id

            newNoteTitle.setText(mNoteEntity?.title)
            newNoteContent.setText(mNoteEntity?.noteContent)

            mFolderName = mNoteEntity?.folderName!!

            mColorCode = mNoteEntity?.color!!

            newNoteTitle.setTextColor(Color.parseColor(mColorCode))

            mIsStarred = mNoteEntity?.pinned!!

            mIsLocked = mNoteEntity?.locked!!

            toolsDate.text = AppUtil.getTimeDifference(Calendar.getInstance().timeInMillis - mNoteEntity?.dateModified?.time!!)

            if (mNoteEntity?.imageList != null && mNoteEntity?.imageList?.isNotEmpty()!!) {
                newNoteImageList.visibility = View.VISIBLE
                mImagesLocations.addAll(mNoteEntity?.imageList!!)
                mImageAdapter.submitList(mImagesLocations)
            }

            if (mNoteEntity?.audioList != null && mNoteEntity?.audioList?.isNotEmpty()!!) {
                newNoteAudioList!!.visibility = View.VISIBLE
                mAudioLocations.addAll(mNoteEntity?.audioList!!)
                mAudioListAdapter.submitList(mAudioLocations)
            }

            if (mNoteEntity?.checkList != null && mNoteEntity?.checkList?.isNotEmpty()!!) {
                newNoteCheckList!!.visibility = View.VISIBLE
                mCheckList.addAll(mNoteEntity?.checkList!!)
                mCheckListAdapter.submitList(mCheckList)
            }

            if (mNoteEntity?.hasReminder != 0) {
                mHasReminder = 1
            }

            setMenuIconState()
        })
    }

    private fun setMenuIconState() {
        mStarMenu?.setIcon(if (mIsStarred == 1) R.drawable.ic_star_48px else R.drawable.ic_star_border_48px)
        mLockMenu?.setIcon(if (mIsLocked == 1) R.drawable.ic_lock_48px else R.drawable.ic_lock_open_48px)
    }

    private fun addCheckListItem() {
        val list = CheckListObject()
        list.text = ""
        list.done = false
        mCheckList.add(list)
        mCheckListAdapter.submitList(mCheckList)
        mCheckListAdapter.notifyItemInserted(mCheckList.size - 1)
        changeVisibility(mCheckList, newNoteCheckList)
    }

    private fun addAudioToList(audioFileLocation: String) {
        mAudioLocations.add(audioFileLocation)
        mAudioListAdapter.submitList(mAudioLocations)
        mAudioListAdapter.notifyItemInserted(mAudioLocations.size - 1)
        changeVisibility(mAudioLocations, newNoteAudioList)
    }

    private fun addImageToList(imageLocation: String) {
        mImagesLocations.add(imageLocation)
        mImageAdapter.submitList(mImagesLocations)
        mImageAdapter.notifyItemInserted(mImagesLocations.size - 1)
        changeVisibility(mImagesLocations, newNoteImageList)
    }

    private fun changeVisibility(list: List<Any>, recyclerView: RecyclerView) {
        recyclerView.visibility = if (list.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun checkPermission(permission: String, requestCode: Int, action: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(permission), requestCode)
            return
        }
        action()
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun startCameraIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity?.packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(activity!!, "com.nrs.nsnik.notes.fileprovider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST_CODE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    private fun startGalleryIntent() {
        val chosePicture = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        chosePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (chosePicture.resolveActivity(activity?.packageManager) != null)
            startActivityForResult(chosePicture, ATTACH_PICTURE_REQUEST_CODE)
        else
            Toast.makeText(activity!!, activity?.resources?.getString(R.string.noGallery), Toast.LENGTH_LONG).show()
    }

    private fun saveImage(image: Bitmap) {
        val imageFileName = AppUtil.makeName(FILE_TYPES.IMAGE)
        mFileUtil.saveImage(image, imageFileName)
        addImageToList(imageFileName)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun recordAudio() {
        AudioUtil.recordAudio(activity!!, mRootFolder, this)
    }

    override fun audioRecorded(fileName: String) {
        addAudioToList(fileName)
    }

    private fun noteAction(noteEntity: NoteEntity, action: ActionType) {
        noteEntity.title = newNoteTitle.text.toString()
        noteEntity.noteContent = newNoteContent.text.toString()
        noteEntity.folderName = mFolderName
        noteEntity.fileName = AppUtil.makeName(FILE_TYPES.TEXT)
        noteEntity.color = mColorCode
        noteEntity.dateModified = Calendar.getInstance().time
        noteEntity.imageList = mImagesLocations
        noteEntity.audioList = mAudioLocations
        noteEntity.checkList = mCheckList
        noteEntity.pinned = mIsStarred
        noteEntity.locked = mIsLocked
        noteEntity.hasReminder = mHasReminder
        if (action == ActionType.SAVE) mNoteViewModel.insertNote(noteEntity) else mNoteViewModel.updateNote(noteEntity)
        activity?.findNavController(R.id.mainNavHost)?.navigateUp()
    }

    private fun verifyAndSave(): Boolean {
        if (newNoteTitle.text.toString().isEmpty() && newNoteContent.text.toString().isEmpty())
            Toast.makeText(activity!!, resources.getString(R.string.noNote), Toast.LENGTH_LONG).show()
        return true
    }

    private fun changeState() {
        mBottomSheetBehavior.state = if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ATTACH_PICTURE_REQUEST_CODE -> if (resultCode == RESULT_OK && resultCode != RESULT_CANCELED) {
                if (data != null) saveImage(MediaStore.Images.Media.getBitmap(activity?.contentResolver, data.data))
            }
            TAKE_PICTURE_REQUEST_CODE -> if (resultCode == RESULT_OK && resultCode != RESULT_CANCELED) saveImage(BitmapFactory.decodeFile(mCurrentPhotoPath))
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

    override fun addClickListener() {
        addCheckListItem()
    }

    override fun onItemRemoved(position: Int, adapterType: AdapterType) {
        when (adapterType) {
            AdapterType.IMAGE_ADAPTER -> {
                mImagesLocations.removeAt(position)
                mImageAdapter.submitList(mImagesLocations)
                mImageAdapter.notifyItemRemoved(position)
            }
            AdapterType.AUDIO_ADAPTER -> {
                mAudioLocations.removeAt(position)
                mAudioListAdapter.submitList(mAudioLocations)
                mAudioListAdapter.notifyItemRemoved(position)
            }
            AdapterType.CHECKLIST_ADAPTER -> {
                mCheckList.removeAt(position)
                mCheckListAdapter.submitList(mCheckList)
                mCheckListAdapter.notifyItemRemoved(position)
            }
        }
    }

//    private fun showUndoBar(position: Int) {
//        val resources = activity?.resources
//        Snackbar.make(activity?.findViewById(R.id.mainDrawerLayout)!!, resources?.getString(R.string.itemRemoved)!!, Snackbar.LENGTH_LONG)
//                .setAction(resources.getString(R.string.undo)) { Timber.d(position.toString()) }
//                .setActionTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
//                .show()
//    }

    private fun createDeleteDialog() {
        val resources = activity?.resources
        ActionAlertDialog.showDialog(
                activity!!,
                resources?.getString(R.string.warning)!!,
                resources.getString(R.string.deleteSingleNoteWarning),
                resources.getString(R.string.yes),
                resources.getString(R.string.no),
                DialogInterface.OnClickListener { dialogInterface, i ->
                    activity?.findNavController(R.id.mainNavHost)?.navigateUp()
                    mNoteViewModel.deleteNote(mNoteEntity!!)
                    mFileUtil.deleteNoteResources(mNoteEntity!!)
                },
                DialogInterface.OnClickListener { dialogInterface, i ->

                }
        )
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
    fun onColorPickerEvent(colorPickerEvent: ColorPickerEvent) {
        mColorCode = colorPickerEvent.color
        newNoteTitle.setTextColor(Color.parseColor(mColorCode))
    }

    @Subscribe
    fun onFullScreenEvent(fullScreenEvent: FullScreenEvent) {
        val resources = activity?.resources
        val bundle = Bundle()
        val byteArray: ByteArray = ByteBufferSerial().toByteArray(mImagesLocations, CollectionSerializers.getListSerializer(CoreSerializers.STRING))
        bundle.putByteArray(resources?.getString(R.string.bundleImageList), byteArray)
        bundle.putInt(resources?.getString(R.string.bundleImageListPosition), fullScreenEvent.position)
        newNoteImageList.findNavController().navigate(R.id.noteToImageList, bundle)
    }

    enum class FILE_TYPES {
        TEXT, IMAGE, AUDIO
    }

    private enum class ActionType {
        SAVE, UPDATE
    }
}