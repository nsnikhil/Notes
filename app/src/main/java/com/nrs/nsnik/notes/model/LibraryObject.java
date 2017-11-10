package com.nrs.nsnik.notes.model;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class LibraryObject {

    @NonNull
    public static Builder builder() {
        return new AutoValue_LibraryObject.Builder();
    }

    public abstract String libraryName();

    public abstract String libraryLink();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder libraryName(String libraryName);

        public abstract Builder libraryLink(String libraryLink);

        public abstract LibraryObject build();
    }

}
