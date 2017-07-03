package com.nrs.nsnik.notes.interfaces;

/*
interfaces used to notify about the
data at a particular position
 */

public interface SendSize {
    /*
    @param position     the position on which change occured
     */
    void validateSize(int position);
}
