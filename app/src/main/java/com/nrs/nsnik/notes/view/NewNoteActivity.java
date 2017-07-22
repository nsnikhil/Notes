/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.data.TableNames;
import com.nrs.nsnik.notes.model.objects.CheckListObject;
import com.nrs.nsnik.notes.model.objects.NoteObject;
import com.nrs.nsnik.notes.util.FileOperation;
import com.nrs.nsnik.notes.util.interfaces.OnAddClickListener;
import com.nrs.nsnik.notes.util.interfaces.OnColorSelectedListener;
import com.nrs.nsnik.notes.util.interfaces.OnItemRemoveListener;
import com.nrs.nsnik.notes.view.adapters.AudioListAdapter;
import com.nrs.nsnik.notes.view.adapters.CheckListAdapter;
import com.nrs.nsnik.notes.view.adapters.ImageAdapter;
import com.nrs.nsnik.notes.view.fragments.dialogFragments.ColorPickerDialogFragment;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewNoteActivity extends AppCompatActivity implements View.OnClickListener, OnAddClickListener, OnItemRemoveListener, OnColorSelectedListener {

    private static final int ATTACH_PICTURE_REQUEST_CODE = 205;
    private static final int TAKE_PICTURE_REQUEST_CODE = 206;

    private static final int RECORD_AUDIO_PERMISSION_CODE = 512;
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 513;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 514;

    @BindView(R.id.newNoteToolbar)
    Toolbar mNoteToolbar;
    @BindView(R.id.newNoteContent)
    EditText mNote;
    @BindView(R.id.newNoteTitle)
    EditText mTitle;
    @BindView(R.id.newNoteImageList)
    RecyclerView mImageRecyclerView;
    @BindView(R.id.newNoteAudioList)
    RecyclerView mAudioRecyclerView;
    @BindView(R.id.newNoteCheckList)
    RecyclerView mCheckListRecyclerView;
    @BindView(R.id.activity_new_note)
    CoordinatorLayout mNoteContainer;

    //Bottom Sheet View
    BottomSheetBehavior mBottomSheetBehavior;
    @BindView(R.id.toolsDate)
    TextView mBottomDate;
    @BindView(R.id.toolsBottomSheet)
    LinearLayout mBottomSheet;
    @BindView(R.id.toolsCheckList)
    TextView mBottomCheckList;
    @BindView(R.id.toolsCamera)
    TextView mBottomCamera;
    @BindView(R.id.toolsAttachment)
    TextView mBottomAttachment;
    @BindView(R.id.toolsReminder)
    TextView mBottomReminder;
    @BindView(R.id.toolsAudio)
    TextView mBottomAudio;
    @BindView(R.id.toolsColor)
    TextView mBottomColor;


    //Variables used in saving or updating note
    private String mFolderName = "nofolder";
    private int IS_LOCKED, IS_STARRED, HAS_ALARM;
    private String mColorCode;
    private Uri mIntentUri = null;
    private MenuItem mStarMenu, mLockMenu;

    private String mCurrentPhotoPath;

    private List<String> mImagesLocations, mAudioLocations;
    private List<CheckListObject> mCheckList;
    private List<String> mFilesToDelete;

    private ImageAdapter mImageAdapter;
    private AudioListAdapter mAudioListAdapter;
    private CheckListAdapter mCheckListAdapter;

    private FileOperation mFileOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        ButterKnife.bind(this);
        mFileOperation = ((MyApplication) getApplicationContext()).getFileOperations();
        initialize();
        setClickListener();
        if (getIntent().getExtras() != null && getIntent().getExtras().getSerializable(getResources().getString(R.string.bundleNoteSerialObject)) != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getResources().getString(R.string.editNote));
            }
            setNote();
        } else {
            if (getIntent().getExtras() != null) {
                mFolderName = getIntent().getExtras().getString(getResources().getString(R.string.newnotefolderbundle));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void initialize() {
        setSupportActionBar(mNoteToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //BottomSheet
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_DRAGGING) {
                    mBottomSheet.setElevation(16);
                } else {
                    mBottomSheet.setElevation(0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        //Image List Setup
        mImagesLocations = new ArrayList<>();
        mImageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mImageAdapter = new ImageAdapter(this, mImagesLocations, this, false);
        mImageRecyclerView.setAdapter(mImageAdapter);


        //Audio List Setup
        mAudioLocations = new ArrayList<>();
        mAudioRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAudioListAdapter = new AudioListAdapter(this, mAudioLocations, this);
        mAudioRecyclerView.setAdapter(mAudioListAdapter);


        //Check List Setup
        mCheckList = new ArrayList<>();
        mCheckListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCheckListAdapter = new CheckListAdapter(this, mCheckList, this);
        mCheckListRecyclerView.setAdapter(mCheckListAdapter);

        //Other initializations
        mFilesToDelete = new ArrayList<>();
    }


    private void setClickListener() {
        mBottomDate.setOnClickListener(this);
        mBottomCheckList.setOnClickListener(this);
        mBottomCamera.setOnClickListener(this);
        mBottomAttachment.setOnClickListener(this);
        mBottomReminder.setOnClickListener(this);
        mBottomAudio.setOnClickListener(this);
        mBottomColor.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolsDate:
                changeState();
                break;
            case R.id.toolsCheckList:
                changeState();
                addCheckListItem();
                break;
            case R.id.toolsCamera:
                changeState();
                checkWriteExternalStoragePermission();
                break;
            case R.id.toolsAttachment:
                changeState();
                checkReadExternalStoragePermission();
                break;
            case R.id.toolsReminder:
                changeState();
                setReminder();
                break;
            case R.id.toolsAudio:
                changeState();
                checkAudioRecordPermission();
                break;
            case R.id.toolsColor:
                changeState();
                ColorPickerDialogFragment pickerDialogFragment = new ColorPickerDialogFragment();
                pickerDialogFragment.show(getSupportFragmentManager(), "color");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_note_menu, menu);
        if (menu.getItem(1) != null) {
            mStarMenu = menu.getItem(1);
        }
        if (menu.getItem(2) != null) {
            mLockMenu = menu.getItem(2);
        }
        setMenuIconState();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newNoteMenuSave:
                if (verifyAndSave()) {
                    if (mIntentUri == null) {
                        try {
                            saveNote();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        updateNote();
                    }
                    finish();
                }
                break;
            case R.id.newNoteMenuStar:
                if (IS_STARRED == 0) {
                    item.setIcon(R.drawable.ic_star_black_48dp);
                    IS_STARRED = 1;
                    Toast.makeText(this, "Starred", Toast.LENGTH_LONG).show();
                } else {
                    item.setIcon(R.drawable.ic_star_border_black_48dp);
                    IS_STARRED = 0;
                    Toast.makeText(this, "Un Starred", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.newNoteMenuLock:
                if (IS_LOCKED == 0) {
                    item.setIcon(R.drawable.ic_lock_outline_black_48dp);
                    IS_LOCKED = 1;
                    Toast.makeText(this, "Locked", Toast.LENGTH_LONG).show();
                } else {
                    item.setIcon(R.drawable.ic_lock_open_black_48dp);
                    IS_LOCKED = 0;
                    Toast.makeText(this, "Un Locked", Toast.LENGTH_LONG).show();
                }
                break;
        }
        return true;
    }

    private void setNote() {
        if (getIntent().getExtras().getSerializable(getResources().getString(R.string.bundleNoteSerialObject)) != null) {
            Bundle args = getIntent().getExtras();
            mIntentUri = Uri.withAppendedPath(TableNames.mContentUri, "noteId/" + args.getInt(getResources().getString(R.string.bundleNoteSerialId)));
            NoteObject object = (NoteObject) args.getSerializable(getResources().getString(R.string.bundleNoteSerialObject));
            if (object != null) {
                mTitle.setText(object.getTitle());
                mNote.setText(object.getNote());
                mFolderName = object.getFolderName();
                mColorCode = object.getmColor();
                mTitle.setTextColor(Color.parseColor(mColorCode));
                IS_STARRED = object.getmIsPinned();
                IS_LOCKED = object.getmIsLocked();
                String editedDate = getResources().getString(R.string.editedHead, mFileOperation.formatDate(object.getmTime()));
                mBottomDate.setText(editedDate);
                if (object.getImages().size() > 0) {
                    mImageRecyclerView.setVisibility(View.VISIBLE);
                    mImagesLocations.addAll(object.getImages());
                    mImageAdapter.notifyDataSetChanged();
                }
                if (object.getAudioLocations().size() > 0) {
                    mAudioRecyclerView.setVisibility(View.VISIBLE);
                    mAudioLocations.addAll(object.getAudioLocations());
                    mImageAdapter.notifyDataSetChanged();
                }
                if (object.getmCheckList().size() > 0) {
                    mCheckListRecyclerView.setVisibility(View.VISIBLE);
                    mCheckList.addAll(object.getmCheckList());
                    mCheckListAdapter.notifyDataSetChanged();
                }
                if (object.getReminder() != 0) {
                    HAS_ALARM = 1;
                }
            }
        }
    }

    private void setMenuIconState() {
        if (mStarMenu != null) {
            if (IS_STARRED == 1) {
                mStarMenu.setIcon(R.drawable.ic_star_black_48dp);
            } else {
                mStarMenu.setIcon(R.drawable.ic_star_border_black_48dp);
            }
        }
        if (mLockMenu != null) {
            if (IS_LOCKED == 1) {
                mLockMenu.setIcon(R.drawable.ic_lock_outline_black_48dp);
            } else {
                mLockMenu.setIcon(R.drawable.ic_lock_open_black_48dp);
            }
        }
    }

    private void addCheckListItem() {
        mCheckList.add(new CheckListObject.CheckListBuilder()
                .setText("")
                .setCompleted(false)
                .build());
        mCheckListAdapter.notifyDataSetChanged();
        displayCheckListView();
    }

    private void displayCheckListView() {
        if (mCheckList.size() > 0) {
            mCheckListRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mCheckListRecyclerView.setVisibility(View.GONE);
        }
    }

    private void addAudioToList(String audioFileLocation) {
        mAudioLocations.add(audioFileLocation);
        mAudioListAdapter.notifyDataSetChanged();
        displayAudioListView();
    }

    private void displayAudioListView() {
        if (mAudioLocations.size() > 0) {
            mAudioRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mAudioRecyclerView.setVisibility(View.GONE);
        }
    }

    private void addImageToList(String imageLocation) {
        mImagesLocations.add(imageLocation);
        mImageAdapter.notifyDataSetChanged();
        displayImageList();
    }

    private void displayImageList() {
        if (mImagesLocations.size() > 0) {
            mImageRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mImageRecyclerView.setVisibility(View.GONE);
        }
    }

    private void checkWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
            return;
        }
        startCameraIntent();
    }

    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
            return;
        }
        startGalleryIntent();
    }

    private void checkAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
            return;
        }
        recordAudio();
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.nrs.nsnik.notes.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void startGalleryIntent() {
        Intent chosePicture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (chosePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chosePicture, ATTACH_PICTURE_REQUEST_CODE);
        } else {
            Toast.makeText(this, getResources().getString(R.string.noGallery), Toast.LENGTH_LONG).show();
        }
    }

    private void addGalleryPhotoToList(Intent data) {
        Uri imageUri = data.getData();
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            String imageFileName = mFileOperation.makeName(FileOperation.FILE_TYPES.IMAGE);
            mFileOperation.saveImage(imageFileName, image);
            addImageToList(imageFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addCameraPhotoToList() {
        Bitmap image = BitmapFactory.decodeFile(mCurrentPhotoPath);
        String imageFileName = mFileOperation.makeName(FileOperation.FILE_TYPES.IMAGE);
        mFileOperation.saveImage(imageFileName, image);
        addImageToList(imageFileName);
    }

    private void recordAudio() {
        //Creating new file for audio
        File folder = getExternalFilesDir(getResources().getString(R.string.folderName));
        String audioFileName = mFileOperation.makeName(FileOperation.FILE_TYPES.AUDIO);
        File audioFileAbsolutePath = new File(folder, audioFileName);


        //Initializing the media recorder
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setOutputFile(audioFileAbsolutePath.getAbsolutePath());

        //Creating the aduio recorder dialog
        AlertDialog.Builder record = new AlertDialog.Builder(NewNoteActivity.this);
        record.setMessage("Recording");
        record.setNeutralButton("Stop Recording", (dialogInterface, i) -> {
            recorder.stop();
            recorder.reset();
            recorder.release();
            addAudioToList(audioFileName);
        });
        record.setCancelable(false);
        record.create().show();

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setReminder() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog time = new TimePickerDialog(NewNoteActivity.this, (timePicker, hour, minutes) -> {
            HAS_ALARM = 1;
            setNotification(calendar, hour, minutes);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        time.show();
    }

    private void setNotification(Calendar calendar, int hour, int minutes) {
        Intent myIntent = new Intent(this, ReminderService.class);
        myIntent.putExtra(getResources().getString(R.string.notificationtitle), mTitle.getText().toString());
        myIntent.putExtra(getResources().getString(R.string.notificationcontent), mNote.getText().toString());
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void saveNote() throws IOException {
        if (mColorCode == null) {
            mColorCode = "#333333";
        }
        Calendar calendar = Calendar.getInstance();
        String time = String.valueOf(calendar.getTimeInMillis());
        NoteObject noteObject = new NoteObject.NoteObjectBuilder()
                .setTitle(mTitle.getText().toString())
                .setNoteContent(mNote.getText().toString())
                .setFolderName(mFolderName)
                .setColor(mColorCode)
                .setTime(time)
                .setImageList(mImagesLocations)
                .setAudioList(mAudioLocations)
                .setCheckList(mCheckList)
                .setPinned(IS_STARRED)
                .setLocked(IS_LOCKED)
                .setHasReminder(HAS_ALARM)
                .build();
        mFileOperation.saveNote(mFileOperation.makeName(FileOperation.FILE_TYPES.TEXT), noteObject, IS_STARRED, IS_LOCKED, time, mColorCode);
    }

    private void updateNote() {
        if (mColorCode == null) {
            mColorCode = "#333333";
        }
        Calendar calendar = Calendar.getInstance();
        String time = String.valueOf(calendar.getTimeInMillis());
        Cursor c = getContentResolver().query(mIntentUri, null, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                NoteObject noteObject = new NoteObject.NoteObjectBuilder()
                        .setTitle(mTitle.getText().toString())
                        .setNoteContent(mNote.getText().toString())
                        .setFolderName(mFolderName)
                        .setColor(mColorCode)
                        .setTime(time)
                        .setImageList(mImagesLocations)
                        .setAudioList(mAudioLocations)
                        .setCheckList(mCheckList)
                        .setPinned(IS_STARRED)
                        .setLocked(IS_LOCKED)
                        .setHasReminder(HAS_ALARM)
                        .build();
                mFileOperation.updateNote(c.getString(c.getColumnIndex(TableNames.table1.mFileName)), noteObject, mIntentUri, IS_STARRED, IS_LOCKED, time, mColorCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
    }

    private boolean verifyAndSave() {
        if (mNote.getText().toString().equalsIgnoreCase("") || mNote.getText().toString().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.noNote), Toast.LENGTH_SHORT).show();
            return false;
        } else if (mTitle.getText().toString().equalsIgnoreCase("") || mTitle.getText().toString().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.noTitle), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void changeState() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ATTACH_PICTURE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    addGalleryPhotoToList(data);
                }
                break;
            case TAKE_PICTURE_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    addCameraPhotoToList();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteClearedItems();
        if (BuildConfig.DEBUG) {
            RefWatcher refWatcher = MyApplication.getRefWatcher(this);
            refWatcher.watch(this);
        }
    }

    @Override
    public void addClickListener() {
        addCheckListItem();
    }

    private void deleteClearedItems() {
        if (mFilesToDelete.size() > 0) {
            mFileOperation.deleteFileList(mFilesToDelete);
        }
    }

    @Override
    public void onItemRemoved(int position, FileOperation.FILE_TYPES types, String fileName) {
        switch (types) {
            case IMAGE:
                mFilesToDelete.add(fileName);
                mImagesLocations.remove(position);
                mImageAdapter.notifyItemRemoved(position);
                displayImageList();
                break;
            case AUDIO:
                mFilesToDelete.add(fileName);
                mAudioLocations.remove(position);
                mAudioListAdapter.notifyItemRemoved(position);
                displayAudioListView();
                break;
        }
    }

    @Override
    public void onColorSelected(String color) {
        mColorCode = color;
        mTitle.setTextColor(Color.parseColor(mColorCode));
    }

    public static class ReminderService extends BroadcastReceiver {
        Context mContext;

        @Override
        public void onReceive(Context context, Intent intent) {
            mContext = context;
            buildNotification(intent);
        }

        private void buildNotification(Intent i) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, mContext.getResources().getString(R.string.notificationChannelReminder));
            notificationBuilder.setSmallIcon(R.drawable.ic_add_alarm_white_48dp);
            notificationBuilder.setContentTitle(i.getExtras().getString(mContext.getResources().getString(R.string.notificationtitle)));
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notificationBuilder.build());
        }
    }
}