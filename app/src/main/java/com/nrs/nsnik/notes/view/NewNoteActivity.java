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
import android.app.PendingIntent;
import android.app.TimePickerDialog;
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
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
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

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.data.TableNames;
import com.nrs.nsnik.notes.model.objects.CheckListObject;
import com.nrs.nsnik.notes.model.objects.NoteObject;
import com.nrs.nsnik.notes.util.FileOperation;
import com.nrs.nsnik.notes.util.events.ColorPickerEvent;
import com.nrs.nsnik.notes.util.interfaces.OnAddClickListener;
import com.nrs.nsnik.notes.util.interfaces.OnItemRemoveListener;
import com.nrs.nsnik.notes.util.receiver.NotificationReceiver;
import com.nrs.nsnik.notes.view.adapters.AudioListAdapter;
import com.nrs.nsnik.notes.view.adapters.CheckListAdapter;
import com.nrs.nsnik.notes.view.adapters.ImageAdapter;
import com.nrs.nsnik.notes.view.fragments.dialogFragments.ColorPickerDialogFragment;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
import io.reactivex.disposables.CompositeDisposable;

public class NewNoteActivity extends AppCompatActivity implements OnAddClickListener, OnItemRemoveListener {

    private static final int ATTACH_PICTURE_REQUEST_CODE = 205;
    private static final int TAKE_PICTURE_REQUEST_CODE = 206;

    private static final int RECORD_AUDIO_PERMISSION_CODE = 512;
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 513;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 514;

    @Nullable
    @BindView(R.id.newNoteToolbar)
    Toolbar mNoteToolbar;
    @Nullable
    @BindView(R.id.newNoteContent)
    EditText mNote;
    @Nullable
    @BindView(R.id.newNoteTitle)
    EditText mTitle;
    @Nullable
    @BindView(R.id.newNoteImageList)
    RecyclerView mImageRecyclerView;
    @Nullable
    @BindView(R.id.newNoteAudioList)
    RecyclerView mAudioRecyclerView;
    @Nullable
    @BindView(R.id.newNoteCheckList)
    RecyclerView mCheckListRecyclerView;
    @Nullable
    @BindView(R.id.activity_new_note)
    CoordinatorLayout mNoteContainer;

    //Bottom Sheet View
    BottomSheetBehavior mBottomSheetBehavior;
    @Nullable
    @BindView(R.id.toolsDate)
    TextView mBottomDate;
    @Nullable
    @BindView(R.id.toolsBottomSheet)
    LinearLayout mBottomSheet;
    @Nullable
    @BindView(R.id.toolsCheckList)
    TextView mBottomCheckList;
    @Nullable
    @BindView(R.id.toolsCamera)
    TextView mBottomCamera;
    @Nullable
    @BindView(R.id.toolsAttachment)
    TextView mBottomAttachment;
    @Nullable
    @BindView(R.id.toolsReminder)
    TextView mBottomReminder;
    @Nullable
    @BindView(R.id.toolsAudio)
    TextView mBottomAudio;
    @Nullable
    @BindView(R.id.toolsColor)
    TextView mBottomColor;
    @InjectExtra
    @Nullable
    NoteObject mNoteObject;
    @InjectExtra
    int mNoteId;
    @InjectExtra
    @Nullable
    String mFolderNameBundle;
    //Variables used in saving or updating note
    @Nullable
    private String mFolderName = "nofolder";
    private int mIsLocked, mIsStarred, mHasReminder;
    private String mColorCode;
    @Nullable
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
    private File mRootFolder;
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        ButterKnife.bind(this);
        mFileOperation = ((MyApplication) getApplicationContext()).getFileOperations();
        mRootFolder = ((MyApplication) getApplicationContext()).getRootFolder();
        Dart.inject(this);
        initialize();
        listeners();

        /*if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable(getResources().getString(R.string.bundleNoteSerialObject)) != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getResources().getString(R.string.editNote));
            }
            setNote();
        } else {
            if (getIntent().getExtras() != null) {
                mFolderName = getIntent().getExtras().getString(getResources().getString(R.string.newnotefolderbundle));
            }
        }*/

