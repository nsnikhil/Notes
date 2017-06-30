package com.nrs.nsnik.notes.interfaces;

import android.database.Cursor;

public interface Observable {

    void add(Observer observer);

    void remove(Observer observer);

    void updateObserver(Cursor cursor);

}
