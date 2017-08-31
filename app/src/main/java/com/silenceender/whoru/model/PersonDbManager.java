package com.silenceender.whoru.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.silenceender.whoru.MainActivity;
import com.silenceender.whoru.utils.PersonDbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Silen on 2017/8/21.
 */

public final class PersonDbManager {

    private Context ctx;
    private static PersonDbHelper mDbHelper;
    private static SQLiteDatabase db;

    public PersonDbManager(Context ctx)
    {
        this.ctx = ctx;
        mDbHelper = new PersonDbHelper(this.ctx);
    }

    public long insert(Person person) {
        db = mDbHelper.getWritableDatabase();
        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(PersonContract.DataEntry.COLUMN_NAME_NAME, person.getName());
        values.put(PersonContract.DataEntry.COLUMN_NAME_PICNAME, person.getPicnames());

        long newRowId = db.insert(PersonContract.DataEntry.TABLE_NAME, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
        return newRowId;
    }

    public void delete(Person person) {
        db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        String selection = PersonContract.DataEntry.COLUMN_NAME_NAME + " LIKE ?";
        String[] selectionArgs = { person.getName() };
        db.delete(PersonContract.DataEntry.TABLE_NAME, selection, selectionArgs);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void clear() {
        db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        db.execSQL("DELETE FROM "+ PersonContract.DataEntry.TABLE_NAME);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public int update(Person person) {
        db = mDbHelper.getReadableDatabase();
        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(PersonContract.DataEntry.COLUMN_NAME_PICNAME, person.getPicnames());

        String selection = PersonContract.DataEntry.COLUMN_NAME_NAME + " LIKE ?";
        String[] selectionArgs = { person.getName() };
        int count = db.update(
                PersonContract.DataEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        db.setTransactionSuccessful();
        db.endTransaction();
        return count;
    }

    public List<Person> query(@Nullable String personName) {
        db = mDbHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor c;
        String[] projection = {
                PersonContract.DataEntry.COLUMN_NAME_NAME,
                PersonContract.DataEntry.COLUMN_NAME_PICNAME
        };

        String selection = PersonContract.DataEntry.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs = { personName };
        if(personName == null) {
            c = db.query(
                    PersonContract.DataEntry.TABLE_NAME,                     // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                // The sort order
            );
        }
        else {
            c = db.query(
                    PersonContract.DataEntry.TABLE_NAME,                     // The table to query
                    projection,                               // The columns to return
                   selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                // The sort order
            );
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        c.moveToFirst();

        if (c.getCount() > 0) {
            List<Person> personList = new ArrayList<Person>(c.getCount());
            do {
                String name = c.getString(
                        c.getColumnIndexOrThrow(PersonContract.DataEntry.COLUMN_NAME_NAME)
                );
                String picname = c.getString(
                        c.getColumnIndexOrThrow(PersonContract.DataEntry.COLUMN_NAME_PICNAME)
                );
                Person person = new Person(name,picname);
                personList.add(person);
            } while (c.moveToNext());
            return personList;
        }
        else return null;
    }

    public static void close() {
        mDbHelper.close();
    }
}
