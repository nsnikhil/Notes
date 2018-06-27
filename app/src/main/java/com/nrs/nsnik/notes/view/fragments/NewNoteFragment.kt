/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.fragments

import android.Manifest
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
import androidx.core.content.FileProvider
import androidx.core.widget.toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.model.CheckListObject
import com.nrs.nsnik.notes.util.FileUtil
import com.nrs.nsnik.notes.util.events.ColorPickerEvent
import com.nrs.nsnik.notes.util.receiver.NotificationReceiver
import com.nrs.nsnik.notes.view.adapters.AudioListAdapter
import com.nrs.nsnik.notes.view.adapters.CheckListAdapter
import com.nrs.nsnik.notes.view.adapters.ImageAdapter
import com.nrs.nsnik.notes.view.fragments.dialogFragments.ColorPickerDialogFragment
import com.nrs.nsnik.notes.view.listeners.OnAddClickListener
import com.nrs.nsnik.notes.viewmodel.NoteViewModel
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

class NewNoteFragment : Fragment(), OnAddClickListener {

    companion object {
        private const val ATTACH_PICTURE_REQUEST_CODE = 205
        private const val TAKE_PICTURE_REQUEST_CODE = 206
        private const val RECORD_AUDIO_PERMISSION_CODE = 512
        private const val EXTERNAL_STORAGE_PERMISSION_CODE = 513
        private const val READ_EXTERNAL_STORAGE_PERMISSION = 514
    }

    //Variables used in saving or updating note
    private var mFolderName: String? = "nofolder"

    private var mIsLocked: Int = 0
    private var mIsStarred: Int = 0
    private var mHasReminder: Int = 0

    private var mColorCode: String? = null


    private var mStarMenu: MenuItem? = null
    private var mLockMenu: MenuItem? = null

    private var mCurrentPhotoPath: String? = null

    private var mImagesLocations: MutableList<String>? = null
    private var mAudioLocations: MutableList<String>? = null
    private var mCheckList: MutableList<CheckListObject>? = null
    private var mFilesToDelete: List<String>? = null

    private var mImageAdapter: ImageAdapter? = null
    private var mAudioListAdapter: AudioListAdapter? = null
    private var mCheckListAdapter: CheckListAdapter? = null

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var mNoteViewModel: NoteViewModel? = null
    private var mFileUtil: FileUtil? = null
    private var mRootFolder: File? = null


