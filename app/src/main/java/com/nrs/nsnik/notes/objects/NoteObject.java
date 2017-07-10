package com.nrs.nsnik.notes.objects;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NoteObject implements Serializable, Parcelable {

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
    private String title, note, folderName;
    private List<String> images, audioLocations;

    /*
    TODO REPLACE WITH AUTO VALUE
     */
    private int reminder;

    public NoteObject(String title, String note, ArrayList<String> images, List<String> audioLocations, int reminder, String folderName) {
        this.note = note;
        this.title = title;
        this.images = images;
        this.audioLocations = audioLocations;
        this.reminder = reminder;
        this.folderName = folderName;
    }

    private NoteObject(Parcel in) {
        title = in.readString();
        note = in.readString();
        audioLocations = in.createStringArrayList();
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

    public List<String> getImages() {
        return images;
    }

    public List<String> getAudioLocations() {
        return audioLocations;
    }

    public int getReminder() {
        return reminder;
    }

    public String getFolderName() {
        return folderName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(note);
        parcel.writeStringList(audioLocations);
        parcel.writeString(folderName);
        parcel.writeStringList(images);
        parcel.writeInt(reminder);
    }
}
