package com.nrs.nsnik.notes.interfaces;

import android.database.Cursor;

/*
NoteObservable emits item and observer can be added
to to observables once observable emit new item their
update method is called which notifies all the
registered observers that a change has occured and they
pass any new data to the observers if any
 */

public interface NoteObservable {

    /*
    @param noteObserver     instance of noteObserver that wants to register to listen for changes
     */
    void add(NoteObserver noteObserver);

    /*
    @param noteObserver     unregister this noteObserver
     */
    void remove(NoteObserver noteObserver);

    /*
    @param cursor   on new data available pass the new cursor to all the
                    observers
     */
    void updateObserver(Cursor cursor);

}
