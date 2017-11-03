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
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.annotation.RequiresPermission;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.NoteEntity;
import com.nrs.nsnik.notes.model.CheckListObject;
import com.nrs.nsnik.notes.util.FileUtil;
import com.nrs.nsnik.notes.util.events.ColorPickerEvent;
import com.nrs.nsnik.notes.util.receiver.NotificationReceiver;
import com.nrs.nsnik.notes.view.adapters.AudioListAdapter;
import com.nrs.nsnik.notes.view.adapters.CheckListAdapter;
import com.nrs.nsnik.notes.view.adapters.ImageAdapter;
import com.nrs.nsnik.notes.view.fragments.dialogFragments.ColorPickerDialogFragment;
import com.nrs.nsnik.notes.view.listeners.OnAddClickListener;
import com.nrs.nsnik.notes.viewmodel.NoteViewModel;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class NewNoteActivity extends AppCompatActivity implements OnAddClickListener {

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
    @Nullable
    @BindView(R.id.toolsDate)
    TextView mBottomDate;
    @Nullable
    @BindView(R.id.toolsBottomSheet)
    ConstraintLayout mBottomSheet;
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
    NoteEntity mNoteEntity;
    @InjectExtra
    int mNoteId;
    @InjectExtra
    @Nullable
    String mFolderNameBundle;
    //Bottom Sheet View
    private BottomSheetBehavior mBottomSheetBehavior;

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

    private CompositeDisposable mCompositeDisposable;
    private NoteViewModel mNoteViewModel;
    private FileUtil mFileUtil;
    private File mRootFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        ButterKnife.bind(this);
        Dart.inject(this);
        mFileUtil = ((MyApplication) getApplication()).getFileUtil();
        mRootFolder = mFileUtil.getRootFolder();
        initialize();
        listeners();
        if (getSupportActionBar() != null && mNoteEntity != null) {
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
        mNoteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
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
        mImageAdapter = new ImageAdapter(this, mImagesLocations, false);
        mImageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mImageRecyclerView.setAdapter(mImageAdapter);


        //Audio List Setup
        mAudioLocations = new ArrayList<>();
        if (mAudioRecyclerView != null) {
            mAudioRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        mAudioListAdapter = new AudioListAdapter(this, mAudioLocations);
        mAudioRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAudioRecyclerView.setAdapter(mAudioListAdapter);


        //Check List Setup
        mCheckList = new ArrayList<>();
        if (mCheckListRecyclerView != null) {
            mCheckListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        mCheckListAdapter = new CheckListAdapter(this, mCheckList, this);
        mCheckListRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
            }, throwable -> Timber.d(throwable.getMessage())));
        }
        if (mBottomCamera != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomCamera).subscribe(v -> {
                changeState();
                checkWriteExternalStoragePermission();
            }, throwable -> Timber.d(throwable.getMessage())));
        }
        if (mBottomAttachment != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomAttachment).subscribe(v -> {
                changeState();
                checkReadExternalStoragePermission();
            }, throwable -> Timber.d(throwable.getMessage())));
        }
        if (mBottomAudio != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomAudio).subscribe(v -> {
                changeState();
                checkAudioRecordPermission();
            }, throwable -> Timber.d(throwable.getMessage())));
        }
        if (mBottomReminder != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomReminder).subscribe(v -> {
                changeState();
                setReminder();
            }, throwable -> Timber.d(throwable.getMessage())));
        }
        if (mBottomColor != null) {
            mCompositeDisposable.add(RxView.clicks(mBottomColor).subscribe(v -> {
                changeState();
                ColorPickerDialogFragment pickerDialogFragment = new ColorPickerDialogFragment();
                pickerDialogFragment.show(getSupportFragmentManager(), "color");
            }, throwable -> Timber.d(throwable.getMessage())));
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
        if (mNoteEntity != null) {

            //TITLE
            if (mTitle != null) {
                mTitle.setText(mNoteEntity.getTitle());
            }

            //NOTE CONTENT
            if (mNote != null) {
                mNote.setText(mNoteEntity.getNoteContent());
            }

            //FOLDER NAME
            mFolderName = mNoteEntity.getFolderName();

            //COLOR CODE
            mColorCode = mNoteEntity.getColor();

            //COLOR AGAIN
            mTitle.setTextColor(Color.parseColor(mColorCode));

            //IS STARED
            mIsStarred = mNoteEntity.getIsPinned();

            //IS LOCKED
            mIsLocked = mNoteEntity.getIsLocked();

            //DATE
            if (mBottomDate != null && mNoteEntity.getDateModified() != null) {
                mBottomDate.setText(mNoteEntity.getDateModified().toString());
            }

            //IMAGES
            if (mNoteEntity.getImageList().size() > 0) {
                if (mImageRecyclerView != null) {
                    mImageRecyclerView.setVisibility(View.VISIBLE);
                }
                mImagesLocations.addAll(mNoteEntity.getImageList());
                mImageAdapter.notifyDataSetChanged();
            }

            //AUDIO
            if (mNoteEntity.getAudioList().size() > 0) {
                if (mAudioRecyclerView != null) {
                    mAudioRecyclerView.setVisibility(View.VISIBLE);
                }
                mAudioLocations.addAll(mNoteEntity.getAudioList());
                mImageAdapter.notifyDataSetChanged();
            }


            //CHECKLIST
            if (mNoteEntity.getCheckList().size() > 0) {
                if (mCheckListRecyclerView != null) {
                    mCheckListRecyclerView.setVisibility(View.VISIBLE);
                }
                mCheckList.addAll(mNoteEntity.getCheckList());
                mCheckListAdapter.notifyDataSetChanged();
            }

            //REMINDER
            if (mNoteEntity.getHasReminder() != 0) {
                mHasReminder = 1;
            }

        }

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

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
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
            String imageFileName = makeName(FILE_TYPES.IMAGE);
            mFileUtil.saveImage(image, imageFileName);
            addImageToList(imageFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addCameraPhotoToList() {
        Bitmap image = BitmapFactory.decodeFile(mCurrentPhotoPath);
        String imageFileName = makeName(FILE_TYPES.IMAGE);
        try {
            mFileUtil.saveImage(image, imageFileName);
            addImageToList(imageFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private void recordAudio() {
        //Creating new file for audio
        String audioFileName = makeName(FILE_TYPES.AUDIO);
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
        NoteEntity noteEntity;
        if (mTitle != null && mNote != null) {
            noteEntity = new NoteEntity();
            noteEntity.setTitle(mTitle.getText().toString());
            noteEntity.setNoteContent(mNote.getText().toString());
            noteEntity.setFolderName(mFolderName);
            noteEntity.setFileName(makeName(FILE_TYPES.TEXT));
            noteEntity.setColor(mColorCode);
            noteEntity.setDateModified(LocalDateTime.now());
            noteEntity.setImageList(mImagesLocations);
            noteEntity.setAudioList(mAudioLocations);
            noteEntity.setCheckList(mCheckList);
            noteEntity.setIsPinned(mIsStarred);
            noteEntity.setIsLocked(mIsLocked);
            noteEntity.setHasReminder(mHasReminder);
            mNoteViewModel.insertNote(noteEntity);
        }
    }

    private void updateNote() {
        if (mColorCode == null) {
            mColorCode = "#333333";
        }
        if (mNoteEntity != null && mTitle != null && mNote != null) {
            mNoteEntity.setTitle(mTitle.getText().toString());
            mNoteEntity.setNoteContent(mNote.getText().toString());
            mNoteEntity.setFolderName(mFolderName);
            mNoteEntity.setFileName(makeName(FILE_TYPES.TEXT));
            mNoteEntity.setColor(mColorCode);
            mNoteEntity.setDateModified(LocalDateTime.now());
            mNoteEntity.setImageList(mImagesLocations);
            mNoteEntity.setAudioList(mAudioLocations);
            mNoteEntity.setCheckList(mCheckList);
            mNoteEntity.setIsPinned(mIsStarred);
            mNoteEntity.setIsLocked(mIsLocked);
            mNoteEntity.setHasReminder(mHasReminder);
            mNoteViewModel.updateNote(mNoteEntity);
        }
    }

    private boolean verifyAndSave() {
        if ((mNote != null && mTitle != null) && (mNote.getText().toString().isEmpty() || mTitle.getText().toString().isEmpty())) {
            Toast.makeText(this, getResources().getString(R.string.noNote), Toast.LENGTH_SHORT).show();
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
            //mFileOperation.deleteFileList(mFilesToDelete);
        }
    }

    private String makeName(@NonNull FILE_TYPES type) {
        Calendar c = Calendar.getInstance();
        switch (type) {
            case TEXT:
                return c.getTimeInMillis() + ".txt";
            case IMAGE:
                return c.getTimeInMillis() + ".jpg";
            case AUDIO:
                return c.getTimeInMillis() + ".3gp";
            default:
                throw new IllegalArgumentException("Invalid type " + type.toString());
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

    private enum FILE_TYPES {TEXT, IMAGE, AUDIO}
}