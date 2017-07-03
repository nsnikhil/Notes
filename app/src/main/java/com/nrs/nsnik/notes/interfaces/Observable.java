package com.nrs.nsnik.notes.interfaces;

import android.database.Cursor;

/*
Observable emits item and observer can be added
to to observables once observable emit new item their
update method is called which notifies all the
registered observers that a change has occured and they
pass any new data to the observers if any
 */

public interface Observable {

    /*
    @param observer     instance of observer that wants to register to listen for changes
     */
    void add(Observer observer);

    /*
    @param observer     unregister this observer
     */
    void remove(Observer observer);

    /*
    @param cursor   on new data available pass the new cursor to all the
                    observers
     */
    void updateObserver(Cursor cursor);

}
