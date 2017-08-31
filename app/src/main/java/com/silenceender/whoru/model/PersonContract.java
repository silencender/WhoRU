package com.silenceender.whoru.model;

import android.provider.BaseColumns;

/**
 * Created by Silen on 2017/8/20.
 */

public final class PersonContract {
    private PersonContract() {}

    public static class DataEntry implements BaseColumns {
        public static final String TABLE_NAME = "person";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_PICNAME = "picname";
    }
}
