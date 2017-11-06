/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.data;

import android.arch.persistence.room.TypeConverter;

import org.jetbrains.annotations.Contract;

import java.util.Date;

public class DateConverter {

    @Contract("null -> null")
    @TypeConverter
    public static Date longToDate(Long date) {
        return date == null ? null : new Date(date);
    }

    @Contract("null -> null")
    @TypeConverter
    public static Long dateToLong(Date date) {
        return date == null ? null : date.getTime();
    }

}
