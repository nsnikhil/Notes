package com.nrs.nsnik.notes.objects;


import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

public class NoteObject implements Serializable{

    private String title,note,audioLocation,folderName;
    private ArrayList<String> images;
    private int reminder;

    public NoteObject(String title,String note,ArrayList<String> images,String audioLocation,int reminder,String folderName){
        this.note = note;
        this.title = title;
        this.images = images;
        this.audioLocation = audioLocation;
        this.reminder = reminder;
        this.folderName = folderName;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public String getAudioLocation() {
        return audioLocation;
    }

    public int getReminder() {
        return reminder;
    }

    public String getFolderName() {
        return folderName;
    }

}
