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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.nrs.nsnik.notes.data.TableNames.table1;
import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
    String mAudioFileName;
    String mFoldername = "nofolder";
    Uri intentUri = null;
    Bitmap mImage = null;
    int hour = 289;
    int minutes = 291;
    int mReminder = 0;
    int audio = 0;
    MediaRecorder mRecorder;
    MediaPlayer mPlayer;
    ArrayList<Bitmap> imagesArray;
    ArrayList<String> imagesLocations;
    ImageAdapter imageAdapter;
    FileOperation fileOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        initilize();
        setClickListener();
        if(getIntent().getData()!=null){
            intentUri = getIntent().getData();
            try {
                setNote();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(getIntent().getExtras()!=null){
            mFoldername  = getIntent().getExtras().getString(getResources().getString(R.string.newnotefolderbundle));
        }
    }

    private void setNote() throws IOException, ClassNotFoundException {
        Cursor c = getContentResolver().query(intentUri,null,null,null,null);
        if(c.moveToFirst()){
            title.setText(c.getString(c.getColumnIndex(table1.mTitile)));
            File folder = getExternalFilesDir(getResources().getString(R.string.folderName));
            File f = new File(folder,c.getString(c.getColumnIndex(table1.mFileName)));
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try{

                //setNoteText
                fis = new FileInputStream(f);
                ois = new ObjectInputStream(fis);
                NoteObject obj = (NoteObject) ois.readObject();
                note.setText(obj.getNote());

                //setNoteImages
                if(obj.getImages().size()>0){
                    imageRecyclerView.setVisibility(View.VISIBLE);
                    for(int i=0;i<obj.getImages().size();i++) {
                        imagesLocations.add(obj.getImages().get(i));
                        File path = new File(folder,obj.getImages().get(i));
                        imagesArray.add(BitmapFactory.decodeFile(path.toString()));
                    }
                }

                //setAudio
                if(obj.getAudioLocation()!=null){
                    newNoteAudioContainer.setVisibility(View.VISIBLE);
                    mAudioFileName = obj.getAudioLocation();
                }

                //setReminder
                if(obj.getReminder()!=0){
                    hour = 292;
                    minutes = 392;
                }

                //foldername
                mFoldername = obj.getFolderName();

            }catch(Exception e){
                e.printStackTrace();
            }finally {
                fis.close();
                ois.close();
            }
        }
    }

    private void deleteNote() throws IOException {
        if(intentUri!=null){
            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(NewNoteActivity.this);
            deleteDialog.setTitle(getResources().getString(R.string.warning));
            deleteDialog.setMessage(getResources().getString(R.string.deletesingledialog));
            deleteDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    imagesArray.clear();
                    imagesLocations.clear();
                    try {
                        deleteFiles();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int count = getContentResolver().delete(intentUri,null,null);
                    if (count == 0) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletenotefailed), Toast.LENGTH_SHORT).show();
                    } else {
                        finish();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.delete), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            deleteDialog.create().show();
        }
    }

    private void  deleteFiles() throws IOException {
        Cursor c = getContentResolver().query(intentUri,null,null,null,null);
        if(c.moveToFirst()){
            File folder = getExternalFilesDir(getResources().getString(R.string.folderName));
            File f = new File(folder,c.getString(c.getColumnIndex(table1.mFileName)));
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try{
                fis = new FileInputStream(f);
                ois = new ObjectInputStream(fis);
                NoteObject obj = (NoteObject) ois.readObject();
                for(int i=0;i<obj.getImages().size();i++){
                    File path = new File(folder,obj.getImages().get(i));
                    path.delete();
                }
                File file = new File(folder,c.getString(c.getColumnIndex(table1.mFileName)));
                file.delete();
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                fis.close();
                ois.close();
            }

        }
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
                if (verifyAndSave()) {
                    try {
                        if(intentUri!=null){
                            updateNote();
                        }else {
                            saveNote();
                        }
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.newNoteMenuDelete:
                try {
                    deleteNote();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    private void updateNote() throws IOException {
        Cursor c = getContentResolver().query(intentUri,null,null,null,null);
        if(hour!=289||minutes!=291){
            mReminder = 1;
        }
        if(c.moveToFirst()){
            NoteObject noteObject = new NoteObject(title.getText().toString(),note.getText().toString(),imagesLocations,mAudioFileName,mReminder,mFoldername);
            fileOperation.updateNote(c.getString(c.getColumnIndex(table1.mFileName)),noteObject,intentUri);
        }
    }

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

    private void saveNote() throws IOException {
        if(hour!=289||minutes!=291){
           mReminder = 1;
        }
        NoteObject noteObject = new NoteObject(title.getText().toString(),note.getText().toString(),imagesLocations,mAudioFileName,mReminder,mFoldername);
        fileOperation.saveNote(makeNoteName(),noteObject);
    }

    private String makeNoteName() {
        Calendar c = Calendar.getInstance();
        return title.getText().toString() + c.getTimeInMillis() + ".txt";
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

        //Image
        imageRecyclerView = (RecyclerView) findViewById(R.id.newNoteImageRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imageRecyclerView.setLayoutManager(layoutManager);
        imagesArray = new ArrayList<>();
        imageAdapter = new ImageAdapter(NewNoteActivity.this, imagesArray, this);
        imageRecyclerView.setAdapter(imageAdapter);
        imagesLocations = new ArrayList<>();

        playAudio = (ImageView) findViewById(R.id.newNoteAudioPlay);
        cancelAudio = (ImageView) findViewById(R.id.newNoteAudioCancel);
        seekAudio = (SeekBar) findViewById(R.id.newNoteAudioSeek);
        seekAudio.incrementProgressBy(10);
        newNoteAudioContainer = (LinearLayout) findViewById(R.id.newNoteAudioContainer);
        fileOperation = new FileOperation(getApplicationContext());


    }

    @Override
    public void onBackPressed() {
        if (!layout.isFab()) {
            layout.hide();
        } else {
            super.onBackPressed();
        }
    }

    private String makeImageName(){
        Calendar c = Calendar.getInstance();
        return title.getText().toString() + c.getTimeInMillis() + ".jpg";
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
                        String name = makeImageName();
                        imagesLocations.add(name);
                        fileOperation.saveImage(name,mImage);
                        Toast.makeText(getApplicationContext(),name,Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(),imagesArray.size()+"",Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(),imagesLocations.size()+"",Toast.LENGTH_SHORT).show();
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
                    String name = makeImageName();
                    imagesLocations.add(name);
                    try {
                        fileOperation.saveImage(name,mImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(),name,Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),imagesArray.size()+"",Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),imagesLocations.size()+"",Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
    public void validateSize(int position) {
        if (imagesArray.size() <= 0) {
            imageRecyclerView.setVisibility(View.GONE);
        } else {
            imageRecyclerView.setVisibility(View.VISIBLE);
        }
        imagesLocations.remove(position);
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
