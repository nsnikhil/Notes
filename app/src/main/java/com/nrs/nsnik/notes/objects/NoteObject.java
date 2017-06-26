package com.nrs.nsnik.notes.objects;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class NoteObject implements Serializable ,Parcelable{

    private String title,note,audioLocation,folderName;
    private ArrayList<String> images;
    private int reminder;

    /*
    TODO REPLACE WITH AUTO VALUE
     */

    public NoteObject(String title,String note,ArrayList<String> images,String audioLocation,int reminder,String folderName){
        this.note = note;
        this.title = title;
        this.images = images;
        this.audioLocation = audioLocation;
        this.reminder = reminder;
        this.folderName = folderName;
    }

    private NoteObject(Parcel in) {
        title = in.readString();
        note = in.readString();
        audioLocation = in.readString();
        folderName = in.readString();
        images = in.createStringArrayList();
        reminder = in.readInt();
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

    public static final Creator<NoteObject> CREATOR = new Creator<NoteObject>() {
        @Override
        public NoteObject createFromParcel(Parcel in) {
            return new NoteObject(in);
        }

        @Override
        public NoteObject[] newArray(int size) {
            return new NoteObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(note);
        parcel.writeString(audioLocation);
        parcel.writeString(folderName);
        parcel.writeStringList(images);
        parcel.writeInt(reminder);
    }
}
