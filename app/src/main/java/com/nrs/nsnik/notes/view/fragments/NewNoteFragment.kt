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

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.model.CheckListObject
import com.nrs.nsnik.notes.util.FileUtil
import com.nrs.nsnik.notes.util.events.ColorPickerEvent
import com.nrs.nsnik.notes.util.events.FullScreenEvent
import com.nrs.nsnik.notes.util.receiver.NotificationReceiver
import com.nrs.nsnik.notes.view.adapters.AudioListAdapter
import com.nrs.nsnik.notes.view.adapters.CheckListAdapter
import com.nrs.nsnik.notes.view.adapters.ImageAdapter
import com.nrs.nsnik.notes.view.fragments.dialogFragments.ColorPickerDialogFragment
import com.nrs.nsnik.notes.view.listeners.AdapterType
import com.nrs.nsnik.notes.view.listeners.OnAddClickListener
import com.nrs.nsnik.notes.view.listeners.OnItemRemoveListener
import com.nrs.nsnik.notes.viewmodel.NoteViewModel
import com.twitter.serial.serializer.CollectionSerializers
import com.twitter.serial.serializer.CoreSerializers
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial
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

class NewNoteFragment : Fragment(), OnAddClickListener, OnItemRemoveListener {

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

    private var mColorCode: String = "#333333"

    private var mStarMenu: MenuItem? = null
    private var mLockMenu: MenuItem? = null

    private var mCurrentPhotoPath: String? = null