    private var mNoteEntity: NoteEntity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_new_note, container, false)
        mFileUtil = (activity?.application as MyApplication).fileUtil
        mRootFolder = mFileUtil!!.rootFolder
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        listeners()
    }


    private fun initialize() {
        mNoteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)


        if (arguments != null) {
            setNote()
        }

        toolsBottomSheet.apply {
            BottomSheetBehavior.from(toolsBottomSheet).setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, newState: Float) {
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }

            })
        }


        mImagesLocations = ArrayList()
        newNoteImageList.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = ImageAdapter(false)
        }


        //Audio List Setup
        mAudioLocations = ArrayList()
        newNoteAudioList.apply {
            layoutManager = LinearLayoutManager(activity)
            itemAnimator = DefaultItemAnimator()
            adapter = AudioListAdapter()
        }


        //Check List Setup
        mCheckList = ArrayList()
        newNoteCheckList.apply {
            layoutManager = LinearLayoutManager(activity)
            itemAnimator = DefaultItemAnimator()
            adapter = CheckListAdapter(this@NewNoteFragment)
        }

        //Other initializations
        mFilesToDelete = ArrayList()

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
        if (menu?.getItem(0) != null) {
            mStarMenu = menu.getItem(0)
        }
        if (menu?.getItem(1) != null) {
            mLockMenu = menu.getItem(1)
        }
        setMenuIconState()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.newNoteMenuStar -> mIsStarred = setMenuState(mIsStarred, item, R.drawable.ic_star_black_48px, R.drawable.ic_star_border_black_48px, "Starred")
            R.id.newNoteMenuLock -> mIsLocked = setMenuState(mIsLocked, item, R.drawable.ic_lock_black_48px, R.drawable.ic_lock_open_black_48px, "Locked")
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
        if (mNoteEntity != null) {

            //mNoteEntity!!.uid = mNoteId

            newNoteTitle.setText(mNoteEntity!!.title)
            newNoteContent.setText(mNoteEntity!!.noteContent)

            mFolderName = mNoteEntity!!.folderName

            mColorCode = mNoteEntity!!.color

            newNoteTitle!!.setTextColor(Color.parseColor(mColorCode))

            mIsStarred = mNoteEntity!!.pinned

            mIsLocked = mNoteEntity!!.locked

            toolsDate!!.text = mNoteEntity!!.dateModified!!.toString()

            if (mNoteEntity!!.imageList!!.isNotEmpty()) {
                newNoteImageList.visibility = View.VISIBLE
                mImagesLocations!!.addAll(mNoteEntity!!.imageList!!)
                mImageAdapter?.notifyDataSetChanged()
            }


            if (mNoteEntity!!.audioList!!.isNotEmpty()) {
                newNoteAudioList!!.visibility = View.VISIBLE
                mAudioLocations!!.addAll(mNoteEntity!!.audioList!!)
                mAudioListAdapter?.notifyDataSetChanged()
            }


            if (mNoteEntity!!.checkList!!.isNotEmpty()) {
                newNoteCheckList!!.visibility = View.VISIBLE
                mCheckList!!.addAll(mNoteEntity!!.checkList!!)
                mCheckListAdapter?.notifyDataSetChanged()
            }

            if (mNoteEntity!!.hasReminder != 0) {
                mHasReminder = 1
            }
        }
    }

    private fun setMenuIconState() {
        if (mStarMenu != null) {
            if (mIsStarred == 1) {
                mStarMenu!!.setIcon(R.drawable.ic_star_black_48px)
            } else {
                mStarMenu!!.setIcon(R.drawable.ic_star_border_black_48px)
            }
        }
        if (mLockMenu != null) {
            if (mIsLocked == 1) {
                mLockMenu!!.setIcon(R.drawable.ic_lock_black_48px)
            } else {
                mLockMenu!!.setIcon(R.drawable.ic_lock_open_black_48px)
            }
        }
    }


    private fun addCheckListItem() {
        mCheckList!!.add(CheckListObject().apply {
            text = ""
            done = false
        })
        mCheckListAdapter!!.notifyDataSetChanged()
        if (mCheckList!!.isNotEmpty()) newNoteCheckList.visibility = View.VISIBLE else View.GONE
    }


    private fun addAudioToList(audioFileLocation: String) {
        mAudioLocations!!.add(audioFileLocation)
        mAudioListAdapter!!.notifyDataSetChanged()
        if (mAudioLocations!!.isNotEmpty()) newNoteAudioList.visibility = View.VISIBLE else View.GONE

    }


    private fun addImageToList(imageLocation: String) {
        mImagesLocations!!.add(imageLocation)
        mImageAdapter!!.notifyDataSetChanged()
        if (mImagesLocations!!.isNotEmpty()) newNoteImageList.visibility = View.VISIBLE else View.GONE
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
            mFileUtil!!.saveImage(image, imageFileName)
            addImageToList(imageFileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun addCameraPhotoToList() {
        val image = BitmapFactory.decodeFile(mCurrentPhotoPath)
        val imageFileName = makeName(FILE_TYPES.IMAGE)
        try {
            mFileUtil!!.saveImage(image, imageFileName)
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
        noteEntity.color = if (mColorCode != null) mColorCode else "#333333"
        noteEntity.dateModified = Calendar.getInstance().time
        noteEntity.imageList = mImagesLocations
        noteEntity.audioList = mAudioLocations
        noteEntity.checkList = mCheckList
        noteEntity.pinned = mIsStarred
        noteEntity.locked = mIsLocked
        noteEntity.hasReminder = mHasReminder
        if (action == ACTIONTYPE.SAVE) mNoteViewModel!!.insertNote(noteEntity) else mNoteViewModel!!.updateNote(noteEntity)
    }

    private fun verifyAndSave(): Boolean {
        if (newNoteTitle.text.toString().isEmpty() && newNoteContent.text.toString().isEmpty())
            activity?.toast(resources.getString(R.string.noNote), Toast.LENGTH_LONG)
        return true
    }

    private fun changeState() {
//        if (mBottomSheetBehavior!!.getState() === BottomSheetBehavior.STATE_EXPANDED) {
//            mBottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
//        } else {
//            mBottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            ATTACH_PICTURE_REQUEST_CODE -> if (resultCode == RESULT_OK) {
                addGalleryPhotoToList(data)
            }
            TAKE_PICTURE_REQUEST_CODE -> if (resultCode == RESULT_OK) {
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

    private fun deleteClearedItems() {
        if (mFilesToDelete!!.size > 0) {
            //mFileOperation.deleteFileList(mFilesToDelete);
        }
    }

    private fun makeName(type: FILE_TYPES): String {
        val c = Calendar.getInstance()
        return when (type) {
            FILE_TYPES.TEXT -> c.timeInMillis.toString() + ".txt"
            FILE_TYPES.IMAGE -> c.timeInMillis.toString() + ".jpg"
            FILE_TYPES.AUDIO -> c.timeInMillis.toString() + ".3gp"
            else -> throw IllegalArgumentException("Invalid type " + type.toString())
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

    private enum class FILE_TYPES {
        TEXT, IMAGE, AUDIO
    }

    private enum class ACTIONTYPE {
        SAVE, UPDATE
    }

}