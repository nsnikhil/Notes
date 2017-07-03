package com.nrs.nsnik.notes.interfaces;


import android.database.Cursor;

/*
Observers act upon the item they received from
observables they keep waiting and whenever a new data is
received they call the update method to perform
operation with new data
 */

public interface Observer {
    /*
    @param cursor   the new data/cursor received from observables
     */
    void updateItems(Cursor cursor);
}
