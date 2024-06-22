package com.example.pm1e1293512;

import android.provider.BaseColumns;

public final class ContactContract {

    private ContactContract() {}

    public static class ContactEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_PHONE = "phone";
        public static final String COLUMN_NAME_NOTE = "note";
    }
}
