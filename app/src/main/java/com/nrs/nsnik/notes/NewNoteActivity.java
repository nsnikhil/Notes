package com.nrs.nsnik.notes;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;
import com.nrs.nsnik.notes.adapters.ImageAdapter;
import com.nrs.nsnik.notes.data.TableNames.table1;
import com.nrs.nsnik.notes.helpers.FileOperation;
import com.nrs.nsnik.notes.interfaces.SendSize;
import com.nrs.nsnik.notes.objects.NoteObject;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewNoteActivity extends AppCompatActivity implements View.OnClickListener, Runnable, SendSize {

    private static final int mGetPictureCode = 205;
    private static final int mTakePictureCode = 2;
    private static final int mPermissionCode = 5142;
    private static final int STORAGE_PERMISSION_CODE = 512;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 513;
    private static final String TAG = NewNoteActivity.class.getSimpleName();
    @BindView(R.id.newNoteToolbar)
    Toolbar mNoteToolbar;
    @BindView(R.id.newNoteContent)
    EditText mNote;
    @BindView(R.id.newNoteTitle)
    EditText mTitle;
    @BindView(R.id.newNoteImageRecyclerView)
    RecyclerView imageRecyclerView;
    @BindView(R.id.newNoteAudioContainer)
    LinearLayout newNoteAudioContainer;
    @BindView(R.id.fabtoolbar)
    FABToolbarLayout mFabTools;
    @BindView(R.id.fabtoolbar_fab)
    FloatingActionButton mNoteTools;
    @BindView(R.id.newNoteTakePicture)
    ImageView mTakePicture;
    @BindView(R.id.newNoteChoosePicture)
    ImageView mAddImage;
    @BindView(R.id.newNoteAddAudio)
    ImageView mAddAudio;
    @BindView(R.id.newNoteSetReminder)
    ImageView mAddReminder;
    @BindView(R.id.newNoteAudioPlay)
    ImageView mPlayAudio;
    @BindView(R.id.newNoteAudioCancel)
    ImageView mCancelAudio;
    @BindView(R.id.newNoteAudioSeek)
    SeekBar seekAudio;
    @BindView(R.id.activity_new_note)RelativeLayout mNoteContainer;
    String mAudioFileName, mFolderName = "nofolder";
    Uri mIntentUri = null;
    Bitmap mImage = null;
    int mHour = 289, mMinutes = 291, mReminder = 0, mAudio = 0;
    MediaRecorder mRecorder;
    MediaPlayer mPlayer;
    String mCurrentPhotoPath;
    ArrayList<Bitmap> mImagesArray;
    ArrayList<String> mImagesLocations;
    ImageAdapter mImageAdapter;
    FileOperation mFileOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        ButterKnife.bind(this);
        initialize();
        setClickListener();
        if (getIntent().getData() != null) {
            mIntentUri = getIntent().getData();
            try {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getResources().getString(R.string.editNote));
                }
                setNote();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (getIntent().getExtras() != null) {
            mFolderName = getIntent().getExtras().getString(getResources().getString(R.string.newnotefolderbundle));
        }
    }

    private void setNote() throws IOException {
        Cursor c = getContentResolver().query(mIntentUri, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            mTitle.setText(c.getString(c.getColumnIndex(table1.mTitle)));
            File folder = getExternalFilesDir(getResources().getString(R.string.folderName));
            File f = new File(folder, c.getString(c.getColumnIndex(table1.mFileName)));
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(f);
                ois = new ObjectInputStream(fis);
                NoteObject obj = (NoteObject) ois.readObject();
                mNote.setText(obj.getNote());
                if (obj.getImages().size() > 0) {
                    imageRecyclerView.setVisibility(View.VISIBLE);
                    for (int i = 0; i < obj.getImages().size(); i++) {
                        mImagesLocations.add(obj.getImages().get(i));
                        File path = new File(folder, obj.getImages().get(i));
                        mImagesArray.add(BitmapFactory.decodeFile(path.toString()));
                    }
                    mImageAdapter.modifyList(mImagesLocations);
                }
                if (obj.getAudioLocation() != null) {
                    newNoteAudioContainer.setVisibility(View.VISIBLE);
                    mAudioFileName = obj.getAudioLocation();
                }
                if (obj.getReminder() != 0) {
                    mHour = 292;
                    mMinutes = 392;
                }
                mFolderName = obj.getFolderName();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (ois != null) {
                    ois.close();
                }
                c.close();
            }
        }
    }

    private void deleteNote() {
        if (mIntentUri != null) {
            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(NewNoteActivity.this);
            deleteDialog.setTitle(getResources().getString(R.string.warning));
            deleteDialog.setMessage(getResources().getString(R.string.deleteSingleNoteWarning));
            deleteDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mImagesArray.clear();
                    mImagesLocations.clear();
                    try {
                        mFileOperation.deleteFile(mIntentUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }int count = getContentResolver().delete(mIntentUri, null, null);
                    if (count == 0) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deleteNoteFailed), Toast.LENGTH_SHORT).show();
                    } else {
                        finish();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.delete), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            deleteDialog.create().show();
        }
    }

    private void setClickListener() {
        mTakePicture.setOnClickListener(this);
        mAddImage.setOnClickListener(this);
        mAddAudio.setOnClickListener(this);
        mAddReminder.setOnClickListener(this);
        mNoteTools.setOnClickListener(this);
        mPlayAudio.setOnClickListener(this);
        mCancelAudio.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_note_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getIntent().getData() == null) {
            MenuItem menuItem = menu.findItem(R.id.newNoteMenuDelete);
            menuItem.setVisible(false);
        }
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
            case R.id.newNoteMenuDelete:
                deleteNote();
                break;
        }
        return true;
    }

    private void updateNote() {
        Cursor c = getContentResolver().query(mIntentUri, null, null, null, null);
        if (mHour != 289 || mMinutes != 291) {
            mReminder = 1;
        }
        try {
            if (c != null && c.moveToFirst()) {
                NoteObject noteObject = new NoteObject(mTitle.getText().toString(), mNote.getText().toString(), mImagesLocations, mAudioFileName, mReminder, mFolderName);
                mFileOperation.updateNote(c.getString(c.getColumnIndex(table1.mFileName)), noteObject, mIntentUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
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

    private void saveNote() throws IOException {
        if (mHour != 289 || mMinutes != 291) {
            mReminder = 1;
        }
        NoteObject noteObject = new NoteObject(mTitle.getText().toString(), mNote.getText().toString(), mImagesLocations, mAudioFileName, mReminder, mFolderName);
        mFileOperation.saveNote(makeNoteName(), noteObject);
    }

    private String makeNoteName() {
        Calendar c = Calendar.getInstance();
        return mTitle.getText().toString() + c.getTimeInMillis() + ".txt";
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imageRecyclerView.setLayoutManager(layoutManager);
        mImagesArray = new ArrayList<>();
        mImageAdapter = new ImageAdapter(NewNoteActivity.this, mImagesLocations,this,false);
        imageRecyclerView.setAdapter(mImageAdapter);
        mImagesLocations = new ArrayList<>();
        seekAudio.incrementProgressBy(10);
        mFileOperation = new FileOperation(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        if (!mFabTools.isFab()) {
            mFabTools.hide();
        } else {
            super.onBackPressed();
        }
    }

    private String makeImageName() {
        Calendar c = Calendar.getInstance();
        return mTitle.getText().toString() + c.getTimeInMillis() + ".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case mGetPictureCode:
                if (resultCode == RESULT_OK) {
                    Uri u = data.getData();
                    try {
                        mImage = MediaStore.Images.Media.getBitmap(getContentResolver(), u);
                        mImagesArray.add(mImage);
                        imageRecyclerView.swapAdapter(mImageAdapter, true);
                        imageRecyclerView.setVisibility(View.VISIBLE);
                        String name = makeImageName();
                        mImagesLocations.add(name);
                        mFileOperation.saveImage(name, mImage);
                        mImageAdapter.modifyList(mImagesLocations);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case mTakePictureCode:
                if (resultCode == RESULT_OK && data != null) {
                    addToList();
                }
                break;
        }
    }

    private void addToList() {

        int targetW = mNoteContainer.getWidth();
        int targetH = 200;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImagesArray.add(bitmap);
        imageRecyclerView.swapAdapter(mImageAdapter, true);
        imageRecyclerView.setVisibility(View.VISIBLE);

        String name = makeImageName();
        mImagesLocations.add(name);
        try {
            mFileOperation.saveImage(name, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageAdapter.modifyList(mImagesLocations);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabtoolbar_fab:
                mFabTools.show();
                break;
            case R.id.newNoteTakePicture:
                checkStoragePermission();
                break;
            case R.id.newNoteChoosePicture:
                checkReadPermission();
                break;
            case R.id.newNoteAddAudio:
                checkPermission();
                break;
            case R.id.newNoteSetReminder:
                Calendar c = Calendar.getInstance();
                TimePickerDialog time = new TimePickerDialog(NewNoteActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        mHour = i;
                        mMinutes = i1;
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
                time.show();

                setNotification();
                break;
            case R.id.newNoteAudioPlay:
                mPlayer = new MediaPlayer();
                try {
                    File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
                    File f = new File(folder, mAudioFileName);
                    mPlayer.setDataSource(String.valueOf(f));
                    mPlayer.prepare();
                    mPlayer.start();
                    mPlayAudio.setImageResource(R.drawable.ic_pause_black_24dp);
                    Thread prog = new Thread(this);
                    prog.start();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mPlayAudio.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.newNoteAudioCancel:
                deleteAudioFile();
                mPlayer = null;
                mRecorder = null;
                mAudio = 0;
                mAudioFileName = null;
                newNoteAudioContainer.setVisibility(View.GONE);
                break;
        }
    }

    private void deleteAudioFile() {
        File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
        File f = new File(folder, mAudioFileName);
        if (f.exists()) {
            f.delete();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, mPermissionCode);
            return;
        }
        recordAudio();
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            return;
        }
        startTheCamera();
    }

    private void checkReadPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
            return;
        }
        startGalleryIntent();
    }

    private void startGalleryIntent(){
        Intent chosePicture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (chosePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chosePicture, mGetPictureCode);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.noGallery), Toast.LENGTH_SHORT).show();
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg",storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void startTheCamera(){
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
                startActivityForResult(takePictureIntent, mTakePictureCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case mPermissionCode:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recordAudio();
                }
                break;
        }
    }

    private void recordAudio() {
        File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
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
        record.setNeutralButton("Stop Recording", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                newNoteAudioContainer.setVisibility(View.VISIBLE);
                mAudio = 1;
            }
        });
        recordDialog = record.create();
        recordDialog.setCancelable(false);
        recordDialog.show();
    }

    private void setNotification() {
        Intent myIntent = new Intent(this, ReminderService.class);
        myIntent.putExtra(getResources().getString(R.string.notificationtitle), mTitle.getText().toString());
        myIntent.putExtra(getResources().getString(R.string.notificationcontent), mNote.getText().toString());
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, mHour);
        calendar.set(java.util.Calendar.MINUTE, mMinutes);
        calendar.set(java.util.Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    public void run() {
        int currentPosition = mPlayer.getCurrentPosition();
        int total = mPlayer.getDuration();
        seekAudio.setMax(total);
        while (mPlayer != null && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mPlayer.getCurrentPosition();
            } catch (Exception e) {
                return;
            }
            seekAudio.setProgress(currentPosition);
        }
    }


    @Override
    public void validateSize(int position) {
        if (mImagesLocations.size() <= 0) {
            imageRecyclerView.setVisibility(View.GONE);
        } else {
            imageRecyclerView.setVisibility(View.VISIBLE);
        }
        //mImagesLocations.remove(position);
    }


    public static class ReminderService extends BroadcastReceiver {
        Context mContext;
        @Override
        public void onReceive(Context context, Intent intent) {
            mContext = context;
            buildNotification(intent);
        }

        private void buildNotification(Intent i) {
            NotificationCompat.Builder notfifcationBuilder = new NotificationCompat.Builder(mContext);
            notfifcationBuilder.setSmallIcon(R.drawable.ic_add_alarm_white_48dp);
            notfifcationBuilder.setContentTitle(i.getExtras().getString(mContext.getResources().getString(R.string.notificationtitle)));
            notfifcationBuilder.setContentText(i.getExtras().getString(mContext.getResources().getString(R.string.notificationcontent)));
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notfifcationBuilder.build());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MyApplication.getRefWatcher(this);
        refWatcher.watch(this);
    }

}
