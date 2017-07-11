package com.nrs.nsnik.notes;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.nrs.nsnik.notes.adapters.AudioListAdapter;
import com.nrs.nsnik.notes.adapters.CheckListAdapter;
import com.nrs.nsnik.notes.adapters.ImageAdapter;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.fragments.dialogFragments.ColorPickerDialogFragment;
import com.nrs.nsnik.notes.helpers.FileOperation;
import com.nrs.nsnik.notes.interfaces.SendSize;
import com.nrs.nsnik.notes.objects.CheckListObject;
import com.nrs.nsnik.notes.objects.NoteObject;
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

public class NewNoteActivity extends AppCompatActivity implements View.OnClickListener, SendSize {

    private static final int ATTACH_PICTURE_REQUEST_CODE = 205;
    private static final int TAKE_PICTURE_REQUEST_CODE = 206;
    private static final int GET_COLOR_REQUEST_CODE = 207;


    private static final int RECORD_AUDIO_PERMISSION_CODE = 512;
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 513;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 514;

    private static final String TAG = NewNoteActivity.class.getSimpleName();

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


    private String mFolderName = "nofolder";

    private Uri mIntentUri = null;


    private String mCurrentPhotoPath;

    private List<String> mImagesLocations, mAudioLocations;
    private List<CheckListObject> mCheckList;

    private ImageAdapter mImageAdapter;
    private AudioListAdapter mAudioListAdapter;
    private CheckListAdapter mCheckListAdapter;
    private FileOperation mFileOperation;

