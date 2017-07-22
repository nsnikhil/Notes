/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.util.interfaces;

import android.database.Cursor;

/*
NoteObservable emits item and observer can be added
to to observables once observable emit new item their
update method is called which notifies all the
registered observers that a change has occurred and they
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
