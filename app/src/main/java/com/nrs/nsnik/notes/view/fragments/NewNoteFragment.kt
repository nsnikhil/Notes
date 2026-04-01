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
import android.content.DialogInterface
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding4.view.clicks
import com.nrs.nsnik.notes.BuildConfig
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.databinding.FragmentNewNoteBinding
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
import com.nrs.nsnik.notes.view.fragments.dialogFragments.PasswordDialogFragment
import com.nrs.nsnik.notes.view.listeners.AdapterType
import com.nrs.nsnik.notes.view.listeners.AudioRecordListener
import com.nrs.nsnik.notes.view.listeners.OnAddClickListener
import com.nrs.nsnik.notes.view.listeners.OnItemRemoveListener
import com.nrs.nsnik.notes.viewmodel.NoteViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NewNoteFragment : Fragment(), OnAddClickListener, OnItemRemoveListener, AudioRecordListener {

    companion object {
        private const val RECORD_AUDIO_PERMISSION_CODE = 512
        private const val EXTERNAL_STORAGE_PERMISSION_CODE = 513
    }

    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!

    // Activity Result API launchers — registered at class init time (before STARTED)
    private val cameraLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                mCurrentPhotoPath?.let { path -> saveImage(BitmapFactory.decodeFile(path)) }
            }
        }

    private val galleryLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                saveImage(MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri))
            }
        }

    // Views from included new_note_tools layout (no android:id on <include>)
    private lateinit var toolsBottomSheet: ConstraintLayout
    private lateinit var toolsDate: TextView
    private lateinit var toolsCheckList: TextView
    private lateinit var toolsCamera: TextView
    private lateinit var toolsAttachment: TextView
    private lateinit var toolsAudio: TextView
    private lateinit var toolsReminder: TextView
    private lateinit var toolsColor: TextView

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        mFileUtil = (activity?.application as MyApplication).fileUtil
        mRootFolder = mFileUtil.rootFolder
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Resolve views from included new_note_tools layout
        toolsBottomSheet = binding.root.findViewById(R.id.toolsBottomSheet)
        toolsDate = binding.root.findViewById(R.id.toolsDate)
        toolsCheckList = binding.root.findViewById(R.id.toolsCheckList)
        toolsCamera = binding.root.findViewById(R.id.toolsCamera)
        toolsAttachment = binding.root.findViewById(R.id.toolsAttachment)
        toolsAudio = binding.root.findViewById(R.id.toolsAudio)
        toolsReminder = binding.root.findViewById(R.id.toolsReminder)
        toolsColor = binding.root.findViewById(R.id.toolsColor)

        childFragmentManager.setFragmentResultListener(
            PasswordDialogFragment.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(PasswordDialogFragment.RESULT_PASSWORD_CREATED, false)) {
                mLockMenu?.let { item ->
                    mIsLocked = setMenuState(mIsLocked, item, R.drawable.ic_lock_48px, R.drawable.ic_lock_open_48px, "Locked")
                }
            }
        }

        Debug.startMethodTracing()
        initialize()
        listeners()
        Debug.stopMethodTracing()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        mNoteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        mBottomSheetBehavior = BottomSheetBehavior.from(toolsBottomSheet)

        toolsBottomSheet.apply {
            mBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, newState: Float) {
                    binding.newNoteBackground.alpha = (newState / 2)
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {}
                        BottomSheetBehavior.STATE_EXPANDED -> {}
                        BottomSheetBehavior.STATE_COLLAPSED -> {}
                        BottomSheetBehavior.STATE_DRAGGING -> {}
                        BottomSheetBehavior.STATE_SETTLING -> {}
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {}
                    }
                }
            })
        }

        mImageAdapter = ImageAdapter(false, this)
        binding.newNoteImageList.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = mImageAdapter
        }
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.newNoteImageList)
        binding.newNoteImageList.addItemDecoration(CirclePagerIndicatorDecoration())

        mAudioListAdapter = AudioListAdapter(this)
        binding.newNoteAudioList.apply {
            layoutManager = LinearLayoutManager(activity)
            itemAnimator = DefaultItemAnimator()
            adapter = mAudioListAdapter
        }

        mCheckListAdapter = CheckListAdapter(this, this)
        binding.newNoteCheckList.apply {
            layoutManager = LinearLayoutManager(activity)
            itemAnimator = DefaultItemAnimator()
            adapter = mCheckListAdapter
        }

        if (arguments != null) setNote()
    }

    private fun listeners() {
        compositeDisposable.addAll(

                toolsDate.clicks().subscribe { changeState() },

                toolsCheckList.clicks().subscribe({
                    changeState()
                    addCheckListItem()
                }, { throwable -> Timber.d(throwable.message) }),

                toolsCamera.clicks().subscribe({
                    changeState()
                    // BUG FIX: Removed WRITE_EXTERNAL_STORAGE permission check.
                    // On API 29+ that permission is auto-denied and the callback never fires.
                    // Camera permission is the correct one to request for TakePicture.
                    checkPermission(Manifest.permission.CAMERA, EXTERNAL_STORAGE_PERMISSION_CODE, ::startCameraIntent)
                }, { throwable -> Timber.d(throwable.message) }),

                toolsAttachment.clicks().subscribe({
                    changeState()
                    // ST-002 FIX: Removed checkPermission(READ_EXTERNAL_STORAGE) wrapper.
                    // ActivityResultContracts.GetContent() uses the system document picker,
                    // which grants temporary read access via content URI — no storage
                    // permission needed. READ_EXTERNAL_STORAGE is also broken on API 33+
                    // (replaced by granular READ_MEDIA_IMAGES).
                    galleryLauncher.launch("image/*")
                }, { throwable -> Timber.d(throwable.message) }),

                toolsAudio.clicks().subscribe({
                    changeState()
                    checkPermission(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_PERMISSION_CODE, ::recordAudio)
                }, { throwable -> Timber.d(throwable.message) }),

                toolsReminder.clicks().subscribe({
                    changeState()
                    ReminderUtil.setReminder(activity!!, binding.newNoteTitle.text.toString(), binding.newNoteContent.text.toString())
                }, { throwable -> Timber.d(throwable.message) }),

                toolsColor.clicks().subscribe({
                    changeState()
                    ColorPickerDialogFragment().show(parentFragmentManager, "color")
                }, { throwable -> Timber.d(throwable.message) })

        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_note_menu, menu)
        if (menu.getItem(1) != null) mStarMenu = menu.getItem(1)
        if (menu.getItem(2) != null) mLockMenu = menu.getItem(2)
        if (mNoteEntity == null) menu.getItem(3)?.isVisible = false
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
        if (PasswordUtil.checkLock((activity?.applicationContext as MyApplication).sharedPreferences, activity!!, childFragmentManager, "password"))
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

    private fun setNote() {

        mFolderName = arguments?.getString(activity?.resources?.getString(R.string.bundleListFragmentFolderName), "noFolder")!!

        binding.newNoteContent.setText(arguments?.getString(activity?.resources?.getString(R.string.bundleReceiveIntentText), ""))

        val imageUri: Uri? = arguments?.getParcelable(activity?.resources?.getString(R.string.bundleReceiveIntentImage))
        val imageUris: List<Uri?>? = arguments?.getParcelableArrayList(activity?.resources?.getString(R.string.bundleReceiveIntentImageList))

        if (imageUri != null) saveImage(MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri))
        else imageUris?.forEach {
            saveImage(MediaStore.Images.Media.getBitmap(activity?.contentResolver, it))
        }

        val noteBytes = arguments?.getByteArray(activity?.resources?.getString(R.string.bundleNoteEntity))
        mNoteEntity = if (noteBytes != null) Json.decodeFromString<NoteEntity>(String(noteBytes, Charsets.UTF_8)) else null

        if (mNoteEntity == null) return

        val id: Int = mNoteEntity?.uid!!

        mFileUtil.getLiveNote(mNoteEntity?.fileName!!).observe(this, androidx.lifecycle.Observer {

            mNoteEntity = it

            mNoteEntity?.uid = id

            binding.newNoteTitle.setText(mNoteEntity?.title)
            binding.newNoteContent.setText(mNoteEntity?.noteContent)

            mFolderName = mNoteEntity?.folderName!!

            mColorCode = mNoteEntity?.color!!

            binding.newNoteTitle.setTextColor(Color.parseColor(mColorCode))

            mIsStarred = mNoteEntity?.pinned!!

            mIsLocked = mNoteEntity?.locked!!

            toolsDate.text = AppUtil.getTimeDifference(Calendar.getInstance().timeInMillis - mNoteEntity?.dateModified?.time!!)

            if (mNoteEntity?.imageList != null && mNoteEntity?.imageList?.isNotEmpty()!!) {
                binding.newNoteImageList.visibility = View.VISIBLE
                mImagesLocations.addAll(mNoteEntity?.imageList!!)
                // Pass a new list copy so AsyncListDiffer detects the change
                mImageAdapter.submitList(mImagesLocations.toList())
            }

            if (mNoteEntity?.audioList != null && mNoteEntity?.audioList?.isNotEmpty()!!) {
                binding.newNoteAudioList.visibility = View.VISIBLE
                mAudioLocations.addAll(mNoteEntity?.audioList!!)
                // Pass a new list copy so AsyncListDiffer detects the change
                mAudioListAdapter.submitList(mAudioLocations.toList())
            }

            if (mNoteEntity?.checkList != null && mNoteEntity?.checkList?.isNotEmpty()!!) {
                binding.newNoteCheckList.visibility = View.VISIBLE
                mCheckList.addAll(mNoteEntity?.checkList!!)
                // Pass a new list copy so AsyncListDiffer detects the change
                mCheckListAdapter.submitList(mCheckList.toList())
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
        // Pass a new list copy so AsyncListDiffer detects the change
        mCheckListAdapter.submitList(mCheckList.toList())
        // BUG FIX: removed redundant notifyItemInserted — submitList() handles adapter notification via DiffUtil
        changeVisibility(mCheckList, binding.newNoteCheckList)
    }

    private fun addAudioToList(audioFileLocation: String) {
        mAudioLocations.add(audioFileLocation)
        // Pass a new list copy so AsyncListDiffer detects the change
        mAudioListAdapter.submitList(mAudioLocations.toList())
        // BUG FIX: removed redundant notifyItemInserted — submitList() handles adapter notification via DiffUtil
        changeVisibility(mAudioLocations, binding.newNoteAudioList)
    }

    private fun addImageToList(imageLocation: String) {
        mImagesLocations.add(imageLocation)
        // Pass a new list copy so AsyncListDiffer detects the change
        mImageAdapter.submitList(mImagesLocations.toList())
        // BUG FIX: removed redundant notifyItemInserted — submitList() handles adapter notification via DiffUtil
        changeVisibility(mImagesLocations, binding.newNoteImageList)
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

    private fun startCameraIntent() {
        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        if (photoFile != null) {
            // ST-002 FIX: Use BuildConfig.APPLICATION_ID so the authority matches the
            // flavor-specific ${applicationId}.fileprovider declared in AndroidManifest.xml
            val photoURI = FileProvider.getUriForFile(activity!!, "${BuildConfig.APPLICATION_ID}.fileprovider", photoFile)
            cameraLauncher.launch(photoURI)
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
        noteEntity.title = binding.newNoteTitle.text.toString()
        noteEntity.noteContent = binding.newNoteContent.text.toString()
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
        if (binding.newNoteTitle.text.toString().isEmpty() && binding.newNoteContent.text.toString().isEmpty())
            Toast.makeText(activity!!, resources.getString(R.string.noNote), Toast.LENGTH_LONG).show()
        return true
    }

    private fun changeState() {
        mBottomSheetBehavior.state = if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
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
                // Pass a new list copy so AsyncListDiffer detects the change
                mImageAdapter.submitList(mImagesLocations.toList())
                // BUG FIX: removed redundant notifyItemRemoved — submitList() handles adapter notification via DiffUtil
            }
            AdapterType.AUDIO_ADAPTER -> {
                mAudioLocations.removeAt(position)
                // Pass a new list copy so AsyncListDiffer detects the change
                mAudioListAdapter.submitList(mAudioLocations.toList())
                // BUG FIX: removed redundant notifyItemRemoved — submitList() handles adapter notification via DiffUtil
            }
            AdapterType.CHECKLIST_ADAPTER -> {
                mCheckList.removeAt(position)
                // Pass a new list copy so AsyncListDiffer detects the change
                mCheckListAdapter.submitList(mCheckList.toList())
                // BUG FIX: removed redundant notifyItemRemoved — submitList() handles adapter notification via DiffUtil
            }
        }
    }

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
        binding.newNoteTitle.setTextColor(Color.parseColor(mColorCode))
    }

    @Subscribe
    fun onFullScreenEvent(fullScreenEvent: FullScreenEvent) {
        val resources = activity?.resources
        val bundle = Bundle()
        val byteArray: ByteArray = Json.encodeToString(mImagesLocations.toList()).toByteArray(Charsets.UTF_8)
        bundle.putByteArray(resources?.getString(R.string.bundleImageList), byteArray)
        bundle.putInt(resources?.getString(R.string.bundleImageListPosition), fullScreenEvent.position)
        binding.newNoteImageList.findNavController().navigate(R.id.noteToImageList, bundle)
    }

    enum class FILE_TYPES {
        TEXT, IMAGE, AUDIO
    }

    private enum class ActionType {
        SAVE, UPDATE
    }
}
