package com.nexus.nsnik.notes;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.nexus.nsnik.notes.data.TableNames;
import com.nexus.nsnik.notes.data.TableNames.table1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class NewNoteActivity extends AppCompatActivity implements View.OnClickListener, Runnable, SendSize {

    private static final int mGetPictureCode = 205;
    private static final int mTakePictureCode = 2;
    private static final int mPermissionCode = 5142;
    Toolbar newNoteToolbar;
    EditText title, note;
    RecyclerView imageRecyclerView;
    LinearLayout newNoteAudioContainer;
    FABToolbarLayout layout;
    ImageView takePicture, addImage, addAudio, addReminder, playAudio, cancelAudio;
    SeekBar seekAudio;
    android.support.design.widget.FloatingActionButton newNoteMenu;
    String mFileName;
    String mAudioFileName;
    String mImageFileName;
    Uri intentUri = null;
    Cursor intentCursor = null;
    Bitmap mImage = null;
    int hour = 289;
    int minutes = 291;
    int audio = 0;
    MediaRecorder mRecorder;
    MediaPlayer mPlayer;
    String uriFolderName = null;
    ArrayList<Bitmap> imagesArray;
    ImageAdapter imageAdapter;
    String imageName[] = new String[10];
    File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        initilize();
        Bundle extra = null;
        if (getIntent().getExtras() != null) {
            Log.d("here0", "");
            extra = getIntent().getExtras();
            if (extra != null) {
                Log.d("here1", "");
                uriFolderName = extra.getString(getResources().getString(R.string.newnotefolderbundle));
            }
        }
        setClickListener();
        if (getIntent().getData() != null) {
            invalidateOptionsMenu();
            //setNote();
        }
    }

    /*private void setNote() {
        intentUri = getIntent().getData();
        getSupportActionBar().setTitle(getResources().getString(R.string.editNote));
        intentCursor = getContentResolver().query(intentUri, null, null, null, null);
        if (intentCursor.moveToFirst()) {
            title.setText(intentCursor.getString(intentCursor.getColumnIndex(table1.mTitile)));
            setNoteText();
            if (intentCursor.getString(intentCursor.getColumnIndex(table1.mPicture)) != null) {
                setNoteImage();

            }
            if (intentCursor.getString(intentCursor.getColumnIndex(table1.mAudio)) != null) {
                newNoteAudioContainer.setVisibility(View.VISIBLE);
                mAudioFileName = intentCursor.getString(intentCursor.getColumnIndex(table1.mAudio));
            }
        }
    }*/

    private void setNoteText() {
        File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
        File f = new File(folder, intentCursor.getString(intentCursor.getColumnIndex(table1.mNote)));
        try {
            String noteContent = Files.toString(f, Charsets.UTF_8);
            note.setText(noteContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setNoteImage() {
        File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
        //  mImageFileName = intentCursor.getString(intentCursor.getColumnIndex(table1.mPicture));
        File f = new File(folder, mImageFileName);
        String fpath = String.valueOf(f);
        mImage = BitmapFactory.decodeFile(fpath);
        // newNoteImage.setImageBitmap(mImage);
        //  newNoteImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private void setClickListener() {
        takePicture.setOnClickListener(this);
        addImage.setOnClickListener(this);
        addAudio.setOnClickListener(this);
        addReminder.setOnClickListener(this);
        newNoteMenu.setOnClickListener(this);
        playAudio.setOnClickListener(this);
        cancelAudio.setOnClickListener(this);
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
                /*if (verifyAndSave()) {
                    saveToFile();
                    if (hour != 289 && minutes != 291) {
                        setNotification();
                    }
                    if (intentUri == null) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.noteSaved), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.updateNote), Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }*/
                break;
            case R.id.newNoteMenuDelete:
                //deleteNote();
                break;
        }
        return true;
    }

   /* private void deleteNote() {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(NewNoteActivity.this);
        deleteDialog.setTitle(getResources().getString(R.string.warning));
        deleteDialog.setMessage(getResources().getString(R.string.deletesingledialog));
        if(uriFolderName!=null){
            Log.d("Uri",Uri.withAppendedPath(TableNames.mContentUri,uriFolderName).toString());
        }
        deleteDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        deleteDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
                Cursor c = null;
                try{
                    c =  getContentResolver().query(intentUri, null, null, null, null);
                    if (c.moveToFirst()) {
                        if (c.getString(c.getColumnIndex(table1.mNote)) != null) {
                            File f = new File(folder, c.getString(c.getColumnIndex((table1.mNote))));
                            if (f.exists()) {
                                f.delete();
                            }
                        }
                        if (c.getString(c.getColumnIndex(table1.mPicture)) != null) {
                            File fimg = new File(folder, c.getString(c.getColumnIndex(table1.mPicture)));
                            if (fimg.exists()) {
                                fimg.delete();
                            }
                        }
                        if (c.getString(c.getColumnIndex(table1.mAudio)) != null) {
                            File faudio = new File(folder, c.getString(c.getColumnIndex(table1.mAudio)));
                            if (faudio.exists()) {
                                faudio.delete();
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(c!=null){
                        c.close();
                    }
                }
                int count = getContentResolver().delete(intentUri, null, null);
                if (count != 0) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletednote), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletenotefailed), Toast.LENGTH_SHORT).show();
                }
            }
        });
        deleteDialog.create().show();
    }*/

    private boolean verifyAndSave() {
        if (note.getText().toString().equalsIgnoreCase("") || note.getText().toString().isEmpty() || note.getText().toString() == null) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.noNote), Toast.LENGTH_SHORT).show();
            return false;
        } else if (title.getText().toString().equalsIgnoreCase("") || title.getText().toString().isEmpty() || title.getText().toString() == null) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.noTitle), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

   /* private void saveToFile() {
        ContentValues cv = new ContentValues();
        if (intentUri == null) {
            saveNoteToFile();
            cv.put(table1.mTitile, title.getText().toString());
            cv.put(table1.mNote, mFileName);
            saveImages(cv);
            if (hour != 289 && minutes != 291) {
                cv.put(table1.mReminder, 1);
            }
            if (audio == 1) {
                cv.put(table1.mAudio, mAudioFileName);
            }
            if (uriFolderName != null) {
                cv.put(table1.mFolderName, uriFolderName);
            } else {
                cv.put(table1.mFolderName, getResources().getString(R.string.nofolder));
            }
            insertVal(cv);

        } else {
            if (updateToFile()) {
                cv.put(table1.mTitile, title.getText().toString());
                cv.put(table1.mNote, mFileName);
                updateImageToFile();
                cv.put(table1.mPicture, mImageFileName);
                cv.put(table1.mAudio, mAudioFileName);
                if (hour != 289 && minutes != 291) {
                    cv.put(table1.mReminder, 1);
                }
                updateVal(cv);
            }
        }
    }*/


    private void insertVal(ContentValues cv) {
        Uri u = getContentResolver().insert(TableNames.mContentUri, cv);
        if (u == null) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.insertFailed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.insertedNote), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateVal(ContentValues cv) {
        if (intentUri != null) {
            int count = getContentResolver().update(intentUri, cv, null, null);
            if (count == 0) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.updateFailed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.updateNote), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean updateToFile() {
        File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
        String fileName = null;
        if (intentCursor.moveToFirst()) {
            fileName = intentCursor.getString(intentCursor.getColumnIndex(table1.mNote));
        }
        File f = new File(folder, fileName);
        mFileName = fileName;
        try {
            Files.write(note.getText().toString(), f, Charsets.UTF_8);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveNoteToFile() {
        Calendar c = Calendar.getInstance();
        String fileName = title.getText().toString() + c.getTimeInMillis() + ".txt";
        mFileName = fileName;
        FileOperation.saveText(getApplicationContext(), fileName, note.getText().toString());
    }


   /* private void updateImageToFile() {
        if (mImage != null && intentCursor.getString(intentCursor.getColumnIndex(table1.mPicture)) != null) {
            File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
            String imageFileName = intentCursor.getString(intentCursor.getColumnIndex(table1.mPicture));
            mImageFileName = imageFileName;
            if (mImageFileName != null && mImage != null) {
                File f = new File(folder, imageFileName);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                try {
                    Files.write(byteArray, f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (mImage != null && intentCursor.getString(intentCursor.getColumnIndex(table1.mPicture)) == null) {
            saveImageToFile();
        } else if (mImage == null && intentCursor.getString(intentCursor.getColumnIndex(table1.mPicture)) == null || intentCursor.getString(intentCursor.getColumnIndex(table1.mPicture)) != null) {
            File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
            if (intentCursor.getString(intentCursor.getColumnIndex(table1.mPicture)) != null) {
                File f = new File(folder, intentCursor.getString(intentCursor.getColumnIndex(table1.mPicture)));
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }*/

    private void saveImageToFile() {
        Calendar c = Calendar.getInstance();
        String imageFileName = title.getText().toString() + c.getTimeInMillis() + ".jpg";
        mImageFileName = imageFileName;
        FileOperation.saveImage(getApplicationContext(),imageFileName,null);//replace null with bitmap
    }

    private void initilize() {
        newNoteToolbar = (Toolbar) findViewById(R.id.newNoteToolbar);
        setSupportActionBar(newNoteToolbar);
        newNoteToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        title = (EditText) findViewById(R.id.newNoteTitle);
        note = (EditText) findViewById(R.id.newNoteContent);
        newNoteMenu = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fabtoolbar_fab);
        layout = (FABToolbarLayout) findViewById(R.id.fabtoolbar);
        takePicture = (ImageView) findViewById(R.id.newNoteTakePicture);
        addImage = (ImageView) findViewById(R.id.newNoteChoosePicture);
        addAudio = (ImageView) findViewById(R.id.newNoteAddAudio);
        addReminder = (ImageView) findViewById(R.id.newNoteSetReminder);
        imageRecyclerView = (RecyclerView) findViewById(R.id.newNoteImageRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imageRecyclerView.setLayoutManager(layoutManager);
        playAudio = (ImageView) findViewById(R.id.newNoteAudioPlay);
        cancelAudio = (ImageView) findViewById(R.id.newNoteAudioCancel);
        seekAudio = (SeekBar) findViewById(R.id.newNoteAudioSeek);
        seekAudio.incrementProgressBy(10);
        newNoteAudioContainer = (LinearLayout) findViewById(R.id.newNoteAudioContainer);
        imagesArray = new ArrayList<>();
        imageAdapter = new ImageAdapter(NewNoteActivity.this, imagesArray, this);
        imageRecyclerView.setAdapter(imageAdapter);
    }

    @Override
    public void onBackPressed() {
        if (!layout.isFab()) {
            layout.hide();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case mGetPictureCode:
                if (resultCode == RESULT_OK) {
                    Uri u = data.getData();
                    try {
                        mImage = MediaStore.Images.Media.getBitmap(getContentResolver(), u);
                        imagesArray.add(mImage);
                        imageRecyclerView.swapAdapter(imageAdapter, true);
                        imageRecyclerView.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case mTakePictureCode:
                if (resultCode == RESULT_OK) {
                    Bundle extra = data.getExtras();
                    mImage = (Bitmap) extra.get("data");
                    imagesArray.add(mImage);
                    imageRecyclerView.swapAdapter(imageAdapter, true);
                    imageRecyclerView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void createFileName(int ){
        Calendar c = Calendar.getInstance();
        String imageFileName = title.getText().toString()+ c.getTimeInMillis() + ".jpg";
        File f = new File(folder,imageFileName);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabtoolbar_fab:
                layout.show();
                break;
            case R.id.newNoteTakePicture:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePicture.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePicture, mTakePictureCode);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.noCamera), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.newNoteChoosePicture:
                Intent chosePicture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (chosePicture.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(chosePicture, mGetPictureCode);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.noGallery), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.newNoteAddAudio:
                checkPermission();
                break;
            case R.id.newNoteSetReminder:
                Calendar c = Calendar.getInstance();
                TimePickerDialog time = new TimePickerDialog(NewNoteActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        hour = i;
                        minutes = i1;
                    }
                }, c.getTime().getHours(), c.getTime().getMinutes(), true);
                time.show();
                Toast.makeText(getApplicationContext(), "Set Reminder", Toast.LENGTH_SHORT).show();
                break;
            case R.id.newNoteAudioPlay:
                mPlayer = new MediaPlayer();
                try {
                    File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
                    File f = new File(folder, mAudioFileName);
                    mPlayer.setDataSource(String.valueOf(f));
                    mPlayer.prepare();
                    mPlayer.start();
                    playAudio.setImageResource(R.drawable.pausesmall);
                    Thread prog = new Thread(this);
                    prog.start();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            playAudio.setImageResource(R.drawable.playsmall);
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
                audio = 0;
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
                audio = 1;
            }
        });
        recordDialog = record.create();
        recordDialog.setCancelable(false);
        recordDialog.show();
    }

    private void setNotification() {
        Intent myIntent = new Intent(this, ReminderService.class);
        myIntent.putExtra(getResources().getString(R.string.notificationtitle), title.getText().toString());
        myIntent.putExtra(getResources().getString(R.string.notificationcontent), note.getText().toString());
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, minutes);
        calendar.set(java.util.Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    public void run() {
        int currentPosition = mPlayer.getCurrentPosition();
        int total = mPlayer.getDuration();
        while (mPlayer != null && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
            seekAudio.setProgress(currentPosition);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void validateSize() {
        if (imagesArray.size() <= 0) {
            imageRecyclerView.setVisibility(View.GONE);
        } else {
            imageRecyclerView.setVisibility(View.VISIBLE);
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
            NotificationCompat.Builder notfifcationBuilder = new NotificationCompat.Builder(mContext);
            notfifcationBuilder.setSmallIcon(R.drawable.ic_add_alarm_white_48dp);
            notfifcationBuilder.setContentTitle(i.getExtras().getString(mContext.getResources().getString(R.string.notificationtitle)));
            notfifcationBuilder.setContentText(i.getExtras().getString(mContext.getResources().getString(R.string.notificationcontent)));
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notfifcationBuilder.build());
        }

    }

}