    private var mImagesLocations: MutableList<String> = mutableListOf()
    private var mAudioLocations: MutableList<String> = mutableListOf()
    private var mCheckList: MutableList<CheckListObject> = mutableListOf()
    private var mFilesToDelete: List<String>? = null

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
        initialize()
        listeners()
    }

    private fun initialize() {
        mNoteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)

        mBottomSheetBehavior = BottomSheetBehavior.from(toolsBottomSheet)

        toolsBottomSheet.apply {
            mBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, newState: Float) {

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

        //Other initializations
        mFilesToDelete = ArrayList()

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
                    checkWriteExternalStoragePermission()
                }, { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(toolsAttachment).subscribe({
                    changeState()
                    checkReadExternalStoragePermission()
                }, { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(toolsAudio).subscribe({
                    changeState()
                    checkAudioRecordPermission()
                }, { throwable -> Timber.d(throwable.message) }),

                RxView.clicks(toolsReminder).subscribe({
                    changeState()
                    setReminder()
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
        setMenuIconState()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.newNoteMenuSave -> {
                if (verifyAndSave()) if (mNoteEntity == null) noteAction(NoteEntity(), ACTIONTYPE.SAVE) else noteAction(mNoteEntity!!, ACTIONTYPE.UPDATE)
            }
            R.id.newNoteMenuDelete -> {
            }
            R.id.newNoteMenuStar -> mIsStarred = setMenuState(mIsStarred, item, R.drawable.ic_star_black_48px, R.drawable.ic_star_border_black_48px, "Starred")
            R.id.newNoteMenuLock -> mIsLocked = setMenuState(mIsLocked, item, R.drawable.ic_lock_black_48px, R.drawable.ic_lock_open_black_48px, "Locked")
            android.R.id.home -> activity?.findNavController(R.id.mainNavHost)?.navigateUp()
        }
        return true
    }

    private fun setMenuState(state: Int, item: MenuItem, drawable: Int = 0, drawableAlt: Int = 0, message: String): Int {
        return if (state == 0) {
            item.setIcon(drawable)
            activity?.toast(message, Toast.LENGTH_LONG)
            1
        } else {
            item.setIcon(drawableAlt)
            activity?.toast("Un $message", Toast.LENGTH_LONG)
            0
        }
    }

    private fun setNote() {

        mFolderName = arguments?.getString(activity?.resources?.getString(R.string.bundleListFragmentFolderName), "noFolder")!!

        mNoteEntity = ByteBufferSerial().fromByteArray(arguments?.getByteArray(activity?.resources?.getString(R.string.bundleNoteEntity)), NoteEntity.SERIALIZER)

        if (mNoteEntity == null) return

        val noteEntity: NoteEntity = mFileUtil.getNote(mNoteEntity?.fileName!!)

        newNoteTitle.setText(noteEntity.title)
        newNoteContent.setText(noteEntity.noteContent)

        mFolderName = noteEntity.folderName!!

        mColorCode = noteEntity.color!!

        newNoteTitle.setTextColor(Color.parseColor(mColorCode))

        mIsStarred = noteEntity.pinned

        mIsLocked = noteEntity.locked

        toolsDate.text = getTimeDifference(Calendar.getInstance().timeInMillis - noteEntity.dateModified?.time!!)

        if (noteEntity.imageList != null && noteEntity.imageList?.isNotEmpty()!!) {
            newNoteImageList.visibility = View.VISIBLE
            mImagesLocations.addAll(noteEntity.imageList!!)
            mImageAdapter.submitList(mImagesLocations)
        }

        if (noteEntity.audioList != null && noteEntity.audioList?.isNotEmpty()!!) {
            newNoteAudioList!!.visibility = View.VISIBLE
            mAudioLocations.addAll(noteEntity.audioList!!)
            mAudioListAdapter.submitList(mAudioLocations)
        }

        if (noteEntity.checkList != null && noteEntity.checkList?.isNotEmpty()!!) {
            newNoteCheckList!!.visibility = View.VISIBLE
            mCheckList.addAll(noteEntity.checkList!!)
            mCheckListAdapter.submitList(mCheckList)
        }

        if (noteEntity.hasReminder != 0) {
            mHasReminder = 1
        }

        setMenuIconState()
    }

    private fun setMenuIconState() {
        mStarMenu?.setIcon(if (mIsStarred == 1) R.drawable.ic_star_black_48px else R.drawable.ic_star_border_black_48px)
        mLockMenu?.setIcon(if (mIsLocked == 1) R.drawable.ic_lock_black_48px else R.drawable.ic_lock_open_black_48px)
    }

    private fun getTimeDifference(diff: Long): String {
        val seconds = (diff / 1000).toInt()
        if (seconds < 60) return "$seconds sec ago"
        val minutes = (seconds / 60)
        if (minutes < 60) return "$minutes min ago"
        val hours = (minutes / 60)
        return "$hours hrs ago"
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

    private fun checkWriteExternalStoragePermission() {
        if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), EXTERNAL_STORAGE_PERMISSION_CODE)
            return
        }
        startCameraIntent()
    }

    private fun checkReadExternalStoragePermission() {
        if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_PERMISSION)
            return
        }
        startGalleryIntent()
    }

    private fun checkAudioRecordPermission() {
        if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
            return
        }
        recordAudio()
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
        if (chosePicture.resolveActivity(activity?.packageManager) != null) {
            startActivityForResult(chosePicture, ATTACH_PICTURE_REQUEST_CODE)
        } else {
            activity?.toast("getResources().getString(R.string.noGallery)", Toast.LENGTH_LONG)
        }
    }

    private fun addGalleryPhotoToList(data: Intent) {
        val imageUri = data.data
        try {
            val image = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
            val imageFileName = makeName(FILE_TYPES.IMAGE)
            mFileUtil.saveImage(image, imageFileName)
            addImageToList(imageFileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun addCameraPhotoToList() {
        val image = BitmapFactory.decodeFile(mCurrentPhotoPath)
        val imageFileName = makeName(FILE_TYPES.IMAGE)
        try {
            mFileUtil.saveImage(image, imageFileName)
            addImageToList(imageFileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun recordAudio() {
        //Creating new file for audio
        val audioFileName = makeName(FILE_TYPES.AUDIO)
        val audioFileAbsolutePath = File(mRootFolder, audioFileName)

        //Initializing the media recorder
        val recorder = MediaRecorder()
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        recorder.setOutputFile(audioFileAbsolutePath.absolutePath)

        //Creating the audio recorder dialog
        val record = AlertDialog.Builder(activity!!)
        record.setMessage(resources.getString(R.string.audioRecording))
        record.setNeutralButton(resources.getString(R.string.audioStopRecording)) { dialogInterface, i ->
            recorder.stop()
            recorder.reset()
            recorder.release()
            addAudioToList(audioFileName)
        }
        record.setCancelable(false)
        record.create().show()

        try {
            recorder.prepare()
            recorder.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun setReminder() {
        val calendar = Calendar.getInstance()
        val time = TimePickerDialog(activity!!, { timePicker, hour, minutes ->
            mHasReminder = 1
            setNotification(calendar, hour, minutes)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
        time.show()
    }

    private fun setNotification(calendar: Calendar, hour: Int, minutes: Int) {
        val myIntent = Intent(activity!!, NotificationReceiver::class.java)

        myIntent.putExtra(resources.getString(R.string.notificationTitle), newNoteTitle.text.toString())

        myIntent.putExtra(resources.getString(R.string.notificationContent), newNoteContent.text.toString())

        val alarmManager = activity?.getSystemService(ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(activity!!, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minutes)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    private fun noteAction(noteEntity: NoteEntity, action: ACTIONTYPE) {
        noteEntity.title = newNoteTitle.text.toString()
        noteEntity.noteContent = newNoteContent.text.toString()
        noteEntity.folderName = mFolderName
        noteEntity.fileName = makeName(FILE_TYPES.TEXT)
        noteEntity.color = mColorCode
        noteEntity.dateModified = Calendar.getInstance().time
        noteEntity.imageList = mImagesLocations
        noteEntity.audioList = mAudioLocations
        noteEntity.checkList = mCheckList
        noteEntity.pinned = mIsStarred
        noteEntity.locked = mIsLocked
        noteEntity.hasReminder = mHasReminder
        if (action == ACTIONTYPE.SAVE) mNoteViewModel.insertNote(noteEntity) else mNoteViewModel.updateNote(noteEntity)
        activity?.findNavController(R.id.mainNavHost)?.navigateUp()
    }

    private fun verifyAndSave(): Boolean {
        if (newNoteTitle.text.toString().isEmpty() && newNoteContent.text.toString().isEmpty())
            activity?.toast(resources.getString(R.string.noNote), Toast.LENGTH_LONG)
        return true
    }

    private fun changeState() {
        mBottomSheetBehavior.state = if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ATTACH_PICTURE_REQUEST_CODE -> if (resultCode == RESULT_OK && resultCode != RESULT_CANCELED) {
                if (data != null) addGalleryPhotoToList(data)
            }
            TAKE_PICTURE_REQUEST_CODE -> if (resultCode == RESULT_OK && resultCode != RESULT_CANCELED) {
                addCameraPhotoToList()
            }
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
                showUndoBar(position)
                mImagesLocations.removeAt(position)
                mImageAdapter.submitList(mImagesLocations)
                mImageAdapter.notifyItemRemoved(position)
            }
            AdapterType.AUDIO_ADAPTER -> {
                showUndoBar(position)
                mAudioLocations.removeAt(position)
                mAudioListAdapter.submitList(mAudioLocations)
                mAudioListAdapter.notifyItemRemoved(position)
            }
            AdapterType.CHECKLIST_ADAPTER -> {
                showUndoBar(position)
                mCheckList.removeAt(position)
                mCheckListAdapter.submitList(mCheckList)
                mCheckListAdapter.notifyItemRemoved(position)
            }
        }
    }

    private fun showUndoBar(position: Int) {
        val resources = activity?.resources
        Snackbar.make(activity?.findViewById(R.id.mainDrawerLayout)!!, resources?.getString(R.string.itemRemoved)!!, Snackbar.LENGTH_LONG)
                .setAction(resources.getString(R.string.undo)) { Timber.d(position.toString()) }
                .setActionTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
                .show()
    }

    private fun deleteClearedItems() {
        if (mFilesToDelete!!.isNotEmpty()) {
            //mFileOperation.deleteFileList(mFilesToDelete);
        }
    }

    private fun makeName(type: FILE_TYPES): String {
        val c = Calendar.getInstance()
        return when (type) {
            FILE_TYPES.TEXT -> c.timeInMillis.toString() + ".txt"
            FILE_TYPES.IMAGE -> c.timeInMillis.toString() + ".jpg"
            FILE_TYPES.AUDIO -> c.timeInMillis.toString() + ".3gp"
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

    private enum class FILE_TYPES {
        TEXT, IMAGE, AUDIO
    }

    private enum class ACTIONTYPE {
        SAVE, UPDATE
    }
}