    /*
    TODO DOCUMENTATION AFTER REVIEWING THE ENTIRE CODE IN THIS CLASS
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        ButterKnife.bind(this);
        initialize();
        setClickListener();
        if (getIntent().getExtras().getSerializable(getResources().getString(R.string.bundleNoteSerialObject)) != null) {
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

        mFileOperation = new FileOperation(getApplicationContext(), true);

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
        mAudioListAdapter = new AudioListAdapter(this, mAudioLocations);
        mAudioRecyclerView.setAdapter(mAudioListAdapter);


        //Check List Setup
        mCheckList = new ArrayList<>();
        mCheckListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCheckListAdapter = new CheckListAdapter(this, mCheckList);
        mCheckListRecyclerView.setAdapter(mAudioListAdapter);
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
                Toast.makeText(this, "Add CheckList", Toast.LENGTH_SHORT).show();
                break;
            case R.id.toolsCamera:
                Toast.makeText(this, "Start Camera", Toast.LENGTH_SHORT).show();
                break;
            case R.id.toolsAttachment:
                Toast.makeText(this, "Start Gallery", Toast.LENGTH_SHORT).show();
                break;
            case R.id.toolsReminder:
                Toast.makeText(this, "Add Reminder", Toast.LENGTH_SHORT).show();
                break;
            case R.id.toolsAudio:
                Toast.makeText(this, "Add Audio", Toast.LENGTH_SHORT).show();
                changeState();
                break;
            case R.id.toolsColor:
                ColorPickerDialogFragment pickerDialogFragment = new ColorPickerDialogFragment();
                pickerDialogFragment.show(getSupportFragmentManager(), "color");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newNoteMenuSave:
                if (verifyAndSave()) {
                    try {
                        if (mIntentUri != null) {
                            updateNote();
                        } else {
                            saveNote();
                        }
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        return true;
    }

    private void setNote() {
        if (getIntent().getExtras().getSerializable(getResources().getString(R.string.bundleNoteSerialObject)) != null) {
            Bundle args = getIntent().getExtras();
            mIntentUri = Uri.withAppendedPath(TableNames.mContentUri, String.valueOf(args.getInt(getResources().getString(R.string.bundleNoteSerialId))));
            NoteObject object = (NoteObject) args.getSerializable(getResources().getString(R.string.bundleNoteSerialObject));
            if (object != null) {
                File folder = getExternalFilesDir(getResources().getString(R.string.folderName));
                mTitle.setText(object.getTitle());
                mNote.setText(object.getNote());
                mFolderName = object.getFolderName();
                if (object.getImages().size() > 0) {
                    mImageRecyclerView.setVisibility(View.VISIBLE);
                    mImagesLocations.addAll(object.getImages());
                    mImageAdapter.modifyList(mImagesLocations);
                }
                if (object.getAudioLocations().size() > 0) {
                    mAudioRecyclerView.setVisibility(View.VISIBLE);
                    mAudioLocations.addAll(object.getAudioLocations());
                    mAudioListAdapter.modifyList(mAudioLocations);
                }
                if (object.getmCheckList().size() > 0) {
                    mCheckListRecyclerView.setVisibility(View.VISIBLE);
                    mCheckList.addAll(object.getmCheckList());
                    mCheckListAdapter.modifyCheckList(mCheckList);
                }
                /*if (object.getReminder() != 0) {
                    mHour = 292;
                    mMinutes = 392;
                }*/
            }
        }
    }

    private void updateNote() {
        Cursor c = getContentResolver().query(mIntentUri, null, null, null, null);
       /* if (mHour != 289 || mMinutes != 291) {
            mReminder = 1;
        }*/
        /*try {
            if (c != null && c.moveToFirst()) {
                NoteObject noteObject = new NoteObject(mTitle.getText().toString(), mNote.getText().toString(), mImagesLocations, mAudioFileName, mReminder, mFolderName);
                mFileOperation.updateNote(c.getString(c.getColumnIndex(table1.mFileName)), noteObject, mIntentUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }*/
    }

    private void saveNote() throws IOException {
        /*if (mHour != 289 || mMinutes != 291) {
            mReminder = 1;
        }*/
      /*  NoteObject noteObject = new NoteObject(mTitle.getText().toString(), mNote.getText().toString(), mImagesLocations, mAudioFileName, mReminder, mFolderName);
        mFileOperation.saveNote(makeNoteName(), noteObject);*/
    }

    private String makeNoteName() {
        Calendar c = Calendar.getInstance();
        return mTitle.getText().toString() + c.getTimeInMillis() + ".txt";
    }

    private String makeImageName() {
        Calendar c = Calendar.getInstance();
        return mTitle.getText().toString() + c.getTimeInMillis() + ".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ATTACH_PICTURE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    takeGalleryAction(data);
                }
                break;
            case TAKE_PICTURE_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    addToList();
                }
                break;
        }
    }

    private void takeGalleryAction(Intent data) {
        Uri u = data.getData();
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), u);
            mImageRecyclerView.swapAdapter(mImageAdapter, true);
            mImageRecyclerView.setVisibility(View.VISIBLE);
            String name = makeImageName();
            mImagesLocations.add(name);
            mFileOperation.saveImage(name, image);
            mImageAdapter.modifyList(mImagesLocations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToList() {

        int targetW = mNoteContainer.getWidth();
        int targetH = 400;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        String name = makeImageName();
        mImagesLocations.add(name);
        try {
            mFileOperation.saveImage(name, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageAdapter.modifyList(mImagesLocations);
    }


    private void changeState() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void setReminder() {
     /*   Calendar c = Calendar.getInstance();
        TimePickerDialog time = new TimePickerDialog(NewNoteActivity.this, (timePicker, i, i1) -> {
            mHour = i;
            mMinutes = i1;
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        time.show();

        setNotification();*/
    }


    private void checkAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
            return;
        }
        recordAudio();
    }

    private void checkWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
            return;
        }
        startTheCamera();
    }

    private void checkReadeExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
            return;
        }
        startGalleryIntent();
    }

    private void startGalleryIntent() {
        Intent chosePicture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (chosePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chosePicture, ATTACH_PICTURE_REQUEST_CODE);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.noGallery), Toast.LENGTH_SHORT).show();
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

    private void startTheCamera() {
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

    private void recordAudio() {
       /* File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
        Calendar c = Calendar.getInstance();
        if (mAudioFileName == null) {
            mAudioFileName = c.getTimeInMillis() + "audio.3gp";
        }
        File f = new File(folder, mAudioFileName);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(String.valueOf(f));
        AlertDialog recordDialog;
        AlertDialog.Builder record = new AlertDialog.Builder(NewNoteActivity.this);
        record.setMessage("Recording");
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
        record.setNeutralButton("Stop Recording", (dialogInterface, i) -> {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            newNoteAudioContainer.setVisibility(View.VISIBLE);
            mAudio = 1;
        });
        recordDialog = record.create();
        recordDialog.setCancelable(false);
        recordDialog.show();*/
    }

    private void setNotification() {
      /*  Intent myIntent = new Intent(this, ReminderService.class);
        myIntent.putExtra(getResources().getString(R.string.notificationtitle), mTitle.getText().toString());
        myIntent.putExtra(getResources().getString(R.string.notificationcontent), mNote.getText().toString());
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, mHour);
        calendar.set(java.util.Calendar.MINUTE, mMinutes);
        calendar.set(java.util.Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);*/
    }

    private boolean verifyAndSave() {
        if (mNote.getText().toString().equalsIgnoreCase("") || mNote.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.noNote), Toast.LENGTH_SHORT).show();
            return false;
        } else if (mTitle.getText().toString().equalsIgnoreCase("") || mTitle.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.noTitle), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void validateSize(int position) {
        if (mImagesLocations.size() <= 0) {
            mImageRecyclerView.setVisibility(View.GONE);
        } else {
            mImageRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            RefWatcher refWatcher = MyApplication.getRefWatcher(this);
            refWatcher.watch(this);
        }
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
            notificationBuilder.setContentText(i.getExtras().getString(mContext.getResources().getString(R.string.notificationcontent)));
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notificationBuilder.build());
        }

    }
}