        if (getSupportActionBar() != null && mNoteObject != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.editNote));
            setNote();
        } else if (mFolderNameBundle != null) {
            mFolderName = mFolderNameBundle;
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
        if (mBottomSheet != null) {
            mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);

            mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (mBottomSheet != null) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_DRAGGING) {
                            mBottomSheet.setElevation(16);
                        } else {
                            mBottomSheet.setElevation(0);
                        }
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        }

        //Image List Setup
        mImagesLocations = new ArrayList<>();
        if (mImageRecyclerView != null) {
            mImageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }
        mImageAdapter = new ImageAdapter(this, mImagesLocations, this, false);
        mImageRecyclerView.setAdapter(mImageAdapter);


        //Audio List Setup
        mAudioLocations = new ArrayList<>();
        if (mAudioRecyclerView != null) {
            mAudioRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        mAudioListAdapter = new AudioListAdapter(this, mAudioLocations, this);
        mAudioRecyclerView.setAdapter(mAudioListAdapter);


        //Check List Setup
        mCheckList = new ArrayList<>();
        if (mCheckListRecyclerView != null) {
            mCheckListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        mCheckListAdapter = new CheckListAdapter(this, mCheckList, this);
        mCheckListRecyclerView.setAdapter(mCheckListAdapter);

        //Other initializations
        mFilesToDelete = new ArrayList<>();

        mCompositeDisposable = new CompositeDisposable();
    }

    private void listeners() {
        if (mBottomDate != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomDate).subscribe(v -> changeState()));
        }
        if (mBottomCheckList != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomCheckList).subscribe(v -> {
                changeState();
                addCheckListItem();
            }));
        }
        if (mBottomCamera != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomCamera).subscribe(v -> {
                changeState();
                checkWriteExternalStoragePermission();
            }));
        }
        if (mBottomAttachment != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomAttachment).subscribe(v -> {
                changeState();
                checkReadExternalStoragePermission();
            }));
        }
        if (mBottomAudio != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomAudio).subscribe(v -> {
                changeState();
                checkAudioRecordPermission();
            }));
        }
        if (mBottomReminder != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomReminder).subscribe(v -> {
                changeState();
                setReminder();
            }));
        }
        if (mBottomColor != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomColor).subscribe(v -> {
                changeState();
                ColorPickerDialogFragment pickerDialogFragment = new ColorPickerDialogFragment();
                pickerDialogFragment.show(getSupportFragmentManager(), "color");
            }));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newNoteMenuSave:
                if (verifyAndSave()) {
                    if (mIntentUri == null) {
                        saveNote();
                    } else {
                        updateNote();
                    }
                    finish();
                }
                break;
            case R.id.newNoteMenuStar:
                if (mIsStarred == 0) {
                    item.setIcon(R.drawable.ic_star_black_48px);
                    mIsStarred = 1;
                    Toast.makeText(this, "Starred", Toast.LENGTH_LONG).show();
                } else {
                    item.setIcon(R.drawable.ic_star_border_black_48px);
                    mIsStarred = 0;
                    Toast.makeText(this, "Un Starred", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.newNoteMenuLock:
                if (mIsLocked == 0) {
                    item.setIcon(R.drawable.ic_lock_black_48px);
                    mIsLocked = 1;
                    Toast.makeText(this, "Locked", Toast.LENGTH_LONG).show();
                } else {
                    item.setIcon(R.drawable.ic_lock_open_black_48px);
                    mIsLocked = 0;
                    Toast.makeText(this, "Un Locked", Toast.LENGTH_LONG).show();
                }
                break;
        }
        return true;
    }

    private void setNote() {
        if (mNoteObject != null && mNoteId != 0) {
            mIntentUri = Uri.withAppendedPath(TableNames.mContentUri, "noteId/" + mNoteId);
            if (mNoteObject != null) {
                if (mTitle != null) {
                    mTitle.setText(mNoteObject.title());
                }
                if (mNote != null) {
                    mNote.setText(mNoteObject.noteContent());
                }
                mFolderName = mNoteObject.folderName();
                mColorCode = mNoteObject.color();
                mTitle.setTextColor(Color.parseColor(mColorCode));
                mIsStarred = mNoteObject.isPinned();
                mIsLocked = mNoteObject.isLocked();
                String editedDate = getResources().getString(R.string.editedHead, mFileOperation.formatDate(mNoteObject.time()));
                if (mBottomDate != null) {
                    mBottomDate.setText(editedDate);
                }
                if (mNoteObject.imagesList().size() > 0) {
                    if (mImageRecyclerView != null) {
                        mImageRecyclerView.setVisibility(View.VISIBLE);
                    }
                    mImagesLocations.addAll(mNoteObject.imagesList());
                    mImageAdapter.notifyDataSetChanged();
                }
                if (mNoteObject.audioList().size() > 0) {
                    if (mAudioRecyclerView != null) {
                        mAudioRecyclerView.setVisibility(View.VISIBLE);
                    }
                    mAudioLocations.addAll(mNoteObject.audioList());
                    mImageAdapter.notifyDataSetChanged();
                }
                if (mNoteObject.checkList().size() > 0) {
                    if (mCheckListRecyclerView != null) {
                        mCheckListRecyclerView.setVisibility(View.VISIBLE);
                    }
                    mCheckList.addAll(mNoteObject.checkList());
                    mCheckListAdapter.notifyDataSetChanged();
                }
                if (mNoteObject.hasReminder() != 0) {
                    mHasReminder = 1;
                }
            }
        }
        /*if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable(getResources().getString(R.string.bundleNoteSerialObject)) != null) {
            Bundle args = getIntent().getExtras();
            mIntentUri = Uri.withAppendedPath(TableNames.mContentUri, "noteId/" + args.getInt(getResources().getString(R.string.bundleNoteSerialId)));
            NoteObject object = args.getParcelable(getResources().getString(R.string.bundleNoteSerialObject));
            if (object != null) {
                if (mTitle != null) {
                    mTitle.setText(object.title());
                }
                if (mNote != null) {
                    mNote.setText(object.noteContent());
                }
                mFolderName = object.folderName();
                mColorCode = object.color();
                mTitle.setTextColor(Color.parseColor(mColorCode));
                mIsStarred = object.isPinned();
                mIsLocked = object.isLocked();
                String editedDate = getResources().getString(R.string.editedHead, mFileOperation.formatDate(object.time()));
                if (mBottomDate != null) {
                    mBottomDate.setText(editedDate);
                }
                if (object.imagesList().size() > 0) {
                    if (mImageRecyclerView != null) {
                        mImageRecyclerView.setVisibility(View.VISIBLE);
                    }
                    mImagesLocations.addAll(object.imagesList());
                    mImageAdapter.notifyDataSetChanged();
                }
                if (object.audioList().size() > 0) {
                    if (mAudioRecyclerView != null) {
                        mAudioRecyclerView.setVisibility(View.VISIBLE);
                    }
                    mAudioLocations.addAll(object.audioList());
                    mImageAdapter.notifyDataSetChanged();
                }
                if (object.checkList().size() > 0) {
                    if (mCheckListRecyclerView != null) {
                        mCheckListRecyclerView.setVisibility(View.VISIBLE);
                    }
                    mCheckList.addAll(object.checkList());
                    mCheckListAdapter.notifyDataSetChanged();
                }
                if (object.hasReminder() != 0) {
                    mHasReminder = 1;
                }
            }
        }*/
    }

    private void setMenuIconState() {
        if (mStarMenu != null) {
            if (mIsStarred == 1) {
                mStarMenu.setIcon(R.drawable.ic_star_black_48px);
            } else {
                mStarMenu.setIcon(R.drawable.ic_star_border_black_48px);
            }
        }
        if (mLockMenu != null) {
            if (mIsLocked == 1) {
                mLockMenu.setIcon(R.drawable.ic_lock_black_48px);
            } else {
                mLockMenu.setIcon(R.drawable.ic_lock_open_black_48px);
            }
        }
    }

    private void addCheckListItem() {
        mCheckList.add(CheckListObject.builder()
                .text("")
                .done(false)
                .build());
        mCheckListAdapter.notifyDataSetChanged();
        displayCheckListView();
    }

    private void displayCheckListView() {
        if (mCheckList.size() > 0) {
            if (mCheckListRecyclerView != null) {
                mCheckListRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mCheckListRecyclerView != null) {
                mCheckListRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    private void addAudioToList(String audioFileLocation) {
        mAudioLocations.add(audioFileLocation);
        mAudioListAdapter.notifyDataSetChanged();
        displayAudioListView();
    }

    private void displayAudioListView() {
        if (mAudioLocations.size() > 0) {
            if (mAudioRecyclerView != null) {
                mAudioRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mAudioRecyclerView != null) {
                mAudioRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    private void addImageToList(String imageLocation) {
        mImagesLocations.add(imageLocation);
        mImageAdapter.notifyDataSetChanged();
        displayImageList();
    }

    private void displayImageList() {
        if (mImagesLocations.size() > 0) {
            if (mImageRecyclerView != null) {
                mImageRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mImageRecyclerView != null) {
                mImageRecyclerView.setVisibility(View.GONE);
            }
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

    @NonNull
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

    private void addGalleryPhotoToList(@NonNull Intent data) {
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
        String audioFileName = mFileOperation.makeName(FileOperation.FILE_TYPES.AUDIO);
        File audioFileAbsolutePath = new File(mRootFolder, audioFileName);

        //Initializing the media recorder
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setOutputFile(audioFileAbsolutePath.getAbsolutePath());

        //Creating the audio recorder dialog
        AlertDialog.Builder record = new AlertDialog.Builder(NewNoteActivity.this);
        record.setMessage(getResources().getString(R.string.audioRecording));
        record.setNeutralButton(getResources().getString(R.string.audioStopRecording), (dialogInterface, i) -> {
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
            mHasReminder = 1;
            setNotification(calendar, hour, minutes);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        time.show();
    }

    private void setNotification(@NonNull Calendar calendar, int hour, int minutes) {
        Intent myIntent = new Intent(this, NotificationReceiver.class);
        if (mTitle != null) {
            myIntent.putExtra(getResources().getString(R.string.notificationTitle), mTitle.getText().toString());
        }
        if (mNote != null) {
            myIntent.putExtra(getResources().getString(R.string.notificationContent), mNote.getText().toString());
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void saveNote() {
        if (mColorCode == null) {
            mColorCode = "#333333";
        }
        Calendar calendar = Calendar.getInstance();
        String time = String.valueOf(calendar.getTimeInMillis());
        NoteObject noteObject = null;
        if (mTitle != null && mNote != null) {
            noteObject = NoteObject.builder()
                    .title(mTitle.getText().toString())
                    .noteContent(mNote.getText().toString())
                    .folderName(mFolderName)
                    .color(mColorCode)
                    .time(time)
                    .imagesList(mImagesLocations)
                    .audioList(mAudioLocations)
                    .checkList(mCheckList)
                    .isPinned(mIsStarred)
                    .isLocked(mIsLocked)
                    .hasReminder(mHasReminder)
                    .build();

        }
        mFileOperation.saveNote(mFileOperation.makeName(FileOperation.FILE_TYPES.TEXT), noteObject, mIsStarred, mIsLocked, time, mColorCode);
    }

    private void updateNote() {
        if (mColorCode == null) {
            mColorCode = "#333333";
        }
        Calendar calendar = Calendar.getInstance();
        String time = String.valueOf(calendar.getTimeInMillis());
        Cursor c = null;
        if (mIntentUri != null) {
            c = getContentResolver().query(mIntentUri, null, null, null, null);
        }
        try {
            if (c != null && c.moveToFirst()) {
                NoteObject noteObject = null;
                if (mTitle != null && mNote != null) {
                    noteObject = NoteObject.builder()
                            .title(mTitle.getText().toString())
                            .noteContent(mNote.getText().toString())
                            .folderName(mFolderName)
                            .color(mColorCode)
                            .time(time)
                            .imagesList(mImagesLocations)
                            .audioList(mAudioLocations)
                            .checkList(mCheckList)
                            .isPinned(mIsStarred)
                            .isLocked(mIsLocked)
                            .hasReminder(mHasReminder)
                            .build();
                }
                if (noteObject != null) {
                    mFileOperation.updateNote(c.getString(c.getColumnIndex(TableNames.table1.mFileName)), noteObject, mIntentUri, mIsStarred, mIsLocked, time, mColorCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
        deleteClearedItems();
    }

    private boolean verifyAndSave() {
        if (mNote != null && mNote.getText().toString().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.noNote), Toast.LENGTH_SHORT).show();
            return false;
        } else if (mTitle != null && mTitle.getText().toString().isEmpty()) {
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
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        switch (requestCode) {
            case ATTACH_PICTURE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    addGalleryPhotoToList(data);
                }
                break;
            case TAKE_PICTURE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    addCameraPhotoToList();
                }
                break;
        }
    }

    private void cleanUp() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable.dispose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanUp();
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
    public void onItemRemoved(int position, @NonNull FileOperation.FILE_TYPES types, String fileName) {
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
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onColorPickerEvent(ColorPickerEvent colorPickerEvent) {
        mColorCode = colorPickerEvent.getColor();
        if (mTitle != null) {
            mTitle.setTextColor(Color.parseColor(mColorCode));
        }
    }
}